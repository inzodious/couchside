package couchside.shim

import android.app.usage.UsageStats
import android.content.ComponentName
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageInstaller
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * What's installed on the simulated TV: the app itself (from its manifest) and stand-ins for the apps a TV has, so a
 * launcher app has a row to show. `apps.json` in the simulator's data folder replaces the stand-ins.
 */
object Apps {
    @Serializable
    data class StandIn(
        val pkg: String,
        val label: String,
        /** Tile colour, #RRGGBB. */
        val color: String = "#30343C",
        val category: Int = ApplicationInfo.CATEGORY_UNDEFINED,
        val system: Boolean = false,
        /** Minutes of use to start with, so Home's facts have something to show. */
        val minutes: Int = 0,
    )

    private val DEFAULTS = listOf(
        StandIn("com.google.android.tv", "Live TV", "#1A73E8", ApplicationInfo.CATEGORY_VIDEO, system = true, minutes = 95),
        StandIn("com.netflix.ninja", "Netflix", "#B1060F", ApplicationInfo.CATEGORY_VIDEO, minutes = 640),
        StandIn("com.google.android.youtube.tv", "YouTube", "#D93025", ApplicationInfo.CATEGORY_VIDEO, minutes = 410),
        StandIn("com.spotify.tv.android", "Spotify", "#1DB954", ApplicationInfo.CATEGORY_AUDIO, minutes = 120),
        StandIn("com.google.android.play.games", "Play Games", "#0F9D58", ApplicationInfo.CATEGORY_GAME, system = true, minutes = 12),
        StandIn("com.android.vending", "Play Store", "#34A853", ApplicationInfo.CATEGORY_PRODUCTIVITY, system = true, minutes = 8),
        StandIn("com.android.tv.settings", "Settings", "#5F6368", ApplicationInfo.CATEGORY_UNDEFINED, system = true, minutes = 21),
    )

    private val json = Json { ignoreUnknownKeys = true; prettyPrint = true }
    private val file get() = File(Device.root, "apps.json")

    val standIns: List<StandIn> by lazy {
        runCatching { json.decodeFromString<List<StandIn>>(file.readText()) }.getOrElse {
            runCatching { if (!file.exists()) file.writeText(json.encodeToString(DEFAULTS)) }
            DEFAULTS
        }
    }

    val MAIN: String? = Target.launcher
    val HOME: String? = Target.homeTarget
    val HOME_ALIAS: String? = Target.home
    const val STAND_IN = "couchside.StandInActivity"

    private val FEATURES = setOf(
        PackageManager.FEATURE_LEANBACK, PackageManager.FEATURE_TELEVISION, PackageManager.FEATURE_ETHERNET, PackageManager.FEATURE_WIFI,
        PackageManager.FEATURE_AUDIO_OUTPUT, PackageManager.FEATURE_SCREEN_LANDSCAPE, PackageManager.FEATURE_HDMI_CEC,
    )

    /** Extra system features, e.g. `amazon.hardware.fire_tv` when the host pretends to be a Fire TV. */
    val extraFeatures: MutableSet<String> = ConcurrentHashMap.newKeySet()

    @JvmStatic fun feature(name: String): Boolean = name in FEATURES || name in extraFeatures

    // ---- The app itself ----

    private fun rId(type: String, name: String) = Device.res().id(name, type, Device.PACKAGE)

    private val own: ApplicationInfo by lazy {
        ApplicationInfo().apply {
            packageName = Device.PACKAGE
            name = Target.applicationClass
            className = name
            processName = Device.PACKAGE
            nonLocalizedLabel = Target.label
            icon = Target.icon?.let { rId(it.substringBefore('/'), it.substringAfter('/')) } ?: 0
            banner = Target.banner?.let { rId(it.substringBefore('/'), it.substringAfter('/')) } ?: 0
            category = ApplicationInfo.CATEGORY_VIDEO
            sourceDir = Device.codePath()
            publicSourceDir = sourceDir
            dataDir = Device.root.path
            nativeLibraryDir = Device.root.path
        }
    }

    @JvmStatic fun own(): ApplicationInfo = own

    private val infos = ConcurrentHashMap<String, ApplicationInfo>()

    private fun standIn(pkg: String) = standIns.firstOrNull { it.pkg == pkg }

