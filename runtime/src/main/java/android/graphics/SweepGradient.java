package android.graphics;

public class SweepGradient extends Shader {
    public SweepGradient(float cx, float cy, int[] colors, float[] positions) { base = Sk.sweep(cx, cy, colors, positions); }
    public SweepGradient(float cx, float cy, int color0, int color1) { this(cx, cy, new int[] {color0, color1}, null); }
}
