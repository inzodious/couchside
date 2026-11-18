package android.graphics;

public class ColorMatrixColorFilter extends ColorFilter {
    public ColorMatrixColorFilter(ColorMatrix matrix) { this(matrix.getArray()); }
    public ColorMatrixColorFilter(float[] array) {
        // Android's translate column is 0..255; Skia's is 0..1.
        float[] a = array.clone();
        a[4] /= 255f; a[9] /= 255f; a[14] /= 255f; a[19] /= 255f;
        skia = Sk.colorMatrix(a);
    }
}
