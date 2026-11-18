import java.io.File
import java.util.zip.ZipFile
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

/** Each library file as a jar: an AAR's classes.jar (and the jars in its libs/), a jar as it is. */
abstract class LibraryJars : DefaultTask() {
    @get:InputFiles abstract val libraries: ConfigurableFileCollection
    @get:OutputDirectory abstract val out: DirectoryProperty

    @TaskAction
    fun unpack() {
        val dir = out.get().asFile
        dir.deleteRecursively()
        dir.mkdirs()
        libraries.files.forEach { lib ->
            when (lib.extension) {
                "jar" -> lib.copyTo(dir.resolve(lib.name), overwrite = true)
                "aar" -> ZipFile(lib).use { zip ->
                    zip.entries().asSequence().filter { it.name == "classes.jar" || (it.name.startsWith("libs/") && it.name.endsWith(".jar")) }.forEach { e ->
                        val name = if (e.name == "classes.jar") lib.nameWithoutExtension + ".jar" else lib.nameWithoutExtension + "-" + e.name.substringAfter("libs/")
                        zip.getInputStream(e).use { input -> dir.resolve(name).outputStream().use(input::copyTo) }
                    }
                }
            }
        }
    }
}

/**
 * Writes `R.java` for each Android module (ids as compile-time constants, like aapt's) and copies its `res` folder
 * onto the classpath under `couchside-res/<namespace>/`, with an index the runtime's resources read.
 */
abstract class GenerateR : DefaultTask() {
    /** Namespace to res folder, the app module's first. */
    @get:Input abstract val modules: MapProperty<String, String>
    @get:InputFiles abstract val resDirs: ConfigurableFileCollection
    @get:OutputDirectory abstract val java: DirectoryProperty
    @get:OutputDirectory abstract val resources: DirectoryProperty

    @TaskAction
    fun generate() {
        java.get().asFile.deleteRecursively()
        resources.get().asFile.deleteRecursively()
        val index = StringBuilder()
        modules.get().entries.forEachIndexed { m, (namespace, path) ->
            val res = File(path)
            val byType = sortedMapOf<String, MutableMap<String, String>>()
            // The TV is xhdpi; nodpi art wins, then the closest density, then a version-qualified or plain folder.
            val rank = listOf("-nodpi", "-xhdpi", "-xxhdpi", "-xxxhdpi", "-hdpi", "-mdpi", "-anydpi", "")
            res.listFiles().orEmpty().filter { it.isDirectory }.sortedBy { dir ->
                val q = dir.name.substringAfter('-', "").let { if (it.isEmpty()) "" else "-$it" }
                rank.indexOfFirst { it.isNotEmpty() && q.startsWith(it) }.let { if (it < 0) rank.size else it }
            }.forEach { dir ->
                val type = dir.name.substringBefore('-')
                if (type == "values") {
                    dir.listFiles().orEmpty().filter { it.extension == "xml" }.forEach { xml ->
                        Regex("<(string|color|dimen|integer|bool|style|string-array|plurals|array|integer-array|attr|declare-styleable)\\s[^>]*name=\"([^\"]+)\"").findAll(xml.readText()).forEach {
                            val t = when (it.groupValues[1]) { "string-array", "integer-array" -> "array"; "declare-styleable" -> "styleable"; else -> it.groupValues[1] }
                            byType.getOrPut(t) { sortedMapOf() }.putIfAbsent(it.groupValues[2].replace('.', '_'), "${dir.name}/${xml.name}")
                        }
                    }
                } else {
                    dir.listFiles().orEmpty().filter { it.isFile }.forEach { f ->
                        byType.getOrPut(type) { sortedMapOf() }.putIfAbsent(f.name.substringBefore('.'), "${dir.name}/${f.name}")
                    }
                }
            }
            val out = StringBuilder("package $namespace;\n\npublic final class R {\n")
            byType.entries.forEachIndexed { t, (type, names) ->
                out.append("    public static final class $type {\n")
                names.entries.forEachIndexed { i, (name, file) ->
                    val id = 0x7f000000 - (m shl 24) + ((t + 1) shl 16) + i + 1
                    if (type == "styleable") out.append("        public static final int[] $name = {};\n")
                    else {
                        out.append("        public static final int $name = 0x${Integer.toHexString(id)};\n")
                        index.append("$id\t$namespace\t$type\t$name\t$file\n")
                    }
                }
                out.append("    }\n")
            }
            out.append("}\n")
            java.get().asFile.resolve(namespace.replace('.', '/')).apply { mkdirs() }.resolve("R.java").writeText(out.toString())
            res.copyRecursively(resources.get().asFile.resolve("couchside-res/$namespace"), overwrite = true)
        }
        resources.get().asFile.resolve("couchside-res").apply { mkdirs() }.resolve("index.tsv").writeText(index.toString())
    }
}

