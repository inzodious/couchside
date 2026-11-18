package androidx.activity.compose

import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallbackHandle
import androidx.activity.OnBackPressedDispatcher
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionContext
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState

fun ComponentActivity.setContent(parent: CompositionContext? = null, content: @Composable () -> Unit) {
    this.content = content
}

/** The desktop host provides the top activity's dispatcher. */
val LocalBackDispatcher = compositionLocalOf<OnBackPressedDispatcher?> { null }

@Composable
fun BackHandler(enabled: Boolean = true, onBack: () -> Unit) {
    val current = rememberUpdatedState(onBack)
    val callback = remember { OnBackPressedCallbackHandle(enabled) { current.value() } }
    SideEffect { callback.enabled = enabled }
    val dispatcher = LocalBackDispatcher.current ?: return
    DisposableEffect(dispatcher) {
        dispatcher.add(callback)
        onDispose { dispatcher.remove(callback) }
    }
}
