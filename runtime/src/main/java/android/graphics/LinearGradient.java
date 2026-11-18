package android.graphics;

public class LinearGradient extends Shader {
    public LinearGradient(float x0, float y0, float x1, float y1, int[] colors, float[] positions, TileMode tile) {
        base = Sk.linear(x0, y0, x1, y1, colors, positions, tile);
    }
    public LinearGradient(float x0, float y0, float x1, float y1, int color0, int color1, TileMode tile) {
        this(x0, y0, x1, y1, new int[] {color0, color1}, null, tile);
    }
}
