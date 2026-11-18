// tv-material's glow node draws through Android's Paint.setShadowLayer. This is the same node (the library's own
// element makes and updates it) drawing the same glow with Skia directly: the shape, blurred, in the glow's colour.
package androidx.tv.material3

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.skiaPaint
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.invalidateDraw
import org.jetbrains.skia.FilterBlurMode
import org.jetbrains.skia.MaskFilter

internal class SurfaceGlowNode(private var shape: Shape, private var glowBlurRadiusPx: Float, private var color: Color) : Modifier.Node(), DrawModifierNode {
    private var paint: Paint? = null

    fun reactToUpdates(newShape: Shape, newGlowBlurRadiusPx: Float, newColor: Color) {
        shape = newShape
        glowBlurRadiusPx = newGlowBlurRadiusPx
        color = newColor
        paint = null
        invalidateDraw()
    }

    override fun ContentDrawScope.draw() {
        val glow = paint ?: Paint().also { p ->
            // Android's shadow takes its alpha from the shadow colour, unless that's opaque: then from the paint, which is clear here.
            p.color = if (color.alpha >= 1f) color.copy(alpha = 0f) else color
            if (glowBlurRadiusPx > 0f) p.skiaPaint.maskFilter = MaskFilter.makeBlur(FilterBlurMode.NORMAL, 0.57735f * glowBlurRadiusPx + 0.5f, true)
            paint = p
        }
        drawIntoCanvas { canvas ->
            when (val outline = shape.createOutline(size, layoutDirection, this)) {
                is Outline.Rectangle -> canvas.drawRect(outline.rect, glow)
                is Outline.Rounded -> {
                    val r = outline.roundRect
                    canvas.drawRoundRect(0f, 0f, size.width, size.height, r.topLeftCornerRadius.x, r.topLeftCornerRadius.y, glow)
                }
                is Outline.Generic -> canvas.drawPath(outline.path, glow)
            }
        }
        drawContent()
    }
}
