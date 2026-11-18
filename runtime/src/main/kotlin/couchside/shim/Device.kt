package couchside.shim

import android.app.Application
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Bundle
import android.view.Display
import android.view.KeyEvent
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream
import java.util.concurrent.ConcurrentHashMap
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * The one simulated TV. Every `Context` answers from here: where the app's files live on this PC, its preferences,
 * the TV's settings, the system services the app asks for, and the activities on screen.
 */
object Device {
    @JvmField val PACKAGE: String = Target.packageName

    /** Where the app's data lives: COUCHSIDE_DATA, or the OS's usual place for an app's data. */
    val root: File by lazy {
        val home = System.getProperty("user.home")
        val dir = System.getenv("COUCHSIDE_DATA")?.let(::File) ?: when {
            System.getProperty("os.name").startsWith("Windows") -> File(System.getenv("APPDATA") ?: "$home\\AppData\\Roaming", "Couchside\\data\\${Target.name}")
            System.getProperty("os.name").startsWith("Mac") -> File(home, "Library/Application Support/Couchside/data/${Target.name}")
            else -> File(System.getenv("XDG_DATA_HOME") ?: "$home/.local/share", "couchside/data/${Target.name}")
        }
        dir.apply { mkdirs() }
    }

    @JvmStatic fun dir(name: String): File = (if (name.isEmpty()) root else File(root, name)).apply { mkdirs() }

    @JvmStatic fun codePath(): String = runCatching { File(Device::class.java.protectionDomain.codeSource.location.toURI()).path }.getOrDefault(root.path)

    // ---- The app ----

    private var app: Application? = null

    /** The app's own Application, made once (the manifest names it; the host passes the class). */
    @JvmStatic fun application(): Application = app ?: error("The desktop host hasn't started the app yet")

    fun start(application: Application) {
        android.os.Looper.setMainThread(Thread.currentThread())
        app = application
        application.onCreate()
    }

    private val resources = Resources()
    private val table by lazy { ResTable() }
    private val packages = PackageManager()
    private val resolver = ContentResolver()
    private val display = Display()
    @JvmStatic fun resources(): Resources = resources
    @JvmStatic fun res(): ResTable = table
    @JvmStatic fun packages(): PackageManager = packages
    @JvmStatic fun resolver(): ContentResolver = resolver
    @JvmStatic fun display(): Display = display
    @JvmStatic fun activities(): Activities = Activities

    @JvmStatic fun asset(name: String): InputStream =
        Device::class.java.classLoader.getResourceAsStream("couchside-assets/$name") ?: throw FileNotFoundException("asset $name")

    @JvmStatic fun assets(path: String): Array<String> {
        val index = Device::class.java.classLoader.getResourceAsStream("couchside-assets/index.txt")?.bufferedReader()?.readLines().orEmpty()
        val prefix = if (path.isEmpty()) "" else "$path/"
        return index.filter { it.startsWith(prefix) }.map { it.removePrefix(prefix).substringBefore('/') }.distinct().toTypedArray()
    }

    // ---- Preferences and settings ----

    private val prefs = ConcurrentHashMap<String, Prefs>()
    @JvmStatic fun prefs(name: String): SharedPreferences = prefs.getOrPut(name) { Prefs(File(dir("shared_prefs"), "$name.json")) }
    @JvmStatic fun deletePrefs(name: String): Boolean { prefs.remove(name); return File(dir("shared_prefs"), "$name.json").delete() }

    private val settingsFile get() = File(root, "tv-settings.json")
    private val settings: MutableMap<String, String> by lazy {
        val saved = runCatching { Json.parseToJsonElement(settingsFile.readText()).jsonObject.mapValues { it.value.jsonPrimitive.content } }.getOrDefault(emptyMap())
        ConcurrentHashMap(DEFAULT_SETTINGS + saved)
    }
    private val DEFAULT_SETTINGS = mapOf(
        "global/device_name" to "Couchside",
        "secure/android_id" to "51mu1a70r0000001",
        "global/animator_duration_scale" to "1.0",
        "system/font_scale" to "1.0",
    )

