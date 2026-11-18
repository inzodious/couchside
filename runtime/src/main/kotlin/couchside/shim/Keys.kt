package couchside.shim

import android.view.KeyEvent
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type

/** Android key codes and Compose's keys, and the Android event behind the key Compose is handling right now. */
object Keys {
    /** Set by the desktop host while a key is inside Compose (`nativeKeyEvent` on Android). */
    @Volatile var current: KeyEvent? = null

    /** `nativeKeyEvent` for code that was written for Android: the event the simulator sent, or one made to match the Compose key. */
    @JvmStatic fun native(event: androidx.compose.ui.input.key.KeyEvent): KeyEvent {
        current?.let { return it }
        val code = compose.entries.firstOrNull { it.value == event.key }?.key ?: KeyEvent.KEYCODE_UNKNOWN
        val action = if (event.type == androidx.compose.ui.input.key.KeyEventType.KeyUp) KeyEvent.ACTION_UP else KeyEvent.ACTION_DOWN
        return KeyEvent(action, code)
    }

    /** The Compose key for an Android key code, as Compose for Android maps them. */
    val compose: Map<Int, Key> = mapOf(
        KeyEvent.KEYCODE_DPAD_UP to Key.DirectionUp, KeyEvent.KEYCODE_DPAD_DOWN to Key.DirectionDown, KeyEvent.KEYCODE_DPAD_LEFT to Key.DirectionLeft,
        KeyEvent.KEYCODE_DPAD_RIGHT to Key.DirectionRight, KeyEvent.KEYCODE_DPAD_CENTER to Key.DirectionCenter, KeyEvent.KEYCODE_ENTER to Key.Enter,
        KeyEvent.KEYCODE_NUMPAD_ENTER to Key.NumPadEnter, KeyEvent.KEYCODE_BACK to Key.Back, KeyEvent.KEYCODE_ESCAPE to Key.Escape,
        KeyEvent.KEYCODE_MENU to Key.Menu, KeyEvent.KEYCODE_SPACE to Key.Spacebar, KeyEvent.KEYCODE_DEL to Key.Backspace, KeyEvent.KEYCODE_FORWARD_DEL to Key.Delete,
        KeyEvent.KEYCODE_TAB to Key.Tab, KeyEvent.KEYCODE_PAGE_UP to Key.PageUp, KeyEvent.KEYCODE_PAGE_DOWN to Key.PageDown, KeyEvent.KEYCODE_MOVE_HOME to Key.MoveHome,
        KeyEvent.KEYCODE_MOVE_END to Key.MoveEnd, KeyEvent.KEYCODE_SEARCH to Key.Search, KeyEvent.KEYCODE_SETTINGS to Key.Settings, KeyEvent.KEYCODE_INFO to Key.Info,
        KeyEvent.KEYCODE_GUIDE to Key.Guide, KeyEvent.KEYCODE_TV to Key.Tv, KeyEvent.KEYCODE_CHANNEL_UP to Key.ChannelUp, KeyEvent.KEYCODE_CHANNEL_DOWN to Key.ChannelDown,
        KeyEvent.KEYCODE_MEDIA_PLAY to Key.MediaPlay, KeyEvent.KEYCODE_MEDIA_PAUSE to Key.MediaPause, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE to Key.MediaPlayPause,
        KeyEvent.KEYCODE_MEDIA_STOP to Key.MediaStop, KeyEvent.KEYCODE_MEDIA_NEXT to Key.MediaNext, KeyEvent.KEYCODE_MEDIA_PREVIOUS to Key.MediaPrevious,
        KeyEvent.KEYCODE_MEDIA_REWIND to Key.MediaRewind, KeyEvent.KEYCODE_MEDIA_FAST_FORWARD to Key.MediaFastForward, KeyEvent.KEYCODE_MEDIA_SKIP_FORWARD to Key.MediaSkipForward,
        KeyEvent.KEYCODE_MEDIA_SKIP_BACKWARD to Key.MediaSkipBackward, KeyEvent.KEYCODE_MEDIA_STEP_FORWARD to Key.MediaStepForward, KeyEvent.KEYCODE_MEDIA_STEP_BACKWARD to Key.MediaStepBackward,
        KeyEvent.KEYCODE_MEDIA_RECORD to Key.MediaRecord, KeyEvent.KEYCODE_CAPTIONS to Key.Captions, KeyEvent.KEYCODE_VOLUME_UP to Key.VolumeUp, KeyEvent.KEYCODE_VOLUME_DOWN to Key.VolumeDown,
        KeyEvent.KEYCODE_VOLUME_MUTE to Key.VolumeMute, KeyEvent.KEYCODE_BUTTON_A to Key.ButtonA, KeyEvent.KEYCODE_BUTTON_B to Key.ButtonB, KeyEvent.KEYCODE_BUTTON_X to Key.ButtonX,
        KeyEvent.KEYCODE_BUTTON_Y to Key.ButtonY, KeyEvent.KEYCODE_BUTTON_SELECT to Key.ButtonSelect, KeyEvent.KEYCODE_BUTTON_START to Key.ButtonStart,
        KeyEvent.KEYCODE_PROG_RED to Key.ProgramRed, KeyEvent.KEYCODE_PROG_GREEN to Key.ProgramGreen, KeyEvent.KEYCODE_PROG_YELLOW to Key.ProgramYellow, KeyEvent.KEYCODE_PROG_BLUE to Key.ProgramBlue,
        KeyEvent.KEYCODE_PERIOD to Key.Period, KeyEvent.KEYCODE_COMMA to Key.Comma, KeyEvent.KEYCODE_MINUS to Key.Minus, KeyEvent.KEYCODE_EQUALS to Key.Equals, KeyEvent.KEYCODE_SLASH to Key.Slash,
    ) + (0..25).associate { (KeyEvent.KEYCODE_A + it) to LETTERS[it] } + (0..9).associate { (KeyEvent.KEYCODE_0 + it) to DIGITS[it] }

    private val LETTERS get() = listOf(
        Key.A, Key.B, Key.C, Key.D, Key.E, Key.F, Key.G, Key.H, Key.I, Key.J, Key.K, Key.L, Key.M, Key.N, Key.O, Key.P, Key.Q, Key.R, Key.S, Key.T, Key.U, Key.V, Key.W, Key.X, Key.Y, Key.Z,
    )
    private val DIGITS get() = listOf(Key.Zero, Key.One, Key.Two, Key.Three, Key.Four, Key.Five, Key.Six, Key.Seven, Key.Eight, Key.Nine)
}
