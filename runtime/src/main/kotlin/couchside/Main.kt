package couchside

import android.app.Application
import android.content.ComponentName
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.LocalWindowExceptionHandlerFactory
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowExceptionHandler
import androidx.compose.ui.window.WindowExceptionHandlerFactory
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import couchside.shim.Activities
import couchside.shim.Apps
import couchside.shim.Device
import couchside.shim.Target

/**
 * Couchside: an Android TV app's own code in a desktop window that behaves like a TV. It opens on the app's home screen
 * when the app is a launcher, else on its launcher activity; the keyboard and mouse are the remote (RemoteKeys.kt, Pointer.kt).
 *
 *   --app              open the launcher activity even when the app has a home screen
 *   --fire-tv          say it's a Fire TV
 *   --light            say it has 2 GB of RAM (a light TV, for apps that care)
 *   --fullscreen       start full screen
 *   --set NAME=VALUE   override a BuildConfig string field of the app module for this run (e.g. a server address)
 *   --control=N        listen on 127.0.0.1:N for test commands (Control.kt)
 */
private object Main

@OptIn(ExperimentalComposeUiApi::class)
fun main(args: Array<String>) {
    val flags = args.toSet()
    if ("--fire-tv" in flags) {
        android.os.Build.MANUFACTURER = "Amazon"
        android.os.Build.BRAND = "Amazon"
        android.os.Build.MODEL = "AFTSIM"
        Apps.extraFeatures += "amazon.hardware.fire_tv"
    }
    if ("--light" in flags) Device.ramMb = 2048
    args.forEachIndexed { i, a ->
        val set = if (a == "--set") args.getOrNull(i + 1) else a.removePrefix("--set=").takeIf { a.startsWith("--set=") }
        set?.takeIf { '=' in it }?.let { System.setProperty("buildconfig." + it.substringBefore('='), it.substringAfter('=')) }
    }
    windowClass("couchside-${Target.name}")
    val port = args.firstOrNull { it.startsWith("--control=") }?.substringAfter('=')?.toIntOrNull() ?: System.getenv("COUCHSIDE_PORT")?.toIntOrNull()

    application {
      // A TV app that hits something the simulator doesn't have yet logs it and keeps going, instead of a dialog.
      CompositionLocalProvider(
        LocalWindowExceptionHandlerFactory provides WindowExceptionHandlerFactory { _ ->
            WindowExceptionHandler { e -> android.util.Log.e("Simulator", "Uncaught in the window", e) }
        },
      ) {
        val state = rememberWindowState(
            size = DpSize(1280.dp, 720.dp),
            placement = if ("--fullscreen" in flags) WindowPlacement.Fullscreen else WindowPlacement.Floating,
        )
        var started by remember { mutableStateOf(false) }
        val quit = {
            // Closing never hangs on a library that won't let go (a decoder or JavaScript runtime mid-shutdown).
            Thread { Thread.sleep(3000); Runtime.getRuntime().halt(0) }.apply { isDaemon = true }.start()
            Activities.shutdown()
            exitApplication()
        }
        val icon = remember { Target.icon?.let { runCatching { Device.res().bitmap(Device.res().id(it, null, null)) }.getOrNull() }?.let { BitmapPainter(it.asImageBitmap()) } }
        Window(onCloseRequest = quit, state = state, title = "${Target.label} · Couchside", icon = icon) {
            LaunchedEffect(Unit) {
                // On the AWT thread, like Android's main thread: the app's Application first, then the first activity.
                Device.start((Target.applicationClass?.let { Class.forName(it).getDeclaredConstructor().newInstance() } ?: Application()) as Application)
                RemoteKeys.install(window, state, quit)
                if ("--app" in flags && Apps.MAIN != null) {
                    Activities.start(Device.application(), Intent(Intent.ACTION_MAIN).setComponent(ComponentName(Device.PACKAGE, Apps.MAIN)))
                } else {
                    Activities.home()
                }
                port?.let { Control.listen(it, quit) }
                started = true
            }
            BoxWithConstraints(Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
                if (started) TvScreen(maxWidth, maxHeight)
            }
        }
      }
    }
    kotlin.system.exitProcess(0)
}

/**
 * The X11 window class, so each app's window gets its own dock icon (the launcher's .desktop file names it). AWT
 * takes it from the main class; changing it needs `--add-opens java.desktop/sun.awt.X11=ALL-UNNAMED`, else it's left.
 */
private fun windowClass(name: String) {
    runCatching {
        val toolkit = java.awt.Toolkit.getDefaultToolkit()
        toolkit.javaClass.getDeclaredField("awtAppClassName").apply { isAccessible = true }.set(toolkit, name)
    }
}
