package android.graphics

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.LinearGradientShader
import androidx.compose.ui.graphics.RadialGradientShader
import androidx.compose.ui.graphics.SweepGradientShader
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.skiaShader
import java.util.WeakHashMap
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.Data
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.FilterMipmap
import org.jetbrains.skia.FilterMode
import org.jetbrains.skia.FilterTileMode
import org.jetbrains.skia.Font
import org.jetbrains.skia.FontEdging
import org.jetbrains.skia.FontMgr
import org.jetbrains.skia.FontSlant
import org.jetbrains.skia.FontStyle
import org.jetbrains.skia.IRect
import org.jetbrains.skia.Image
import org.jetbrains.skia.ImageFilter
import org.jetbrains.skia.ImageInfo
import org.jetbrains.skia.Matrix33
import org.jetbrains.skia.MipmapMode
import org.jetbrains.skia.PathBuilder
import org.jetbrains.skia.PathOp
import org.jetbrains.skia.SamplingMode
import org.jetbrains.skia.SurfaceProps
import org.jetbrains.skia.TextLine

/** The Skia calls the Java half of the graphics shim can't spell (value classes, defaults, companions). */
internal object Sk {
    private val wrappers = WeakHashMap<org.jetbrains.skia.Bitmap, Bitmap>()

    @JvmStatic fun wrap(skia: org.jetbrains.skia.Bitmap): Bitmap = synchronized(wrappers) { wrappers.getOrPut(skia) { Bitmap(skia) } }

    @JvmStatic fun blank(width: Int, height: Int): org.jetbrains.skia.Bitmap =
        org.jetbrains.skia.Bitmap().apply { allocPixels(ImageInfo.makeN32Premul(width.coerceAtLeast(1), height.coerceAtLeast(1))); erase(0) }

    @JvmStatic fun canvas(bitmap: org.jetbrains.skia.Bitmap) = org.jetbrains.skia.Canvas(bitmap, SurfaceProps())

    private fun m33(m: FloatArray) = Matrix33(m[0], m[1], m[2], m[3], m[4], m[5], m[6], m[7], m[8])

    @JvmStatic fun concat(canvas: org.jetbrains.skia.Canvas, m: FloatArray) { canvas.concat(m33(m)) }
    @JvmStatic fun setMatrix(canvas: org.jetbrains.skia.Canvas, m: FloatArray) { canvas.setMatrix(m33(m)) }
    @JvmStatic fun matrix(canvas: org.jetbrains.skia.Canvas): FloatArray = canvas.localToDeviceAsMatrix33.mat.copyOf()
    @JvmStatic fun transform(path: PathBuilder, m: FloatArray) { path.transform(m33(m)) }
    @JvmStatic fun withLocalMatrix(shader: org.jetbrains.skia.Shader, m: FloatArray): org.jetbrains.skia.Shader = shader.makeWithLocalMatrix(m33(m))
    @JvmStatic fun colorMatrix(m: FloatArray): org.jetbrains.skia.ColorFilter = org.jetbrains.skia.ColorFilter.makeMatrix(org.jetbrains.skia.ColorMatrix(m.copyOf()))
    @JvmStatic fun pathOp(a: org.jetbrains.skia.Path, b: org.jetbrains.skia.Path, op: Int): org.jetbrains.skia.Path? =
        org.jetbrains.skia.Path.makeCombining(a, b, arrayOf(PathOp.DIFFERENCE, PathOp.INTERSECT, PathOp.UNION, PathOp.XOR, PathOp.REVERSE_DIFFERENCE)[op])

    // Skia doesn't hand back the clip here; callers only use it to skip work, so "everything" is a safe answer.
    @JvmStatic fun clipBounds(canvas: org.jetbrains.skia.Canvas): Rect = Rect(0, 0, 16384, 16384)

    // ---- Shaders ----

