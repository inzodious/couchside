import java.util.Properties
import org.gradle.api.attributes.java.TargetJvmEnvironment
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType

// Compiles an Android project's own source (-Ptarget=<dir>, or COUCHSIDE_TARGET) against the couchside runtime. The project
// is read as it is (TargetProject); a profile for it may add patches.txt, replace/ and shim/ for what can only work on
// a TV. Profiles are looked for in COUCHSIDE_PROFILES, then ~/.config/couchside/profiles, then projects/ here, each
// as <dir>/<name>/. Nothing in the target project is changed, and its own Gradle build is never run.

plugins {
    kotlin("jvm")
    kotlin("plugin.compose")
    kotlin("plugin.serialization")
}

evaluationDependsOn(":runtime")

val targetDir = (findProperty("target") as String?) ?: System.getenv("COUCHSIDE_TARGET")
    ?: error("Say which Android project to build: -Ptarget=<dir> or COUCHSIDE_TARGET")
val target = TargetProject(file(targetDir))
target.warnings.forEach { logger.warn("couchside: $it") }
val profile: File = listOfNotNull(
    System.getenv("COUCHSIDE_PROFILES")?.let(::File),
    File(System.getenv("XDG_CONFIG_HOME") ?: "${System.getProperty("user.home")}/.config", "couchside/profiles"),
    rootDir.resolve("projects"),
).map { it.resolve(target.name) }.firstOrNull { it.isDirectory } ?: rootDir.resolve("projects/${target.name}")

// One build folder per target, so switching projects doesn't throw the other's build away.
layout.buildDirectory.set(rootDir.resolve("build/targets/${target.name}"))

// ---- The project's libraries ----

/**
 * What the project's modules depend on, minus what the desktop provides itself: Compose, lifecycle and activity
 * (Compose for Desktop and the runtime), and the Android flavour of coroutines.
 */
val desktopProvided = listOf(
    "androidx.compose.ui", "androidx.compose.foundation", "androidx.compose.runtime", "androidx.compose.animation",
    "androidx.compose.material", "androidx.compose.material3", "androidx.compose.material3.adaptive", "androidx.compose.ui.tooling",
    "androidx.lifecycle", "androidx.activity", "androidx.savedstate", "androidx.navigationevent",
)

fun Configuration.excludeProvided() {
    desktopProvided.forEach { exclude(group = it) }
    exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-coroutines-android")
}

val libraryCoordinates = target.used.flatMap { it.libraries }.filterNot { d -> desktopProvided.any { d.coordinate.startsWith("$it:") } }
    .filter { it.coordinate.count { c -> c == ':' } >= 2 }

/**
 * Libraries whose Android build can't run on a PC but that have a desktop stand-in (j2v8's V8 becomes Javet's):
 * `libraries/<group>/<name>/desktop` has a line per platform, `<os>-<arch> <coordinate> [more coordinates]`, the os
 * and arch as Java names them. The first coordinate takes the library's place; the others come with it (native
 * builds, say). The library's overlay (its kotlin/ folder) can then put the Android API back on top.
 */
val desktopBuilds: Map<String, List<String>> by lazy {
    val here = "${System.getProperty("os.name").lowercase().substringBefore(' ')}-${System.getProperty("os.arch")}"
    rootDir.resolve("libraries").listFiles().orEmpty().flatMap { g -> g.listFiles().orEmpty().toList() }.mapNotNull { dir ->
        val line = dir.resolve("desktop").takeIf { it.isFile }?.readLines()?.map { it.trim() }?.firstOrNull { it.startsWith("$here ") } ?: return@mapNotNull null
        "${dir.parentFile.name}:${dir.name}" to line.substringAfter(' ').trim().split(Regex("\\s+"))
    }.toMap()
}

/** First pass: as a desktop (JVM) app would, so libraries that have a JVM build (Coil, kotlinx, OkHttp...) get it. */
val jvmLibraries: Configuration by configurations.creating {
    isCanBeConsumed = false
    attributes {
        attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
        attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.LIBRARY))
        attribute(TargetJvmEnvironment.TARGET_JVM_ENVIRONMENT_ATTRIBUTE, objects.named(TargetJvmEnvironment.STANDARD_JVM))
        attribute(KotlinPlatformType.attribute, KotlinPlatformType.jvm)
    }
    excludeProvided()
    resolutionStrategy.dependencySubstitution { desktopBuilds.forEach { (android, desktop) -> substitute(module(android)).using(module(desktop.first())).withoutArtifactSelectors() } }
}

