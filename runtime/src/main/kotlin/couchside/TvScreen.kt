package couchside

import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalBackDispatcher
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.focus.onFocusChanged
import kotlinx.coroutines.delay
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import couchside.shim.Activities

/**
 * The TV's screen is 540 dp high (1080p at Android's xhdpi) whatever size the window is, and as wide as the window's
 * shape makes it: 960 dp in a 16:9 window, wider in a wider one. So the app always fills the window, no black bars.
 */
val TV_HEIGHT = 540.dp
private var tvWidth by mutableStateOf(960.dp)

/** The last frame's drawing, kept so a screenshot can be made of it (Control.kt, and the app's own "Tell us" shot). */
object Screen {
    @Volatile var layer: GraphicsLayer? = null
    @Volatile var frames = 0L
}

/**
 * The simulated TV's screen, filling [width] x [height] of window: the app lays out for 540 dp of height as on a 1080p
 * TV, drawn at the window's own resolution so it stays sharp at any size.
 */
@Composable
fun TvScreen(width: Dp, height: Dp) {
    val window = LocalDensity.current
    val scale = height / TV_HEIGHT
    tvWidth = width / scale
    couchside.shim.ScreenSize.width = Math.round(tvWidth.value * 2)
    val tv = remember(window, scale) { Density(window.density * scale, 1f) }
    val layer = rememberGraphicsLayer()
    Screen.layer = layer
    CompositionLocalProvider(LocalDensity provides tv) {
        Box(
            Modifier.requiredSize(tvWidth, TV_HEIGHT).clipToBounds().drawWithContent {
                layer.record { this@drawWithContent.drawContent() }
                drawLayer(layer)
                Screen.frames++
            },
        ) {
            val top = Activities.top
            for (activity in Activities.stack) key(activity) { ActivityLayer(activity, onTop = activity === top) }
        }
    }
}

/**
 * One activity's content. Like Android, an activity under the top one keeps what it remembers but isn't drawn and can't
 * take the remote's focus; when it comes back on top, focus returns to where it was.
 */
@Composable
private fun ActivityLayer(activity: Activity, onTop: Boolean) {
    val host = activity as? ComponentActivity ?: return
    val content = host.content ?: return
    val root = remember { FocusRequester() }
    val focus = LocalFocusManager.current
    var focused by remember { mutableStateOf(false) }
    // Android never leaves a TV screen without focus: when the focused thing goes away (a new page, say), the window
    // hands focus to the first thing that can take it. Compose for Desktop leaves it nowhere, so that's done here.
    LaunchedEffect(onTop, focused) {
        if (!onTop || focused) return@LaunchedEffect
        withFrameNanos { }
        while (!focused) {
            runCatching { root.requestFocus() }
            delay(50)
        }
    }
    CompositionLocalProvider(
        LocalContext provides activity,
        LocalConfiguration provides activity.resources.configuration,
        LocalView provides activity.window.decorView,
        LocalLifecycleOwner provides host,
        LocalViewModelStoreOwner provides host,
        LocalBackDispatcher provides host.onBackPressedDispatcher,
    ) {
        Box(
            Modifier.requiredSize(tvWidth, TV_HEIGHT)
                .drawWithContent { if (onTop) drawContent() }
                .semantics { if (onTop) testTag = TOP_LAYER }
                .tvPointer { onTop }
                .onKeyEvent { event -> dpad(event, focus) }
                .onFocusChanged { focused = it.hasFocus }
                .focusRequester(root)
                .focusProperties { onEnter = { if (!onTop) cancelFocusChange() } }
                .focusRestorer()
                .focusGroup(),
        ) { content() }
    }
}

/**
 * What Compose for Android does with a key nothing in the content took: the D-pad moves focus, OK enters a group and
 * Back leaves it (AndroidComposeView's own key handler). Compose for Desktop only does this for Tab.
 */
private fun dpad(event: KeyEvent, focus: FocusManager): Boolean {
    val direction = when (event.key) {
        Key.DirectionRight -> FocusDirection.Right
        Key.DirectionLeft -> FocusDirection.Left
        Key.DirectionUp, Key.PageUp -> FocusDirection.Up
        Key.DirectionDown, Key.PageDown -> FocusDirection.Down
        Key.DirectionCenter, Key.Enter, Key.NumPadEnter -> FocusDirection.Enter
        Key.Back, Key.Escape -> FocusDirection.Exit
        Key.Tab, Key.NavigateNext -> if (event.isShiftPressed) FocusDirection.Previous else FocusDirection.Next
        Key.NavigatePrevious -> FocusDirection.Previous
        else -> return false
    }
    if (event.type != KeyEventType.KeyDown) return false
    return focus.moveFocus(direction)
}
