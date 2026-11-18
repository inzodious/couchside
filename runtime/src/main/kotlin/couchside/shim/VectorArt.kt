package couchside.shim

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.drawable.Drawable
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.asComposeCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.VectorGroup
import androidx.compose.ui.graphics.vector.VectorNode
import androidx.compose.ui.graphics.vector.VectorPath
import androidx.compose.ui.graphics.vector.toPath
import androidx.compose.ui.res.loadXmlImageVector
import androidx.compose.ui.unit.Density
import org.xml.sax.InputSource

/** An Android vector drawable (XML) as a Drawable: Compose for Desktop parses it, and its paths are drawn straight to the canvas. */
object VectorArt {
    fun drawable(xml: ByteArray): Drawable {
        val vector = loadXmlImageVector(InputSource(xml.inputStream()), Density(2f))
        return object : Drawable() {
            private var alpha = 255
            override fun getIntrinsicWidth() = (vector.defaultWidth.value * 2f).toInt()
            override fun getIntrinsicHeight() = (vector.defaultHeight.value * 2f).toInt()
            override fun setAlpha(alpha: Int) { this.alpha = alpha }
            override fun setColorFilter(colorFilter: ColorFilter?) {}
            override fun getOpacity() = -3
            override fun draw(canvas: Canvas) {
                val b = bounds
                val to = canvas.skia.asComposeCanvas()
                to.save()
                to.translate(b.left.toFloat(), b.top.toFloat())
                to.scale(b.width() / vector.viewportWidth, b.height() / vector.viewportHeight)
                node(to, vector.root, Size(vector.viewportWidth, vector.viewportHeight), alpha / 255f)
                to.restore()
            }
        }
    }

    private fun node(canvas: androidx.compose.ui.graphics.Canvas, n: VectorNode, size: Size, alpha: Float) {
        when (n) {
            is VectorGroup -> {
                canvas.save()
                canvas.translate(n.translationX + n.pivotX, n.translationY + n.pivotY)
                canvas.rotate(n.rotation)
                canvas.scale(n.scaleX, n.scaleY)
                canvas.translate(-n.pivotX, -n.pivotY)
                if (n.clipPathData.isNotEmpty()) canvas.clipPath(n.clipPathData.toPath())
                for (child in n) node(canvas, child, size, alpha)
                canvas.restore()
            }
            is VectorPath -> {
                val path = n.pathData.toPath().apply { fillType = n.pathFillType }
                n.fill?.let { canvas.drawPath(path, paint(it, size, n.fillAlpha * alpha).apply { style = PaintingStyle.Fill }) }
                n.stroke?.let {
                    canvas.drawPath(path, paint(it, size, n.strokeAlpha * alpha).apply {
                        style = PaintingStyle.Stroke
                        strokeWidth = n.strokeLineWidth
                        strokeCap = n.strokeLineCap
                        strokeJoin = n.strokeLineJoin
                        strokeMiterLimit = n.strokeLineMiter
                    })
                }
            }
        }
    }

    private fun paint(brush: Brush, size: Size, alpha: Float) = Paint().apply {
        isAntiAlias = true
        brush.applyTo(size, this, alpha)
    }
}