/** Second pass, for what has only an Android build: its AAR (or jar), as an Android app would get it. */
val androidLibraries: Configuration by configurations.creating {
    isCanBeConsumed = false
    attributes {
        attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
        attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.LIBRARY))
        attribute(TargetJvmEnvironment.TARGET_JVM_ENVIRONMENT_ATTRIBUTE, objects.named(TargetJvmEnvironment.ANDROID))
        attribute(KotlinPlatformType.attribute, KotlinPlatformType.androidJvm)
    }
    excludeProvided()
}

dependencies {
    libraryCoordinates.forEach { add(if (it.compileOnly) "compileOnly" else "jvmLibraries", it.coordinate) }
    val declared = libraryCoordinates.map { it.coordinate.split(':').take(2).joinToString(":") }.toSet()
    desktopBuilds.filterKeys { it in declared }.values.forEach { coordinates -> coordinates.drop(1).forEach { add("jvmLibraries", it) } }
    // Resolved with the runtime, so a library and the runtime agree on shared versions (Compose, coroutines).
    add("jvmLibraries", project(":runtime"))
}

/** Everything the runtime brings (it and what it depends on), found in the libraries' own resolution. */
fun runtimeComponents(): Set<org.gradle.api.artifacts.component.ComponentIdentifier> {
    val root = jvmLibraries.incoming.resolutionResult.root
    val start = root.dependencies.filterIsInstance<org.gradle.api.artifacts.result.ResolvedDependencyResult>()
        .map { it.selected }.filter { it.id is org.gradle.api.artifacts.component.ProjectComponentIdentifier }
    val seen = HashSet<org.gradle.api.artifacts.component.ComponentIdentifier>()
    val queue = ArrayDeque(start)
    while (queue.isNotEmpty()) {
        val c = queue.removeFirst()
        if (!seen.add(c.id)) continue
        // A BOM's constraints (coroutines-bom names kotlinx-coroutines-guava, say) aren't dependencies of the runtime.
        c.dependencies.filterIsInstance<org.gradle.api.artifacts.result.ResolvedDependencyResult>().filterNot { it.isConstraint }.forEach { queue += it.selected }
    }
    return seen
}

/** The libraries as jars (an AAR's classes), minus what the runtime already brings. */
val libraryJars = tasks.register<LibraryJars>("libraryJars") {
    libraries.from(provider {
        val runtime = runtimeComponents()
        jvmLibraries.incoming.artifactView { lenient(true) }.artifacts.artifacts.filter { it.id.componentIdentifier !in runtime }.map { it.file }
    })
    out.set(layout.buildDirectory.dir("libraries"))
}

// ---- Overlays: per-library fixes in libraries/<group>/<name>/, per-project ones in the profile ----

val resolvedModules: Set<String> by lazy {
    jvmLibraries.incoming.resolutionResult.allComponents.mapNotNull { (it.id as? org.gradle.api.artifacts.component.ModuleComponentIdentifier)?.let { m -> "${m.group}/${m.module}" } }.toSet()
}

/** What the project's build files ask for, before any desktop stand-in takes a library's place. */
val declaredModules: Set<String> by lazy { libraryCoordinates.map { it.coordinate.split(':').take(2).joinToString("/") }.toSet() }

/** A library overlay applies when the project uses that library (a KMP library's -jvm/-android module counts). */
val libraryOverlays: List<File> by lazy {
    rootDir.resolve("libraries").listFiles().orEmpty().flatMap { g -> g.listFiles().orEmpty().toList() }.filter { dir ->
        val key = "${dir.parentFile.name}/${dir.name}"
        (resolvedModules + declaredModules).any { it == key || it.startsWith("$key-") }
    }
}

val librarySources: Configuration by configurations.creating { isCanBeConsumed = false; isTransitive = false }

// ---- Generated sources and resources ----

val namespaces = target.used.mapNotNull { m -> m.namespace?.let { it to m } }.toMap()

val generateR = tasks.register<GenerateR>("generateR") {
    modules.set(namespaces.filterValues { it.res != null }.mapValues { it.value.res!!.path })
    resDirs.from(namespaces.values.mapNotNull { it.res })
    java.set(layout.buildDirectory.dir("generated/r/java"))
    resources.set(layout.buildDirectory.dir("generated/r/resources"))
}

