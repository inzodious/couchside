package android.graphics;

public class RadialGradient extends Shader {
    public RadialGradient(float centerX, float centerY, float radius, int[] colors, float[] stops, TileMode tile) {
        base = Sk.radial(centerX, centerY, radius, colors, stops, tile);
    }
    public RadialGradient(float centerX, float centerY, float radius, int centerColor, int edgeColor, TileMode tile) {
        this(centerX, centerY, radius, new int[] {centerColor, edgeColor}, null, tile);
    }
}
