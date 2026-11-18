package couchside

import android.os.SystemClock
import android.view.KeyEvent
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.input.key.nativeKeyCode
import androidx.compose.ui.input.key.nativeKeyLocation
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import couchside.shim.Activities
import couchside.shim.Keys
import java.awt.KeyboardFocusManager
import java.awt.event.KeyEvent as AwtKey
import javax.swing.Timer

/**
 * The keyboard as the TV's remote. Each key becomes the Android key event a remote (or a keyboard plugged into a TV)
 * would send, goes to the activity on screen through `dispatchKeyEvent` exactly as on Android, and from there into
 * Compose. Holding a key repeats the way Android repeats it: after 500 ms, then every 50 ms, the first repeat marked
 * as a long press.
 *
 *   Arrows            D-pad                       Enter            OK (the remote's centre button)
 *   Esc               Back                        Backspace        Delete, or Back when nothing takes it
 *   Home, F1          Home                        F2, Menu key     Menu
 *   F3                Search                      F5 / F6 / F7     Rewind / Play-pause / Fast-forward
 *   F9 / F10          Channel down / up           Page Up / Down   Page up / down
 *   F11               Full screen                 Ctrl+Q           Quit
 *   Letters, digits, space: themselves, like a keyboard plugged into the TV.
 */
object RemoteKeys {
    private const val REPEAT_AFTER_MS = 500
    private const val REPEAT_EVERY_MS = 50

    private class Held(val code: Int, val downTime: Long, val meta: Int, val char: Char) {
        var repeats = 0
        var repeat: Timer? = null
        var release: Timer? = null
    }

    private val held = HashMap<Int, Held>()
    private lateinit var window: ComposeWindow

    fun install(window: ComposeWindow, state: WindowState, quit: () -> Unit) {
        this.window = window
        Activities.sendToContent = ::toCompose
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher { e ->
            if (!window.isActive) return@addKeyEventDispatcher false
            // A typed character goes straight on to Compose, where a text field being edited takes it (a keyboard plugged into the TV).
            if (e.id == AwtKey.KEY_TYPED) return@addKeyEventDispatcher false
            when {
                e.keyCode == AwtKey.VK_F11 -> if (e.id == AwtKey.KEY_PRESSED) {
                    state.placement = if (state.placement == WindowPlacement.Fullscreen) WindowPlacement.Floating else WindowPlacement.Fullscreen
                }
                e.keyCode == AwtKey.VK_Q && e.isControlDown -> if (e.id == AwtKey.KEY_PRESSED) quit()
                else -> androidCode(e)?.let { code -> if (e.id == AwtKey.KEY_PRESSED) pressed(code, meta(e), e.keyChar) else released(code) }
            }
            true
        }
        window.addWindowFocusListener(object : java.awt.event.WindowAdapter() {
            override fun windowLostFocus(e: java.awt.event.WindowEvent) = held.keys.toList().forEach(::up)
        })
    }

    private fun meta(e: AwtKey): Int =
        (if (e.isShiftDown) KeyEvent.META_SHIFT_ON else 0) or (if (e.isControlDown) KeyEvent.META_CTRL_ON else 0) or (if (e.isAltDown) KeyEvent.META_ALT_ON else 0)

    private fun androidCode(e: AwtKey): Int? = when (e.keyCode) {
        AwtKey.VK_UP, AwtKey.VK_KP_UP -> KeyEvent.KEYCODE_DPAD_UP
        AwtKey.VK_DOWN, AwtKey.VK_KP_DOWN -> KeyEvent.KEYCODE_DPAD_DOWN
        AwtKey.VK_LEFT, AwtKey.VK_KP_LEFT -> KeyEvent.KEYCODE_DPAD_LEFT
        AwtKey.VK_RIGHT, AwtKey.VK_KP_RIGHT -> KeyEvent.KEYCODE_DPAD_RIGHT
        AwtKey.VK_ENTER -> if (e.keyLocation == AwtKey.KEY_LOCATION_NUMPAD) KeyEvent.KEYCODE_NUMPAD_ENTER else KeyEvent.KEYCODE_DPAD_CENTER
        AwtKey.VK_ESCAPE -> KeyEvent.KEYCODE_BACK
        AwtKey.VK_BACK_SPACE -> KeyEvent.KEYCODE_DEL
        AwtKey.VK_DELETE -> KeyEvent.KEYCODE_FORWARD_DEL
        AwtKey.VK_HOME, AwtKey.VK_F1 -> KeyEvent.KEYCODE_HOME
        AwtKey.VK_F2, AwtKey.VK_CONTEXT_MENU -> KeyEvent.KEYCODE_MENU
        AwtKey.VK_F3 -> KeyEvent.KEYCODE_SEARCH
        AwtKey.VK_F5 -> KeyEvent.KEYCODE_MEDIA_REWIND
        AwtKey.VK_F6 -> KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
        AwtKey.VK_F7 -> KeyEvent.KEYCODE_MEDIA_FAST_FORWARD
        AwtKey.VK_F9 -> KeyEvent.KEYCODE_CHANNEL_DOWN
        AwtKey.VK_F10 -> KeyEvent.KEYCODE_CHANNEL_UP
        AwtKey.VK_PAGE_UP -> KeyEvent.KEYCODE_PAGE_UP
        AwtKey.VK_PAGE_DOWN -> KeyEvent.KEYCODE_PAGE_DOWN
        AwtKey.VK_SPACE -> KeyEvent.KEYCODE_SPACE
        AwtKey.VK_TAB -> KeyEvent.KEYCODE_TAB
        AwtKey.VK_PERIOD -> KeyEvent.KEYCODE_PERIOD
        AwtKey.VK_COMMA -> KeyEvent.KEYCODE_COMMA
        AwtKey.VK_MINUS -> KeyEvent.KEYCODE_MINUS
        AwtKey.VK_SLASH -> KeyEvent.KEYCODE_SLASH
        in AwtKey.VK_A..AwtKey.VK_Z -> KeyEvent.KEYCODE_A + (e.keyCode - AwtKey.VK_A)
        in AwtKey.VK_0..AwtKey.VK_9 -> KeyEvent.KEYCODE_0 + (e.keyCode - AwtKey.VK_0)
        in AwtKey.VK_NUMPAD0..AwtKey.VK_NUMPAD9 -> KeyEvent.KEYCODE_0 + (e.keyCode - AwtKey.VK_NUMPAD0)
        else -> null
    }