    private fun colors(c: IntArray) = c.map { androidx.compose.ui.graphics.Color(it) }
    private fun tile(t: Shader.TileMode?) = when (t) {
        Shader.TileMode.REPEAT -> TileMode.Repeated
        Shader.TileMode.MIRROR -> TileMode.Mirror
        Shader.TileMode.DECAL -> TileMode.Decal
        else -> TileMode.Clamp
    }

    @JvmStatic fun linear(x0: Float, y0: Float, x1: Float, y1: Float, c: IntArray, at: FloatArray?, t: Shader.TileMode?): org.jetbrains.skia.Shader =
        LinearGradientShader(Offset(x0, y0), Offset(x1, y1), colors(c), at?.toList(), tile(t)).skiaShader

    @JvmStatic fun radial(x: Float, y: Float, r: Float, c: IntArray, at: FloatArray?, t: Shader.TileMode?): org.jetbrains.skia.Shader =
        RadialGradientShader(Offset(x, y), r, colors(c), at?.toList(), tile(t)).skiaShader

    @JvmStatic fun sweep(x: Float, y: Float, c: IntArray, at: FloatArray?): org.jetbrains.skia.Shader =
        SweepGradientShader(Offset(x, y), colors(c), at?.toList()).skiaShader

    private fun skTile(t: Shader.TileMode?) = when (t) {
        Shader.TileMode.REPEAT -> FilterTileMode.REPEAT
        Shader.TileMode.MIRROR -> FilterTileMode.MIRROR
        Shader.TileMode.DECAL -> FilterTileMode.DECAL
        else -> FilterTileMode.CLAMP
    }

    @JvmStatic fun bitmapShader(bitmap: Bitmap, x: Shader.TileMode?, y: Shader.TileMode?): org.jetbrains.skia.Shader =
        bitmap.image().makeShader(skTile(x), skTile(y), SamplingMode.LINEAR, null)

    @JvmStatic fun dropShadow(dx: Float, dy: Float, radius: Float, color: Int): ImageFilter {
        val sigma = 0.57735f * radius + 0.5f
        return ImageFilter.makeDropShadow(dx, dy, sigma, sigma, color)
    }

    // ---- Pictures ----

    private val SMOOTH = FilterMipmap(FilterMode.LINEAR, MipmapMode.NONE)
    private val SHARP = FilterMipmap(FilterMode.NEAREST, MipmapMode.NONE)

    @JvmStatic fun drawImage(
        canvas: org.jetbrains.skia.Canvas, image: Image,
        sl: Float, st: Float, sr: Float, sb: Float, dl: Float, dt: Float, dr: Float, db: Float,
        smooth: Boolean, paint: org.jetbrains.skia.Paint,
    ) {
        canvas.drawImageRect(image, org.jetbrains.skia.Rect(sl, st, sr, sb), org.jetbrains.skia.Rect(dl, dt, dr, db), if (smooth) SMOOTH else SHARP, paint, true)
    }

    @JvmStatic fun readPixels(bitmap: org.jetbrains.skia.Bitmap, out: IntArray, offset: Int, stride: Int, x: Int, y: Int, w: Int, h: Int) {
        val info = ImageInfo(w, h, ColorType.BGRA_8888, ColorAlphaType.UNPREMUL)
        val bytes = bitmap.readPixels(info, w * 4, x, y) ?: return
        val ints = java.nio.ByteBuffer.wrap(bytes).order(java.nio.ByteOrder.LITTLE_ENDIAN).asIntBuffer()
        for (row in 0 until h) ints.get(row * w, out, offset + row * stride, w)
    }