    @JvmStatic fun application(pkg: String): ApplicationInfo {
        if (pkg == Device.PACKAGE) return own
        val s = standIn(pkg) ?: throw PackageManager.NameNotFoundException(pkg)
        return infos.getOrPut(pkg) {
            ApplicationInfo().apply {
                packageName = s.pkg
                name = s.label
                nonLocalizedLabel = s.label
                category = s.category
                flags = ApplicationInfo.FLAG_INSTALLED or ApplicationInfo.FLAG_HAS_CODE or (if (s.system) ApplicationInfo.FLAG_SYSTEM else 0)
                uid = 10100 + standIns.indexOf(s)
                sourceDir = "/data/app/${s.pkg}/base.apk"
                publicSourceDir = sourceDir
                dataDir = "/data/data/${s.pkg}"
            }
        }
    }

    @JvmStatic fun applications(): List<ApplicationInfo> = listOf(own) + standIns.map { application(it.pkg) }

    @JvmStatic fun pkg(name: String): PackageInfo = PackageInfo().apply {
        applicationInfo = application(name)
        packageName = name
        if (name == Device.PACKAGE) {
            versionName = buildConfig("VERSION_NAME") as? String ?: "0.1.0"
            versionCode = buildConfig("VERSION_CODE") as? Int ?: 1
        } else {
            versionName = "1.0"
            versionCode = 1
        }
        firstInstallTime = INSTALLED
        lastUpdateTime = INSTALLED
        requestedPermissions = emptyArray()
    }

    private val INSTALLED = System.currentTimeMillis() - 90L * 24 * 3600 * 1000

    private fun buildConfig(field: String): Any? = runCatching { Class.forName(Target.buildConfig).getField(field).get(null) }.getOrNull()

    @JvmStatic fun packages(): List<PackageInfo> = applications().map { pkg(it.packageName) }
    @JvmStatic fun packagesForUid(uid: Int): Array<String>? = applications().filter { it.uid == uid }.map { it.packageName }.toTypedArray().ifEmpty { null }

    @JvmStatic fun label(pkg: String?, fallback: CharSequence?): CharSequence = when (pkg) {
        Device.PACKAGE -> Target.label
        else -> standIn(pkg.orEmpty())?.label ?: fallback ?: pkg.orEmpty()
    }

    // ---- Activities ----

    private fun activityInfo(pkg: String, cls: String): ActivityInfo = ActivityInfo().apply {
        packageName = pkg
        name = cls
        applicationInfo = application(pkg)
        nonLocalizedLabel = if (cls == HOME || cls == HOME_ALIAS) Target.homeLabel else applicationInfo.nonLocalizedLabel
        icon = applicationInfo.icon
        banner = applicationInfo.banner
        if (cls == HOME_ALIAS) targetActivity = HOME
    }

    @JvmStatic fun activity(component: ComponentName): ActivityInfo {
        if (component.packageName != Device.PACKAGE && standIn(component.packageName) == null) throw PackageManager.NameNotFoundException(component.flattenToString())
        return activityInfo(component.packageName, component.className)
    }

    private fun resolved(pkg: String, cls: String) = ResolveInfo().apply { activityInfo = activityInfo(pkg, cls); isDefault = true }

    /** The activities an intent could open: the app's own by component, action or category, and each stand-in's launcher. */
    @JvmStatic fun query(intent: Intent): List<ResolveInfo> {
        intent.component?.let { c ->
            return if (c.packageName == Device.PACKAGE || standIn(c.packageName) != null) listOf(resolved(c.packageName, c.className)) else emptyList()
        }
        val pkg = intent.`package`
        val launcher = intent.hasCategory(Intent.CATEGORY_LEANBACK_LAUNCHER) || intent.hasCategory(Intent.CATEGORY_LAUNCHER)
        return when {
            intent.action == Intent.ACTION_MAIN && intent.hasCategory(Intent.CATEGORY_HOME) ->
                if (HOME_ALIAS != null && (pkg == null || pkg == Device.PACKAGE)) listOf(resolved(Device.PACKAGE, HOME_ALIAS)) else emptyList()
            intent.action == Intent.ACTION_MAIN && launcher ->
                (listOfNotNull(MAIN?.let { resolved(Device.PACKAGE, it) }) + standIns.map { resolved(it.pkg, "${it.pkg}.MainActivity") }).filter { pkg == null || it.activityInfo.packageName == pkg }
            intent.action?.startsWith("android.settings.") == true -> listOf(resolved("com.android.tv.settings", "com.android.tv.settings.MainSettings")).filter { standIn("com.android.tv.settings") != null }
            else -> emptyList()
        }
    }