    private fun pressed(code: Int, meta: Int, char: Char) {
        held[code]?.let { h ->
            // The window system's own key repeat (on X11 a release and a press together): the key is still down.
            h.release?.stop()
            h.release = null
            return
        }
        down(code, meta, char)
    }

    private fun released(code: Int) {
        val h = held[code] ?: return
        h.release?.stop()
        h.release = Timer(8) { up(code) }.apply { isRepeats = false; start() }
    }

    /** A key going down, from the keyboard or from a test command. */
    fun down(code: Int, meta: Int = 0, char: Char = AwtKey.CHAR_UNDEFINED) {
        if (code in held) return
        val now = SystemClock.uptimeMillis()
        val h = Held(code, now, meta, char)
        held[code] = h
        send(KeyEvent(now, now, KeyEvent.ACTION_DOWN, code, 0, meta), char)
        h.repeat = Timer(REPEAT_EVERY_MS) {
            h.repeats++
            val flags = if (h.repeats == 1) KeyEvent.FLAG_LONG_PRESS else 0
            send(KeyEvent(h.downTime, SystemClock.uptimeMillis(), KeyEvent.ACTION_DOWN, code, h.repeats, meta, -1, 0, flags), char)
        }.apply { initialDelay = REPEAT_AFTER_MS; start() }
    }

    fun up(code: Int) {
        val h = held.remove(code) ?: return
        h.repeat?.stop()
        h.release?.stop()
        send(KeyEvent(h.downTime, SystemClock.uptimeMillis(), KeyEvent.ACTION_UP, code, 0, h.meta), h.char)
    }

    /** A full press of [code], held for [holdMs]. */
    fun tap(code: Int, holdMs: Int = 60) {
        down(code)
        Timer(holdMs) { up(code) }.apply { isRepeats = false; start() }
    }

    private var backFromDelete = false

    private fun send(event: KeyEvent, char: Char) {
        typed = char
        val code = event.keyCode
        if (code == KeyEvent.KEYCODE_HOME) {
            // Home never reaches an app: the system takes it. Here it opens the app's home screen (Activities.home).
            if (event.action == KeyEvent.ACTION_UP) Activities.home()
            return
        }
        val used = runCatching { Activities.key(event) }.onFailure { android.util.Log.e("Simulator", "Key ${KeyEvent.keyCodeToString(code)} failed", it) }.getOrDefault(true)
        // A PC's Backspace is the remote's Back wherever nothing wants it as Delete.
        if (code == KeyEvent.KEYCODE_DEL) {
            if (event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) backFromDelete = !used
            if (backFromDelete && (event.action == KeyEvent.ACTION_UP || event.repeatCount == 0)) {
                runCatching { Activities.key(KeyEvent(event.downTime, event.eventTime, event.action, KeyEvent.KEYCODE_BACK, 0, event.metaState)) }
            }
        }
    }

    private var typed: Char = AwtKey.CHAR_UNDEFINED
    private var canvas: java.awt.Component? = null

    /** The component Compose listens for keys on: the one in the window that has key listeners. */
    private fun content(): java.awt.Component? {
        canvas?.let { return it }
        fun find(c: java.awt.Component): java.awt.Component? {
            if (c !== window && c.keyListeners.isNotEmpty()) return c
            if (c is java.awt.Container) for (child in c.components) find(child)?.let { return it }
            return null
        }
        return find(window)?.also { canvas = it } ?: window.takeIf { it.keyListeners.isNotEmpty() }
    }

    /** Hands an Android key event to Compose as the key Compose for Android would see, and says whether it was used. */
    private fun toCompose(event: KeyEvent): Boolean {
        val key = Keys.compose[event.keyCode] ?: return false
        val id = when (event.action) {
            KeyEvent.ACTION_DOWN -> AwtKey.KEY_PRESSED
            KeyEvent.ACTION_UP -> AwtKey.KEY_RELEASED
            else -> return false
        }
        val target = content() ?: return false
        val modifiers = (if (event.isShiftPressed) AwtKey.SHIFT_DOWN_MASK else 0) or (if (event.isCtrlPressed) AwtKey.CTRL_DOWN_MASK else 0) or (if (event.isAltPressed) AwtKey.ALT_DOWN_MASK else 0)
        val awt = AwtKey(target, id, System.currentTimeMillis(), modifiers, key.nativeKeyCode, typed, key.nativeKeyLocation.takeIf { it != AwtKey.KEY_LOCATION_UNKNOWN } ?: AwtKey.KEY_LOCATION_STANDARD)
        val before = Keys.current
        Keys.current = event
        try {
            KeyboardFocusManager.getCurrentKeyboardFocusManager().redispatchEvent(target, awt)
        } finally {
            Keys.current = before
        }
        android.util.Log.v("Simulator", "${KeyEvent.keyCodeToString(event.keyCode)} ${if (event.action == KeyEvent.ACTION_DOWN) "down" else "up"} -> Compose on ${target.javaClass.simpleName}: ${if (awt.isConsumed) "used" else "not used"}")
        return awt.isConsumed
    }
}