/** The Hub-style version of a checkout (0.1.<commits>) when the profile asks for it, else what the build file says. */
val commits: Provider<Int> = providers.exec { commandLine("git", "-C", target.root.path, "rev-list", "--count", "HEAD"); isIgnoreExitValue = true }
    .standardOutput.asText.map { it.trim().toIntOrNull() ?: 1 }
val profileProps = Properties().apply { profile.resolve("project.properties").takeIf { it.isFile }?.inputStream()?.use(::load) }

val generateBuildConfig = tasks.register<GenerateBuildConfig>("generateBuildConfig") {
    classes.set(target.used.filter { it.buildConfig && it.namespace != null }.associate { m ->
        val pkg = if (m.isApp) target.packageName else m.namespace!!
        pkg to m.buildConfigFields.map { (name, tv) -> "${tv.first}\t$name\t${tv.second}" }
    })
    appPackage.set(target.packageName)
    versionCode.set(commits.map { c -> profileProps.getProperty("versionCode")?.replace("{commits}", c.toString())?.toInt() ?: target.app.versionCode?.toIntOrNull() ?: 1 })
    versionName.set(commits.map { c -> profileProps.getProperty("versionName")?.replace("{commits}", c.toString()) ?: target.app.versionName ?: "1.0" })
    out.set(layout.buildDirectory.dir("generated/buildconfig"))
}

val writeTarget = tasks.register<WriteTarget>("writeTarget") {
    val home = target.home
    values.set(buildMap {
        put("name", target.name)
        put("label", target.string(target.label) ?: target.name)
        put("package", target.packageName)
        target.applicationClass?.let { put("application", it) }
        target.launcher?.let { put("launcher", it) }
        home?.let { put("home", it.name); put("homeTarget", it.target ?: it.name) }
        put("namespaces", (listOf(target.packageName) + namespaces.keys).distinct().joinToString(","))
        target.icon?.let { put("icon", it) }
        target.banner?.let { put("banner", it) }
        put("buildConfig", "${target.packageName}.BuildConfig")
        home?.label?.let(target::string)?.let { put("homeLabel", it) }
    })
    out.set(layout.buildDirectory.dir("generated/target"))
}

val packAssets = tasks.register<Sync>("packAssets") {
    val dirs = target.used.mapNotNull { it.assets }
    dirs.forEach { from(it) { into("couchside-assets") } }
    into(layout.buildDirectory.dir("generated/assets"))
    doLast {
        val names = dirs.flatMap { d -> d.walkTopDown().filter { it.isFile }.map { it.relativeTo(d).invariantSeparatorsPath }.toList() }.sorted()
        destinationDir.resolve("couchside-assets").mkdirs()
        destinationDir.resolve("couchside-assets/index.txt").writeText(names.joinToString("\n"))
    }
}

val appSources = target.used.flatMap { it.sourceDirs }

val generateNativeKeys = tasks.register<GenerateNativeKeys>("generateNativeKeys") {
    sources.from(appSources)
    out.set(layout.buildDirectory.dir("generated/nativekeys"))
}

val projectPatchList = profile.resolve("patches.txt")
val patchedPaths = PatchList.read(projectPatchList).keys
val patchSources = tasks.register<PatchSources>("patchSources") {
    projectPatches.from(listOf(projectPatchList).filter { it.isFile })
    root.set(target.root.path)
    libraryPatches.set(provider {
        val jars = librarySources.files.map { it.path }
        libraryOverlays.map { it.resolve("patches.txt") }.filter { it.isFile }.associate { it.path to jars }
    })
    out.set(layout.buildDirectory.dir("generated/patched"))
}

/** Files the profile's replace/ has its own version of, by path under a source root. */
val replaced = profile.resolve("replace/kotlin").let { r -> r.walkTopDown().filter { it.isFile }.map { it.relativeTo(r).invariantSeparatorsPath }.toSet() }

