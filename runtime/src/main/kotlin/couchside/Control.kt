package couchside

import android.view.KeyEvent
import androidx.compose.ui.graphics.asSkiaBitmap
import couchside.shim.Activities
import couchside.shim.Device
import java.io.File
import java.net.InetAddress
import java.net.ServerSocket
import javax.swing.SwingUtilities
import kotlin.concurrent.thread
import kotlinx.coroutines.runBlocking
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image

/**
 * Test commands over a local socket (127.0.0.1 only, and only when `--control=<port>` is given), one line each, the way
 * `adb shell input` drives a TV:
 *
 *   key DPAD_RIGHT [holdMs]    press a remote key        shot /path/to.png    save what's on screen
 *   home                       the Home button           top                  the activity on screen
 *   frames                     frames drawn so far       quit                 close the simulator
 */
object Control {
    fun listen(port: Int, quit: () -> Unit) {
        val server = ServerSocket(port, 8, InetAddress.getLoopbackAddress())
        thread(isDaemon = true, name = "control") {
            while (true) {
                val socket = runCatching { server.accept() }.getOrNull() ?: break
                thread(isDaemon = true) {
                    socket.use { s ->
                        val out = s.getOutputStream().bufferedWriter()
                        s.getInputStream().bufferedReader().forEachLine { line ->
                            val reply = runCatching { run(line.trim().split(Regex("\\s+")), quit) }.getOrElse { "error: $it" }
                            out.write(reply + "\n")
                            out.flush()
                        }
                    }
                }
            }
        }
    }

    private fun <T> onMain(work: () -> T): T {
        var result: Result<T>? = null
        SwingUtilities.invokeAndWait { result = runCatching(work) }
        return result!!.getOrThrow()
    }

    private fun run(words: List<String>, quit: () -> Unit): String = when (words.firstOrNull()) {
        "key" -> {
            val code = KeyEvent.keyCodeFromString(words[1].uppercase())
            require(code != KeyEvent.KEYCODE_UNKNOWN) { "no key ${words[1]}" }
            onMain { RemoteKeys.tap(code, words.getOrNull(2)?.toIntOrNull() ?: 60) }
            "ok"
        }
        "home" -> { Activities.home(); "ok" }
        "top" -> onMain { Activities.top?.javaClass?.name ?: "none" }
        "frames" -> Screen.frames.toString()
        "shot" -> {
            val layer = Screen.layer ?: error("nothing drawn yet")
            // The layer is the UI thread's: reading it from here while a frame records it crashes Skia.
            val image = onMain { runBlocking { layer.toImageBitmap() } }
            val data = Image.makeFromBitmap(image.asSkiaBitmap()).encodeToData(EncodedImageFormat.PNG) ?: error("couldn't encode")
            File(words[1]).writeBytes(data.bytes)
            "ok ${image.width}x${image.height}"
        }
        "data" -> Device.root.path
        "quit" -> { SwingUtilities.invokeLater(quit); "ok" }
        else -> "error: unknown command"
    }
}
