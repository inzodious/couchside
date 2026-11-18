package android.graphics;

public class LightingColorFilter extends ColorFilter {
    private final int mul, add;
    public LightingColorFilter(int mul, int add) {
        this.mul = mul; this.add = add;
        skia = org.jetbrains.skia.ColorFilter.Companion.makeLighting(mul, add);
    }
    public int getColorMultiply() { return mul; }
    public int getColorAdd() { return add; }
}
