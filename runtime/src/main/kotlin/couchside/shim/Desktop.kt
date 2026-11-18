package couchside.shim

import android.graphics.Typeface
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.asComposeShader
import androidx.compose.ui.graphics.skiaPaint
import androidx.compose.ui.graphics.skiaShader
import androidx.compose.ui.text.platform.FontLoadResult
import org.jetbrains.skia.FilterBlurMode
import org.jetbrains.skia.MaskFilter
import org.jetbrains.skia.Matrix33

/** What the lines in patches.txt call: the desktop spelling of something the TV app does through Android's Compose. */
object Desktop {
    /** `paint.asFrameworkPaint().maskFilter = BlurMaskFilter(radius, NORMAL)`. */
    @JvmStatic fun blur(paint: Paint, radius: Float) {
        paint.skiaPaint.maskFilter = MaskFilter.makeBlur(FilterBlurMode.NORMAL, 0.57735f * radius + 0.5f, true)
    }

    /** `shader.setLocalMatrix(Matrix().apply { setScale(sx, sy) })`, as a new shader. */
    @JvmStatic fun scaled(shader: Shader, sx: Float, sy: Float): Shader = shader.skiaShader.makeWithLocalMatrix(Matrix33.makeScale(sx, sy)).asComposeShader()

    /** The typeface behind a font Compose resolved (`fonts.resolve(...).value as? Typeface` on Android). */
    @JvmStatic fun typeface(resolved: Any?): Typeface? = when (resolved) {
        is Typeface -> resolved
        is org.jetbrains.skia.Typeface -> Typeface.of(resolved)
        is FontLoadResult -> Typeface.of(resolved.typeface)
        else -> null
    }
}