sourceSets {
    main {
        appSources.forEach { kotlin.srcDir(it); if (it.name == "java") java.srcDir(it) }
        listOf("shim/kotlin", "replace/kotlin").map(profile::resolve).filter { it.isDirectory }.forEach { kotlin.srcDir(it) }
        profile.resolve("shim/java").takeIf { it.isDirectory }?.let { java.srcDir(it) }
        kotlin.srcDir(provider { libraryOverlays.map { it.resolve("kotlin") }.filter { it.isDirectory } })
        kotlin.srcDir(patchSources.map { it.out })
        kotlin.srcDir(generateNativeKeys.map { it.out })
        java.srcDir(generateR.map { it.java })
        java.srcDir(generateBuildConfig.map { it.out })
        resources.srcDir(generateR.map { it.resources })
        resources.srcDir(packAssets.map { it.destinationDir })
        resources.srcDir(writeTarget.map { it.out })
        val root = target.root.invariantSeparatorsPath + "/"
        kotlin.exclude { f ->
            val path = f.file.invariantSeparatorsPath
            if (!path.startsWith(root)) return@exclude false
            path.removePrefix(root) in patchedPaths || replaced.any { path.endsWith("/src/main/kotlin/$it") || path.endsWith("/src/main/java/$it") }
        }
    }
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        freeCompilerArgs.addAll("-Xskip-prerelease-check", "-opt-in=kotlin.RequiresOptIn", "-nowarn")
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf("-nowarn", "-Xlint:none"))
}

val androidApi: Configuration by configurations.creating { isCanBeConsumed = false }

dependencies {
    implementation(project(":runtime"))
    implementation(files(libraryJars.map { it.out.asFileTree }))
    androidApi(project(":runtime", "androidApiJar"))
    compileOnly(files(androidApi))
}

// A library file compiled again here (a "lib" patch) still reaches its library's internals.
tasks.named<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>("compileKotlin") {
    compilerOptions.freeCompilerArgs.addAll(libraryJars.map { t -> t.out.get().asFile.listFiles().orEmpty().filter { it.name.endsWith(".jar") }.map { "-Xfriend-paths=${it.path}" } })
}

afterEvaluate {
    libraryOverlays.filter { it.resolve("patches.txt").isFile }.forEach { dir ->
        resolvedModules.filter { it == "${dir.parentFile.name}/${dir.name}" || it.startsWith("${dir.parentFile.name}/${dir.name}-") }.forEach { m ->
            val version = jvmLibraries.incoming.resolutionResult.allComponents.mapNotNull { it.id as? org.gradle.api.artifacts.component.ModuleComponentIdentifier }
                .first { "${it.group}/${it.module}" == m }.version
            dependencies.add("librarySources", "${m.replace('/', ':')}:$version:sources@jar")
        }
    }
}

// ---- Running ----

/** The launch order: the app (with its overlays), the runtime, the libraries, Android's framework, android.jar last. */
val launchOrder = provider {
    val rank = { f: File -> if (f.name.startsWith("android-all-")) 2 else if (f.name == "android-api.jar") 3 else if (f.path.contains("/runtime/build/libs/")) 0 else 1 }
    listOf(tasks.jar.get().archiveFile.get().asFile.path) +
        configurations.runtimeClasspath.get().files.sortedBy(rank).map { it.path } +
        androidApi.files.map { it.path }
}

tasks.register<Stage>("stage") {
    dependsOn(tasks.jar, libraryJars)
    jars.from(tasks.jar, configurations.runtimeClasspath, androidApi)
    order.set(launchOrder)
    extras.from(writeTarget.map { it.out.file("couchside/target.properties") })
    icon.from(provider { appIcon() })
    // The launcher stages straight into its apps folder (-Pstage=DIR).
    val stage = providers.gradleProperty("stage").orNull
    if (stage != null) out.set(File(stage)) else out.set(layout.buildDirectory.dir("stage"))
}

/** The biggest PNG of the app's icon (or banner) in its resources, for a menu entry; vector-only icons have none. */
fun appIcon(): List<File> {
    val names = listOfNotNull(target.icon, target.banner).map { it.substringAfter('/') }
    return names.firstNotNullOfOrNull { n ->
        target.used.mapNotNull { it.res }.flatMap { res -> res.listFiles().orEmpty().map { it.resolve("$n.png") } }.filter { it.isFile }.maxByOrNull { it.length() }
    }.let(::listOfNotNull)
}

/** Lists what the project's libraries resolved to, and what couldn't be (`./gradlew :app:libraries -Ptarget=...`). */
tasks.register("libraries") {
    doLast {
        val result = jvmLibraries.incoming.artifactView { lenient(true) }.artifacts
        result.artifacts.forEach { println(it.id.componentIdentifier.displayName + "  " + it.file.name) }
        result.failures.forEach { println("FAILED " + it.message?.lines()?.take(3)?.joinToString(" / ")) }
    }
}

