package couchside.shim

import java.util.Properties

/**
 * The app being simulated, as the build read it from the project's manifest and Gradle files (`couchside/target.properties`
 * on the classpath, written by the couchside build).
 */
object Target {
    private val props = Properties().apply {
        Target::class.java.classLoader.getResourceAsStream("couchside/target.properties")?.use(::load)
    }

    private fun get(key: String) = props.getProperty(key)?.takeIf { it.isNotBlank() }

    /** The project's name, for its data folder and the window title. */
    val name: String = get("name") ?: "app"
    val label: String = get("label") ?: name
    val packageName: String = get("package") ?: "app"
    val applicationClass: String? = get("application")
    /** The activity the TV launcher opens (LEANBACK_LAUNCHER, else LAUNCHER). */
    val launcher: String? = get("launcher")
    /** The activity, or activity-alias, the app offers as the TV's home screen; null when it isn't a launcher app. */
    val home: String? = get("home")
    /** What [home] really opens (the alias's target, or [home] itself). */
    val homeTarget: String? = get("homeTarget") ?: home
    val homeLabel: String = get("homeLabel") ?: label
    /** The app's own resource namespaces, the app module's first: an unqualified name is looked up in this order. */
    val namespaces: List<String> = get("namespaces")?.split(',')?.map { it.trim() }.orEmpty()
    /** The app icon and banner, as `type/name` in the app's resources. */
    val icon: String? = get("icon")
    val banner: String? = get("banner")
    val buildConfig: String = get("buildConfig") ?: "$packageName.BuildConfig"
}
