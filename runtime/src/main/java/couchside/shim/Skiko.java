package couchside.shim;

/** Compose for Desktop's own accessors, reached by their class names where the shim declares the Android spelling over them. */
public final class Skiko {
    private Skiko() {}

    public static org.jetbrains.skia.Canvas canvas(androidx.compose.ui.graphics.Canvas canvas) {
        return androidx.compose.ui.graphics.SkiaBackedCanvas_skikoKt.getNativeCanvas(canvas);
    }
}
