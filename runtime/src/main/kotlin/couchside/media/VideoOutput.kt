package couchside.media

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlin.math.roundToInt
import androidx.compose.ui.layout.onSizeChanged
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.Image
import org.jetbrains.skia.ImageInfo

/**
 * Where a Surface's pictures go (android.view.Surface): a decoder posts each one for the time the player wants it on
 * screen, and [VideoOutputView] draws the latest. Pictures are reused once they're off screen.
 */
class VideoOutput {
    @Volatile private var shown: FfDecoder.Picture? = null
    private var image: Image? = null
    private var imageOf: FfDecoder.Picture? = null
    private val spare = ConcurrentLinkedQueue<FfDecoder.Picture>()
    internal var tick by mutableLongStateOf(0L)

    /** The size it's drawn at on screen, in pixels: decoders scale pictures down to it. */
    @Volatile var viewWidth = 1920
    @Volatile var viewHeight = 1080

    /** A picture buffer to decode into (one that's been shown and replaced), or null to make a new one. */
    fun spare(): FfDecoder.Picture? = spare.poll()

    /** Shows [picture] at [atNanos] (System.nanoTime), or now. */
    fun show(picture: FfDecoder.Picture, atNanos: Long = 0) {
        val wait = atNanos - System.nanoTime()
        if (wait > 1_000_000) clock.schedule({ swap(picture) }, wait, TimeUnit.NANOSECONDS) else swap(picture)
    }

    /** A picture the player decided not to show. */
    fun drop(picture: FfDecoder.Picture) { recycle(picture) }

    /** Nothing on screen (the player let go of this surface). */
    fun clear() {
        shown = null
        tick++
    }

    @Volatile var shownCount = 0L
        private set
    @Volatile var drawnCount = 0L
        internal set

    private fun swap(picture: FfDecoder.Picture) {
        shownCount++
        val before = shown
        shown = picture
        if (before != null && before !== picture && before !== imageOf) recycle(before)
        tick++
    }

    private fun recycle(picture: FfDecoder.Picture) { if (spare.size < 4) spare.add(picture) }

    private var bitmap: ImageBitmap? = null
    private var bitmapOf: Image? = null

    internal fun bitmap(image: Image): ImageBitmap {
        if (image !== bitmapOf) { bitmap = image.toComposeImageBitmap(); bitmapOf = image }
        return bitmap!!
    }

    internal fun current(): Image? {
        val picture = shown ?: return null
        if (picture !== imageOf) {
            image?.close()
            val old = imageOf
            image = Image.makeRaster(ImageInfo(picture.width, picture.height, ColorType.BGRA_8888, ColorAlphaType.OPAQUE), picture.pixels, picture.width * 4)
            imageOf = picture
            if (old != null && old !== shown) recycle(old)
        }
        return image
    }

    private companion object {
        val clock = Executors.newSingleThreadScheduledExecutor { r -> Thread(r, "video-clock").apply { isDaemon = true; priority = Thread.MAX_PRIORITY } }
    }
}

/** How a picture fits its view, as PlayerView's resize modes have it. */
enum class Fit { Fit, Fill, Zoom }

/** Draws [output]'s latest picture, letterboxed to keep its shape (or filling, or zoomed). */
@Composable
fun VideoOutputView(output: VideoOutput, modifier: Modifier = Modifier, fit: Fit = Fit.Fit) {
    Canvas(modifier.onSizeChanged { output.viewWidth = it.width; output.viewHeight = it.height }) {
        output.tick
        val image = output.current() ?: return@Canvas
        output.drawnCount++
        val w = image.width.toFloat()
        val h = image.height.toFloat()
        val scale = when (fit) {
            Fit.Fit -> minOf(size.width / w, size.height / h)
            Fit.Zoom -> maxOf(size.width / w, size.height / h)
            Fit.Fill -> 0f
        }
        val dw = if (fit == Fit.Fill) size.width else w * scale
        val dh = if (fit == Fit.Fill) size.height else h * scale
        drawImage(
            output.bitmap(image),
            srcOffset = IntOffset.Zero,
            srcSize = IntSize(image.width, image.height),
            dstOffset = IntOffset(((size.width - dw) / 2).roundToInt(), ((size.height - dh) / 2).roundToInt()),
            dstSize = IntSize(dw.roundToInt(), dh.roundToInt()),
            filterQuality = FilterQuality.Medium,
        )
    }
}