    @JvmStatic fun writePixels(bitmap: org.jetbrains.skia.Bitmap, px: IntArray, offset: Int, stride: Int, x: Int, y: Int, w: Int, h: Int) {
        val bytes = ByteArray(w * h * 4)
        val ints = java.nio.ByteBuffer.wrap(bytes).order(java.nio.ByteOrder.LITTLE_ENDIAN).asIntBuffer()
        for (row in 0 until h) ints.put(row * w, px, offset + row * stride, w)
        val source = Image.makeRaster(ImageInfo(w, h, ColorType.BGRA_8888, ColorAlphaType.UNPREMUL), bytes, w * 4)
        val paint = org.jetbrains.skia.Paint().apply { blendMode = org.jetbrains.skia.BlendMode.SRC }
        canvas(bitmap).drawImageRect(source, org.jetbrains.skia.Rect(0f, 0f, w.toFloat(), h.toFloat()), org.jetbrains.skia.Rect(x.toFloat(), y.toFloat(), (x + w).toFloat(), (y + h).toFloat()), SHARP, paint, true)
        bitmap.notifyPixelsChanged()
    }

    @JvmStatic fun encode(image: Image, format: Int, quality: Int): ByteArray? =
        image.encodeToData(when (format) { 0 -> EncodedImageFormat.JPEG; 1 -> EncodedImageFormat.PNG; else -> EncodedImageFormat.WEBP }, quality)?.bytes

    @JvmStatic fun decode(bytes: ByteArray, opts: BitmapFactory.Options?): Bitmap? {
        val image = runCatching { Image.makeFromEncoded(bytes) }.getOrNull() ?: return null
        opts?.outWidth = image.width
        opts?.outHeight = image.height
        if (opts?.inJustDecodeBounds == true) return null
        val sample = (opts?.inSampleSize ?: 1).coerceAtLeast(1)
        val w = (image.width / sample).coerceAtLeast(1)
        val h = (image.height / sample).coerceAtLeast(1)
        val bitmap = blank(w, h)
        canvas(bitmap).drawImageRect(
            image, org.jetbrains.skia.Rect(0f, 0f, image.width.toFloat(), image.height.toFloat()), org.jetbrains.skia.Rect(0f, 0f, w.toFloat(), h.toFloat()),
            if (sample > 1) SMOOTH else SHARP, org.jetbrains.skia.Paint().apply { blendMode = org.jetbrains.skia.BlendMode.SRC }, true,
        )
        bitmap.notifyPixelsChanged()
        if (opts?.inMutable != true) bitmap.setImmutable()
        return wrap(bitmap)
    }

    // ---- Type ----

    // Android's family names, and the closest faces a PC is likely to have (Roboto when it's installed).
    private val FAMILIES = mapOf(
        "sans-serif" to listOf("Roboto", "Noto Sans", "Inter", "DejaVu Sans", "Segoe UI", "Arial"),
        "sans-serif-condensed" to listOf("Roboto Condensed", "Noto Sans Condensed", "DejaVu Sans Condensed", "Arial Narrow", "Roboto", "Noto Sans", "DejaVu Sans", "Arial"),
        "sans-serif-medium" to listOf("Roboto Medium", "Roboto", "Noto Sans", "DejaVu Sans", "Segoe UI", "Arial"),
        "sans-serif-light" to listOf("Roboto Light", "Roboto", "Noto Sans", "DejaVu Sans", "Segoe UI", "Arial"),
        "sans-serif-black" to listOf("Roboto Black", "Roboto", "Noto Sans", "DejaVu Sans", "Arial Black", "Arial"),
        "serif" to listOf("Noto Serif", "DejaVu Serif", "Liberation Serif", "Georgia", "Times New Roman"),
        "monospace" to listOf("Droid Sans Mono", "Roboto Mono", "DejaVu Sans Mono", "Liberation Mono", "Consolas", "Courier New"),
        "serif-monospace" to listOf("Cutive Mono", "Courier Prime", "Liberation Mono", "Courier New"),
        "casual" to listOf("Coming Soon", "Comic Neue", "Comic Sans MS"),
        "cursive" to listOf("Dancing Script", "Segoe Script"),
    )