    @JvmStatic fun setting(table: String, name: String): String? = settings["$table/$name"]

    @JvmStatic fun putSetting(table: String, name: String, value: String?): Boolean {
        if (value == null) settings.remove("$table/$name") else settings["$table/$name"] = value
        runCatching { settingsFile.writeText(JsonObject(settings.toSortedMap().mapValues { JsonPrimitive(it.value) }).toString()) }
        return true
    }

    // ---- What kind of TV this is (the host's menu changes these) ----

    /** The RAM the TV reports (some apps treat a TV under 2.5 GB as a light one). */
    @Volatile var ramMb: Int = (System.getenv("COUCHSIDE_RAM_MB")?.toIntOrNull() ?: 4096)
    @Volatile var hz: Float = 60f
    @JvmStatic fun ramBytes(): Long = ramMb * 1024L * 1024L
    @JvmStatic fun lowRam(): Boolean = ramMb <= 1024
    @JvmStatic fun refreshRate(): Float = hz

    @JvmStatic fun permission(name: String): Int = when (name) {
        // The microphone belongs to a TV remote; without one the app's own "no mic" paths show.
        "android.permission.RECORD_AUDIO" -> PackageManager.PERMISSION_DENIED
        else -> PackageManager.PERMISSION_GRANTED
    }

    // ---- System services ----

    private val services = ConcurrentHashMap<String, Any>()
    private val makers: Map<String, () -> Any> = mapOf(
        Context.ACTIVITY_SERVICE to { android.app.ActivityManager() },
        Context.POWER_SERVICE to { android.os.PowerManager() },
        Context.ACCESSIBILITY_SERVICE to { android.view.accessibility.AccessibilityManager() },
        Context.UI_MODE_SERVICE to { android.app.UiModeManager() },
        Context.USAGE_STATS_SERVICE to { android.app.usage.UsageStatsManager() },
        Context.WINDOW_SERVICE to { Windows },
        Context.AUDIO_SERVICE to { android.media.AudioManager.instance() },
    )
    private val names: Map<Class<*>, String> = mapOf(
        android.app.ActivityManager::class.java to Context.ACTIVITY_SERVICE,
        android.os.PowerManager::class.java to Context.POWER_SERVICE,
        android.view.accessibility.AccessibilityManager::class.java to Context.ACCESSIBILITY_SERVICE,
        android.app.UiModeManager::class.java to Context.UI_MODE_SERVICE,
        android.app.usage.UsageStatsManager::class.java to Context.USAGE_STATS_SERVICE,
        android.view.WindowManager::class.java to Context.WINDOW_SERVICE,
        android.media.AudioManager::class.java to Context.AUDIO_SERVICE,
    )

    private val missing = ConcurrentHashMap.newKeySet<String>()

    /** A system service, or null for one the simulated TV doesn't have (logged once, so a needed one is easy to spot). */
    @JvmStatic fun service(name: String): Any? {
        val make = makers[name]
        if (make == null) {
            if (missing.add(name)) android.util.Log.d("Simulator", "No system service \"$name\" on the simulated TV")
            return null
        }
        return services.getOrPut(name, make)
    }

    @JvmStatic fun service(type: Class<*>): Any? {
        val name = names[type]
        if (name == null) {
            if (missing.add(type.name)) android.util.Log.d("Simulator", "No system service ${type.simpleName} on the simulated TV")
            return null
        }
        return service(name)
    }

    @JvmStatic fun serviceName(type: Class<*>): String? = names[type]

    // ---- Activities, services and broadcasts ----

    @JvmStatic fun startActivity(from: Context, intent: Intent, options: Bundle?) = Activities.start(from, intent)

    private val running = ConcurrentHashMap<String, android.app.Service>()

