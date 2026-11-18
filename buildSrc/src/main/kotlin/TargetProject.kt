import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.Element

/**
 * An Android project as couchside reads it, without running its own Gradle build: the modules its settings include, each
 * one's namespace, sources, resources and dependencies (through its version catalog), and the app module's manifest
 * and BuildConfig fields. Only what a simulator needs; anything it can't read is left out and reported.
 */
class TargetProject(val root: File) {
    class Dependency(val coordinate: String, val compileOnly: Boolean)

    class Module(val path: String, val dir: File) {
        var namespace: String? = null
        var isApp = false
        var buildConfig = false
        val projects = mutableListOf<String>()
        val libraries = mutableListOf<Dependency>()
        val buildConfigFields = linkedMapOf<String, Pair<String, String>>()
        var versionName: String? = null
        var versionCode: String? = null
        var applicationId: String? = null

        val main get() = dir.resolve("src/main")
        val sourceDirs get() = listOf(main.resolve("kotlin"), main.resolve("java")).filter { it.isDirectory }
        val res get() = main.resolve("res").takeIf { it.isDirectory }
        val assets get() = main.resolve("assets").takeIf { it.isDirectory }
    }

    class Activity(val name: String, val target: String?, val label: String?, val categories: Set<String>, val actions: Set<String>, val enabled: Boolean)

    val warnings = mutableListOf<String>()
    val name: String
    val modules: List<Module>
    private val catalog: Map<String, String>

    init {
        require(root.isDirectory) { "No Android project at $root" }
        val settings = listOf("settings.gradle.kts", "settings.gradle").map(root::resolve).firstOrNull { it.isFile }
            ?: error("$root has no settings.gradle(.kts)")
        val text = settings.readText()
        name = Regex("""rootProject\.name\s*=\s*["']([^"']+)["']""").find(text)?.groupValues?.get(1) ?: root.name
        val paths = Regex("""include\s*\(([^)]*)\)""").findAll(text).flatMap { m -> Regex("""["'](:[^"']+)["']""").findAll(m.groupValues[1]).map { it.groupValues[1] } }
            .plus(Regex("""include\s+(['"]:[^\n]+)""").findAll(text).flatMap { m -> Regex("""['"](:[^'"]+)['"]""").findAll(m.groupValues[1]).map { it.groupValues[1] } })
            .distinct().toList()
        catalog = readCatalog(root.resolve("gradle/libs.versions.toml"))
        modules = paths.map { path -> Module(path, root.resolve(path.removePrefix(":").replace(':', '/'))) }.filter { it.dir.isDirectory }
        modules.forEach(::readModule)
    }

    val app: Module get() = modules.firstOrNull { it.isApp } ?: error("$name has no module applying com.android.application")

    /** The app module, the modules it depends on, and theirs. */
    val used: List<Module> by lazy {
        val byPath = modules.associateBy { it.path }
        val seen = linkedSetOf<Module>()
        fun visit(m: Module) { if (seen.add(m)) m.projects.mapNotNull(byPath::get).forEach(::visit) }
        visit(app)
        seen.toList()
    }

    val packageName: String get() = app.applicationId ?: app.namespace ?: manifestPackage ?: error("No package for the app module")

    // ---- Version catalog (gradle/libs.versions.toml): [versions] and [libraries] ----