    private fun match(name: String?, style: FontStyle): org.jetbrains.skia.Typeface {
        val mgr = FontMgr.default
        val names = FAMILIES[name?.lowercase() ?: "sans-serif"] ?: listOfNotNull(name) + FAMILIES.getValue("sans-serif")
        for (n in names) mgr.matchFamilyStyle(n, style)?.let { return it }
        return mgr.legacyMakeTypeface("", style) ?: org.jetbrains.skia.Typeface.makeEmpty()
    }

    @JvmStatic fun family(name: String?, style: Int): Typeface =
        familyWeight(name, if (style and Typeface.BOLD != 0) 700 else 400, style and Typeface.ITALIC != 0)

    @JvmStatic fun familyWeight(name: String?, weight: Int, italic: Boolean): Typeface =
        Typeface(match(name, FontStyle(weight, 5, if (italic) FontSlant.ITALIC else FontSlant.UPRIGHT)), name ?: "sans-serif")

    @JvmStatic fun typefaceFile(path: String): Typeface =
        Typeface(FontMgr.default.makeFromFile(path, 0) ?: error("Font not found: $path"), null)

    @JvmStatic fun typefaceBytes(bytes: ByteArray): Typeface =
        Typeface(FontMgr.default.makeFromData(Data.makeFromBytes(bytes), 0) ?: error("Not a font"), null)

    @JvmStatic fun font(face: Typeface?, size: Float, scaleX: Float, skewX: Float, fakeBold: Boolean): Font =
        Font((face ?: Typeface.DEFAULT).skia, size, scaleX, skewX).apply {
            isEmboldened = fakeBold
            isSubpixel = true
            edging = FontEdging.ANTI_ALIAS
        }

    private fun spacing(paint: Paint) = paint.letterSpacing * paint.textSize

    @JvmStatic fun textWidth(paint: Paint, text: String): Float {
        if (text.isEmpty()) return 0f
        val gap = spacing(paint)
        val line = TextLine.make(text, paint.font())
        return line.width + if (gap != 0f) gap * text.codePointCount(0, text.length) else 0f
    }

    @JvmStatic fun textBounds(paint: Paint, text: String, out: Rect) {
        if (text.isEmpty()) return out.setEmpty()
        val r = paint.font().measureText(text, null)
        val extra = spacing(paint) * text.codePointCount(0, text.length)
        out.set(kotlin.math.floor(r.left).toInt(), kotlin.math.floor(r.top).toInt(), kotlin.math.ceil(r.right + extra).toInt(), kotlin.math.ceil(r.bottom).toInt())
    }

    @JvmStatic fun drawText(canvas: org.jetbrains.skia.Canvas, text: String, x: Float, y: Float, paint: Paint, skia: org.jetbrains.skia.Paint) {
        if (text.isEmpty()) return
        val font = paint.font()
        val gap = spacing(paint)
        val width = textWidth(paint, text)
        var at = when (paint.textAlign) {
            Paint.Align.CENTER -> x - width / 2f
            Paint.Align.RIGHT -> x - width
            else -> x
        }
        if (gap == 0f) {
            canvas.drawTextLine(TextLine.make(text, font), at, y, skia)
            return
        }
        // Android puts half the letter spacing on each side of every glyph.
        at += gap / 2f
        var i = 0
        while (i < text.length) {
            val n = Character.charCount(text.codePointAt(i))
            val line = TextLine.make(text.substring(i, i + n), font)
            canvas.drawTextLine(line, at, y, skia)
            at += line.width + gap
            i += n
        }
    }

    @JvmStatic fun textPath(paint: Paint, text: String, x: Float, y: Float, out: Path) {
        val font = paint.font()
        val glyphs = font.getStringGlyphs(text)
        val widths = font.getWidths(glyphs)
        val builder = PathBuilder()
        var at = x
        glyphs.forEachIndexed { i, g ->
            font.getPath(g)?.let { builder.addPath(it, at, y) }
            at += widths[i]
        }
        out.set(Path.of(builder.detach()))
    }
}