/**
 * BuildConfig for each module that has one, as the Android build writes it. The app module's String fields can be
 * overridden at run time with the system property `buildconfig.<NAME>` (couchside's `--set NAME=value`).
 */
abstract class GenerateBuildConfig : DefaultTask() {
    /** Package to its fields, each "type\tname\tvalue"; the app's package is [appPackage]. */
    @get:Input abstract val classes: MapProperty<String, List<String>>
    @get:Input abstract val appPackage: Property<String>
    @get:Input abstract val versionCode: Property<Int>
    @get:Input abstract val versionName: Property<String>
    @get:OutputDirectory abstract val out: DirectoryProperty

    @TaskAction
    fun generate() {
        val dir = out.get().asFile
        dir.deleteRecursively()
        classes.get().forEach { (pkg, fields) ->
            val app = pkg == appPackage.get()
            val body = StringBuilder()
            body.append("    public static final boolean DEBUG = false;\n    public static final String BUILD_TYPE = \"release\";\n")
            if (app) {
                body.append("    public static final String APPLICATION_ID = \"$pkg\";\n")
                body.append("    public static final int VERSION_CODE = ${versionCode.get()};\n")
                body.append("    public static final String VERSION_NAME = \"${versionName.get()}\";\n")
            } else {
                body.append("    public static final String LIBRARY_PACKAGE_NAME = \"$pkg\";\n")
            }
            fields.forEach { f ->
                val (type, name, value) = f.split('\t', limit = 3)
                // Strings aren't constants, so a run can point the app elsewhere without rebuilding it.
                val v = if (app && type == "String") "System.getProperty(\"buildconfig.$name\", $value)" else value
                body.append("    public static final $type $name = $v;\n")
            }
            dir.resolve(pkg.replace('.', '/')).apply { mkdirs() }.resolve("BuildConfig.java")
                .writeText("package $pkg;\n\npublic final class BuildConfig {\n$body}\n")
        }
    }
}

/** `couchside/target.properties`: what the runtime needs to know about the app (TargetProject's reading of it). */
abstract class WriteTarget : DefaultTask() {
    @get:Input abstract val values: MapProperty<String, String>
    @get:OutputDirectory abstract val out: DirectoryProperty

    @TaskAction
    fun write() {
        val file = out.get().asFile.resolve("couchside/target.properties").apply { parentFile.mkdirs() }
        val props = java.util.Properties()
        values.get().forEach { (k, v) -> props.setProperty(k, v) }
        file.outputStream().use { props.store(it, "Written by couchside's build") }
    }
}

/**
 * Compose for Android gives a key event's `nativeKeyEvent` as android.view.KeyEvent; on desktop it's an AWT event.
 * For every package of the app that reads it, this writes `repeatCount`/`eventTime` extensions in
 * that package, answered with the Android event the simulator's remote sent (couchside.shim.Keys).
 */
abstract class GenerateNativeKeys : DefaultTask() {
    @get:InputFiles abstract val sources: ConfigurableFileCollection
    @get:OutputDirectory abstract val out: DirectoryProperty

    @TaskAction
    fun generate() {
        val dir = out.get().asFile
        dir.deleteRecursively()
        val packages = sources.asFileTree.filter { it.extension == "kt" }.files
            .filter { "nativeKeyEvent" in it.readText() }
            .mapNotNull { f -> Regex("""^package\s+([\w.]+)""", RegexOption.MULTILINE).find(f.readText())?.groupValues?.get(1) }
            .toSortedSet()
        packages.forEach { pkg ->
            dir.resolve(pkg.replace('.', '/')).apply { mkdirs() }.resolve("CouchsideNativeKeys.kt").writeText(
                """
                |// Written by couchside: what `event.nativeKeyEvent.x` reads on Android, for this package's code.
                |package $pkg
                |
                |internal val Any.repeatCount: Int get() = couchside.shim.Keys.current?.repeatCount ?: 0
                |internal val Any.eventTime: Long get() = couchside.shim.Keys.current?.eventTime ?: android.os.SystemClock.uptimeMillis()
                |""".trimMargin(),
            )
        }
    }
}

