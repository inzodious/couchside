package androidx.compose.ui.viewinterop

import android.content.Context
import android.view.View
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext

val NoOpUpdate: View.() -> Unit = {}

/**
 * Holds the Android view the app made and leaves its place in the layout. A video view (couchside.media.ShowsVideo) shows
 * its picture there; other views have nothing to draw on a PC.
 */
@Composable
fun <T : View> AndroidView(factory: (Context) -> T, modifier: Modifier = Modifier, update: (T) -> Unit = NoOpUpdate) {
    val context = LocalContext.current
    val view = remember { factory(context) }
    SideEffect { update(view) }
    ViewContent(view, modifier)
}

@Composable
fun <T : View> AndroidView(
    factory: (Context) -> T,
    modifier: Modifier = Modifier,
    onReset: ((T) -> Unit)? = null,
    onRelease: (T) -> Unit = NoOpUpdate,
    update: (T) -> Unit = NoOpUpdate,
) {
    val context = LocalContext.current
    val view = remember { factory(context) }
    SideEffect { update(view) }
    androidx.compose.runtime.DisposableEffect(view) { onDispose { onRelease(view) } }
    ViewContent(view, modifier)
}

@Composable
private fun ViewContent(view: View, modifier: Modifier) {
    if (view is couchside.media.ShowsVideo) couchside.media.VideoOutputView(view.videoOutput, modifier, view.videoFit) else Box(modifier)
}