    private fun readCatalog(file: File): Map<String, String> {
        if (!file.isFile) return emptyMap()
        val versions = HashMap<String, String>()
        val libraries = HashMap<String, String>()
        var section = ""
        file.readLines().forEach { raw ->
            val line = raw.substringBefore(" #").trim()
            if (line.isEmpty() || line.startsWith("#")) return@forEach
            if (line.startsWith("[")) { section = line.trim('[', ']'); return@forEach }
            val key = line.substringBefore('=').trim().trim('"')
            val value = line.substringAfter('=').trim()
            when (section) {
                "versions" -> versions[key] = value.trim('"')
                "libraries" -> {
                    val coordinate = if (value.startsWith("\"")) value.trim('"') else {
                        fun field(f: String) = Regex("""$f\s*=\s*"([^"]+)"""").find(value)?.groupValues?.get(1)
                        val module = field("module") ?: listOfNotNull(field("group"), field("name")).joinToString(":")
                        val version = field("version") ?: Regex("""version\.ref\s*=\s*"([^"]+)"""").find(value)?.groupValues?.get(1)?.let(versions::get)
                        if (version == null) module else "$module:$version"
                    }
                    libraries[key.replace('_', '-').replace('.', '-')] = coordinate
                }
            }
        }
        return libraries
    }

    // ---- A module's build file ----

    private fun readModule(m: Module) {
        val file = listOf("build.gradle.kts", "build.gradle").map(m.dir::resolve).firstOrNull { it.isFile } ?: return
        val text = file.readText()
        m.isApp = Regex("""com\.android\.application|plugins\.android\.application""").containsMatchIn(text)
        m.namespace = Regex("""namespace\s*=?\s*["']([^"']+)["']""").find(text)?.groupValues?.get(1)
        m.applicationId = Regex("""applicationId\s*=?\s*["']([^"']+)["']""").find(text)?.groupValues?.get(1)
        m.buildConfig = m.isApp || Regex("""buildConfig\s*=\s*true""").containsMatchIn(text)
        m.versionName = Regex("""versionName\s*=\s*(.+)""").find(text)?.groupValues?.get(1)?.let { Regex(""""([^"]+)"""").findAll(it).lastOrNull()?.groupValues?.get(1) }
        m.versionCode = Regex("""versionCode\s*=\s*(.+)""").find(text)?.groupValues?.get(1)?.let { Regex("""\b(\d+)\b""").findAll(it).lastOrNull()?.groupValues?.get(1) }
        Regex("""buildConfigField\s*\(\s*"([^"]+)"\s*,\s*"([^"]+)"\s*,\s*"((?:[^"\\]|\\.)*)"\s*\)""").findAll(text).forEach {
            m.buildConfigFields[it.groupValues[2]] = it.groupValues[1] to it.groupValues[3].replace("\\\"", "\"").replace("\\\\", "\\")
        }
        val dependencies = text.substringAfter("dependencies {", "")
        Regex("""^\s*(implementation|api|compileOnly|runtimeOnly)\s*\((.+)\)""", RegexOption.MULTILINE).findAll(dependencies).forEach { d ->
            val kind = d.groupValues[1]
            val arg = d.groupValues[2].trim()
            when {
                arg.startsWith("project(") -> Regex("""project\(\s*["'](:[^"']+)["']""").find(arg)?.let { m.projects += it.groupValues[1] }
                arg.startsWith("platform(") || arg.startsWith("enforcedPlatform(") || arg.startsWith("files(") || arg.startsWith("fileTree(") -> Unit
                arg.startsWith("libs.") -> {
                    val alias = arg.removePrefix("libs.").substringBefore(')').substringBefore(' ').replace('.', '-').replace('_', '-')
                    val coordinate = catalog[alias]
                    // `{ artifact { type = "aar" } }` asks for the AAR of a library whose POM doesn't say so (j2v8).
                    val aar = Regex("""type\s*=\s*"aar"""").containsMatchIn(dependencies.substring(d.range.last + 1).substringBefore('\n'))
                    if (coordinate == null) warnings += "${m.path}: no library \"$alias\" in the version catalog"
                    else m.libraries += Dependency(if (aar) "$coordinate@aar" else coordinate, kind == "compileOnly")
                }
                arg.startsWith("\"") -> m.libraries += Dependency(arg.substringAfter('"').substringBefore('"'), kind == "compileOnly")
                else -> warnings += "${m.path}: can't read dependency $arg"
            }
        }
    }

    // ---- The app module's manifest ----

    private val manifest: Element? by lazy {
        app.main.resolve("AndroidManifest.xml").takeIf { it.isFile }?.let { DocumentBuilderFactory.newInstance().apply { isNamespaceAware = true }.newDocumentBuilder().parse(it).documentElement }
    }
    private val manifestPackage get() = manifest?.getAttribute("package")?.takeIf { it.isNotEmpty() }

    private fun Element.android(attr: String) = getAttributeNS("http://schemas.android.com/apk/res/android", attr).takeIf { it.isNotEmpty() }
    private fun Element.children(tag: String) = (0 until childNodes.length).map { childNodes.item(it) }.filterIsInstance<Element>().filter { it.tagName == tag }
    private fun qualify(name: String) = if (name.startsWith(".")) packageName + name else if ('.' !in name) "$packageName.$name" else name

    private val application get() = manifest?.children("application")?.firstOrNull()

    val applicationClass: String? get() = application?.android("name")?.let(::qualify)
    val icon: String? get() = application?.android("icon")?.removePrefix("@")
    val banner: String? get() = application?.android("banner")?.removePrefix("@")
    val label: String? get() = application?.android("label")

    val activities: List<Activity> by lazy {
        val app = application ?: return@lazy emptyList()
        (app.children("activity") + app.children("activity-alias")).map { a ->
            val filters = a.children("intent-filter")
            Activity(
                name = qualify(a.android("name")!!),
                target = a.android("targetActivity")?.let(::qualify),
                label = a.android("label"),
                categories = filters.flatMap { f -> f.children("category").mapNotNull { it.android("name") } }.toSet(),
                actions = filters.flatMap { f -> f.children("action").mapNotNull { it.android("name") } }.toSet(),
                enabled = a.android("enabled") != "false",
            )
        }
    }

    val launcher: String? get() = activities.firstOrNull { "android.intent.category.LEANBACK_LAUNCHER" in it.categories }?.name
        ?: activities.firstOrNull { "android.intent.category.LAUNCHER" in it.categories }?.name

    /** The activity (or alias) that offers itself as the home screen, even when the app ships it switched off. */
    val home: Activity? get() = activities.firstOrNull { "android.intent.category.HOME" in it.categories }

    /** A string resource's value from the app module's res/values, for labels like "@string/app_name". */
    fun string(ref: String?): String? {
        ref ?: return null
        if (!ref.startsWith("@string/")) return ref
        val key = ref.removePrefix("@string/")
        used.mapNotNull { it.res?.resolve("values") }.flatMap { it.listFiles().orEmpty().toList() }.filter { it.extension == "xml" }.forEach { f ->
            Regex("""<string\s+name="$key"[^>]*>([^<]*)</string>""").find(f.readText())?.let { return it.groupValues[1] }
        }
        return null
    }
}