/**
 * The few lines of an app, or of a library's sources, that name something only Android has and that no shim can stand
 * in for. A patches.txt lists blocks of `file: <path>` and `- <old>` / `+ <new>` pairs (exact text within one line);
 * the patched copy is compiled in place of the original, and the build stops when an old text is gone.
 */
object PatchList {
    fun read(file: File): Map<String, List<Pair<String, String>>> {
        if (!file.isFile) return emptyMap()
        val out = LinkedHashMap<String, MutableList<Pair<String, String>>>()
        var current: MutableList<Pair<String, String>>? = null
        var old: String? = null
        file.readLines().forEach { line ->
            when {
                line.startsWith("#") || line.isBlank() -> Unit
                line.startsWith("file: ") -> current = out.getOrPut(line.removePrefix("file: ").trim()) { mutableListOf() }
                line.startsWith("- ") -> old = line.removePrefix("- ")
                line.startsWith("+ ") -> { current!!.add(old!! to line.removePrefix("+ ")); old = null }
                else -> error("$file: can't read \"$line\"")
            }
        }
        return out
    }
}

abstract class PatchSources : DefaultTask() {
    /** The app's patch list (paths under the project root). */
    @get:InputFiles abstract val projectPatches: ConfigurableFileCollection
    @get:Input abstract val root: Property<String>
    /** Libraries' patch lists (paths inside the library's sources jar), each with its sources jars. */
    @get:Input abstract val libraryPatches: MapProperty<String, List<String>>
    @get:OutputDirectory abstract val out: DirectoryProperty

    @TaskAction
    fun patch() {
        val dir = out.get().asFile
        dir.deleteRecursively()
        dir.mkdirs()
        fun write(path: String, text: String) {
            // By package path under the output, so the file keeps its name in errors and stack traces.
            val pkg = Regex("""^package\s+([\w.]+)""", RegexOption.MULTILINE).find(text)?.groupValues?.get(1)
            val name = path.substringAfterLast('/')
            dir.resolve((pkg?.replace('.', '/')?.plus("/") ?: "") + name).apply { parentFile.mkdirs() }.writeText(text)
        }
        fun apply(origin: String, path: String, text0: String, edits: List<Pair<String, String>>): String {
            var text = text0
            edits.forEach { (old, new) ->
                check(old in text) { "$origin: \"$old\" is no longer in $path. Update the patch to the new line." }
                text = text.replace(old, new)
            }
            return text
        }
        projectPatches.files.forEach { list ->
            PatchList.read(list).forEach { (path, edits) -> write(path, apply(list.path, path, File(root.get(), path).readText(), edits)) }
        }
        libraryPatches.get().forEach { (list, jars) ->
            PatchList.read(File(list)).forEach { (path, edits) ->
                val text = jars.firstNotNullOfOrNull { jar -> ZipFile(jar).use { zip -> zip.getEntry(path)?.let { zip.getInputStream(it).readBytes().decodeToString() } } }
                    ?: error("$list: no sources jar has $path")
                write(path, apply(list, path, text, edits))
            }
        }
    }
}

/** Everything a run needs, in one folder: lib/ holds the jars numbered in launch order, classpath.txt lists them. */
abstract class Stage : DefaultTask() {
    @get:InputFiles abstract val jars: ConfigurableFileCollection
    @get:Input abstract val order: ListProperty<String>
    /** Copied next to the classpath as they are (target.properties, for the launcher). */
    @get:InputFiles abstract val extras: ConfigurableFileCollection
    /** The app's icon, as icon.png, for the launcher's menu entry. */
    @get:InputFiles @get:Optional abstract val icon: ConfigurableFileCollection
    @get:OutputDirectory abstract val out: DirectoryProperty

    @TaskAction
    fun stage() {
        val dir = out.get().asFile
        dir.deleteRecursively()
        val lib = dir.resolve("lib").apply { mkdirs() }
        val names = order.get().map(::File).filter { it.isFile }.distinct().mapIndexed { i, jar -> "%03d-%s".format(i, jar.name).also { jar.copyTo(lib.resolve(it)) } }
        dir.resolve("classpath.txt").writeText(names.joinToString("\n") { "lib/$it" })
        extras.files.filter { it.isFile }.forEach { it.copyTo(dir.resolve(it.name)) }
        icon.files.firstOrNull { it.isFile }?.copyTo(dir.resolve("icon.png"))
    }
}