    @JvmStatic fun launch(pkg: String, leanback: Boolean): Intent? {
        val cls = when {
            pkg == Device.PACKAGE -> MAIN ?: return null
            standIn(pkg) != null -> "$pkg.MainActivity"
            else -> return null
        }
        return Intent(Intent.ACTION_MAIN).addCategory(if (leanback) Intent.CATEGORY_LEANBACK_LAUNCHER else Intent.CATEGORY_LAUNCHER)
            .setComponent(ComponentName(pkg, cls)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    private val componentStates = ConcurrentHashMap<ComponentName, Int>()
    @JvmStatic fun setEnabled(component: ComponentName, state: Int) { componentStates[component] = state }
    @JvmStatic fun enabled(component: ComponentName): Int = componentStates[component] ?: PackageManager.COMPONENT_ENABLED_STATE_DEFAULT

    // ---- Pictures ----

    private val icons = ConcurrentHashMap<String, Drawable>()
    private val banners = ConcurrentHashMap<String, Drawable>()

    private fun tile(s: StandIn, w: Int, h: Int, wide: Boolean): Drawable {
        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val base = android.graphics.Color.parseColor(s.color)
        canvas.drawRect(RectF(0f, 0f, w.toFloat(), h.toFloat()), Paint().apply {
            shader = android.graphics.LinearGradient(0f, 0f, w.toFloat(), h.toFloat(), base, mix(base, 0xFF000000.toInt(), 0.45f), android.graphics.Shader.TileMode.CLAMP)
        })
        val text = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFFFFFFF.toInt()
            typeface = Typeface.create("sans-serif", Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        if (wide) {
            text.textSize = h * 0.26f
            while (text.measureText(s.label) > w * 0.86f && text.textSize > 10f) text.textSize *= 0.92f
            canvas.drawText(s.label, w / 2f, h / 2f - (text.ascent() + text.descent()) / 2f, text)
        } else {
            text.textSize = h * 0.5f
            canvas.drawText(s.label.take(1).uppercase(), w / 2f, h / 2f - (text.ascent() + text.descent()) / 2f, text)
        }
        return BitmapDrawable(Device.resources(), bitmap)
    }

    private fun mix(a: Int, b: Int, t: Float): Int {
        fun ch(shift: Int) = (((a shr shift) and 0xFF) * (1 - t) + ((b shr shift) and 0xFF) * t).toInt() shl shift
        return (0xFF shl 24) or ch(16) or ch(8) or ch(0)
    }

    @JvmStatic fun icon(pkg: String?): Drawable? {
        if (pkg == Device.PACKAGE) return Device.res().drawable(own.icon)
        val s = standIn(pkg.orEmpty()) ?: return null
        return icons.getOrPut(s.pkg) { tile(s, 192, 192, wide = false) }
    }

    @JvmStatic fun banner(pkg: String?): Drawable? {
        if (pkg == Device.PACKAGE) return Device.res().drawable(own.banner)
        val s = standIn(pkg.orEmpty()) ?: return null
        return banners.getOrPut(s.pkg) { tile(s, 320, 180, wide = true) }
    }

    @JvmStatic fun drawable(pkg: String, id: Int): Drawable? = if (pkg == Device.PACKAGE) Device.res().drawable(id) else null

    @JvmStatic fun resources(pkg: String): Resources {
        if (pkg != Device.PACKAGE) throw PackageManager.NameNotFoundException(pkg)
        return Device.resources()
    }

    @JvmStatic fun installer(): PackageInstaller? = null

    // ---- Usage ----

    private val usageFile get() = File(Device.root, "usage.json")
    private val used: MutableMap<String, Long> by lazy {
        ConcurrentHashMap(runCatching { json.decodeFromString<Map<String, Long>>(usageFile.readText()) }.getOrDefault(emptyMap()))
    }
    private val lastUsed = ConcurrentHashMap<String, Long>()

    /** The host calls this as the app on screen changes, so "time used" grows the way it does on a TV. */
    fun addUse(pkg: String, millis: Long) {
        if (millis <= 0) return
        used.merge(pkg, millis, Long::plus)
        lastUsed[pkg] = System.currentTimeMillis()
        runCatching { usageFile.writeText(json.encodeToString(used.toMap())) }
    }

    @JvmStatic fun usage(begin: Long, end: Long): List<UsageStats> = applications().mapNotNull { app ->
        val seeded = (standIn(app.packageName)?.minutes ?: 0) * 60_000L
        val total = seeded + (used[app.packageName] ?: 0L)
        if (total <= 0) return@mapNotNull null
        UsageStats().apply {
            packageName = app.packageName
            firstTimeStamp = begin
            lastTimeStamp = end
            totalTimeInForeground = total
            lastTimeUsed = lastUsed[app.packageName] ?: (System.currentTimeMillis() - 3_600_000L)
        }
    }
}
