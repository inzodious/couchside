package androidx.media3.ui

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import couchside.media.Fit
import couchside.media.ShowsVideo
import couchside.media.VideoOutput

/**
 * Media3's PlayerView on the simulated TV: the player's video goes to a surface of its own, drawn where the app put the
 * view (AndroidView). The built-in controls, artwork and subtitle view aren't there; apps that draw their own (as TV
 * apps mostly do) look as they do on a TV.
 */
@UnstableApi
class PlayerView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr), ShowsVideo {
    private val surface = couchside.media.Surfaces.create()
    override val videoOutput: VideoOutput = couchside.media.Surfaces.output(surface)
    override val videoFit: Fit get() = when (resizeMode) {
        AspectRatioFrameLayout.RESIZE_MODE_FILL -> Fit.Fill
        AspectRatioFrameLayout.RESIZE_MODE_ZOOM -> Fit.Zoom
        else -> Fit.Fit
    }

    var player: Player? = null
        set(value) {
            if (field === value) return
            field?.clearVideoSurface(surface)
            if (field != null) videoOutput.clear()
            field = value
            value?.setVideoSurface(surface)
        }

    var useController: Boolean = true
    var resizeMode: Int = AspectRatioFrameLayout.RESIZE_MODE_FIT
    var controllerAutoShow: Boolean = true
    var controllerHideOnTouch: Boolean = true
    var controllerShowTimeoutMs: Int = 5000
    var useArtwork: Boolean = true
    var defaultArtwork: Drawable? = null
    val subtitleView: SubtitleView? = null
    val videoSurfaceView: View? = null
    val overlayFrameLayout: android.widget.FrameLayout? = null

    fun setShutterBackgroundColor(color: Int) {}
    fun setKeepContentOnPlayerReset(keep: Boolean) {}
    fun setShowBuffering(mode: Int) {}
    fun setShowSubtitleButton(show: Boolean) {}
    fun setShowNextButton(show: Boolean) {}
    fun setShowPreviousButton(show: Boolean) {}
    fun setShowFastForwardButton(show: Boolean) {}
    fun setShowRewindButton(show: Boolean) {}
    fun setShowShuffleButton(show: Boolean) {}
    fun setRepeatToggleModes(modes: Int) {}
    fun setErrorMessageProvider(provider: Any?) {}
    fun setCustomErrorMessage(message: CharSequence?) {}
    fun setAspectRatioListener(listener: AspectRatioFrameLayout.AspectRatioListener?) {}
    fun setControllerVisibilityListener(listener: Any?) {}
    fun setFullscreenButtonClickListener(listener: Any?) {}
    fun showController() {}
    fun hideController() {}
    fun isControllerFullyVisible(): Boolean = false
    fun setArtworkDisplayMode(mode: Int) {}
    fun setImageDisplayMode(mode: Int) {}
    fun onResume() {}
    fun onPause() {}

    companion object {
        const val SHOW_BUFFERING_NEVER = 0
        const val SHOW_BUFFERING_WHEN_PLAYING = 1
        const val SHOW_BUFFERING_ALWAYS = 2
        const val ARTWORK_DISPLAY_MODE_OFF = 0
        const val ARTWORK_DISPLAY_MODE_FIT = 1
        const val ARTWORK_DISPLAY_MODE_FILL = 2
        @JvmStatic fun switchTargetView(player: Player, oldPlayerView: PlayerView?, newPlayerView: PlayerView?) {
            oldPlayerView?.player = null
            newPlayerView?.player = player
        }
    }
}