    @JvmStatic fun startService(from: Context, intent: Intent): ComponentName? {
        val component = intent.component ?: return null
        onMain {
            runCatching {
                val service = running.getOrPut(component.className) {
                    (Class.forName(component.className).getDeclaredConstructor().newInstance() as android.app.Service).also { it.onCreate() }
                }
                service.onStartCommand(intent, 0, 1)
            }.onFailure { android.util.Log.w("Simulator", "Service ${component.shortClassName} didn't start: $it") }
        }
        return component
    }

    @JvmStatic fun stopService(intent: Intent): Boolean {
        val service = running.remove(intent.component?.className ?: return false) ?: return false
        onMain { service.onDestroy() }
        return true
    }

    @JvmStatic fun stopService(service: android.app.Service) {
        if (running.remove(service.javaClass.name, service)) onMain { service.onDestroy() }
    }

    private val receivers = java.util.concurrent.CopyOnWriteArrayList<Pair<BroadcastReceiver, IntentFilter>>()

    @JvmStatic fun register(receiver: BroadcastReceiver?, filter: IntentFilter): Intent? {
        if (receiver != null) receivers += receiver to filter
        return null
    }

    @JvmStatic fun unregister(receiver: BroadcastReceiver) { receivers.removeAll { it.first === receiver } }

    /** Delivers to receivers registered in code, and to a receiver class the intent names (the manifest's receivers). */
    @JvmStatic fun broadcast(from: Context, intent: Intent) {
        onMain {
            intent.component?.takeIf { it.packageName == PACKAGE }?.let { component ->
                runCatching { (Class.forName(component.className).getDeclaredConstructor().newInstance() as BroadcastReceiver).onReceive(application(), intent) }
                    .onFailure { android.util.Log.w("Simulator", "Receiver ${component.shortClassName}: $it") }
            }
            receivers.filter { it.second.hasAction(intent.action) }.forEach { (receiver, _) ->
                runCatching { receiver.onReceive(application(), intent) }.onFailure { android.util.Log.w("Simulator", "Receiver ${receiver.javaClass.name}: $it") }
            }
        }
    }

    private fun onMain(work: () -> Unit) {
        if (java.awt.EventQueue.isDispatchThread()) work() else java.awt.EventQueue.invokeLater(work)
    }

    // ---- The window ----

    /** Set by the desktop host: what a window flag or background change should do to the real window. */
    @Volatile var onKeepScreenOn: (Boolean) -> Unit = {}
    @Volatile var onAnnounce: (CharSequence) -> Unit = {}
    @JvmStatic fun keepScreenOn(on: Boolean) = onKeepScreenOn(on)
    @JvmStatic fun announce(text: CharSequence) = onAnnounce(text)
    @JvmStatic fun windowChanged(window: android.view.Window) {}
    @JvmStatic fun windowBackground(window: android.view.Window, drawable: android.graphics.drawable.Drawable?) {}

    // ---- Key names ----

    private val keyNames: Map<Int, String> by lazy {
        KeyEvent::class.java.fields.filter { it.name.startsWith("KEYCODE_") }.associate { it.getInt(null) to it.name }
    }
    @JvmStatic fun keyName(code: Int): String = keyNames[code] ?: code.toString()
    @JvmStatic fun keyCode(name: String): Int =
        keyNames.entries.firstOrNull { it.value == name || it.value == "KEYCODE_$name" }?.key ?: name.toIntOrNull() ?: KeyEvent.KEYCODE_UNKNOWN
}

/** The window manager: one display, and no views to add (a Compose app's overlays are Compose). */
object Windows : android.view.WindowManager {
    override fun getDefaultDisplay(): Display = Device.display()
    override fun removeViewImmediate(view: android.view.View) {}
    override fun addView(view: android.view.View, params: android.view.ViewGroup.LayoutParams) {}
    override fun updateViewLayout(view: android.view.View, params: android.view.ViewGroup.LayoutParams) {}
    override fun removeView(view: android.view.View) {}
}
