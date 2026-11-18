// The Android-only accessors of androidx.compose.ui.graphics, answered with the shim's android.graphics classes.
package androidx.compose.ui.graphics

import couchside.shim.Skiko
import java.util.WeakHashMap

private val canvases = WeakHashMap<org.jetbrains.skia.Canvas, android.graphics.Canvas>()

/** Android's canvas over the Skia canvas Compose is drawing to (this shadows Compose for Desktop's own `nativeCanvas`). */
val Canvas.nativeCanvas: android.graphics.Canvas
    get() {
        val skia = Skiko.canvas(this)
        return synchronized(canvases) { canvases.getOrPut(skia) { android.graphics.Canvas(skia) } }
    }

fun android.graphics.Bitmap.asImageBitmap(): ImageBitmap = skia.asComposeImageBitmap()

fun ImageBitmap.asAndroidBitmap(): android.graphics.Bitmap = android.graphics.Bitmap.of(asSkiaBitmap())

fun Path.asAndroidPath(): android.graphics.Path = android.graphics.Path.of(asSkiaPath())

fun android.graphics.Path.asComposePath(): Path = skia().asComposePath()

fun android.graphics.Shader.asComposeShader(): Shader = requireNotNull(skiaShader()).asComposeShader()

fun android.graphics.Rect.toComposeRect(): androidx.compose.ui.geometry.Rect =
    androidx.compose.ui.geometry.Rect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat())

fun android.graphics.RectF.toComposeRect(): androidx.compose.ui.geometry.Rect = androidx.compose.ui.geometry.Rect(left, top, right, bottom)

fun androidx.compose.ui.geometry.Rect.toAndroidRectF(): android.graphics.RectF = android.graphics.RectF(left, top, right, bottom)
