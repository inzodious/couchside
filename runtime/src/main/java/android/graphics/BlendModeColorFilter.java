package android.graphics;

public final class BlendModeColorFilter extends ColorFilter {
    private final int color; private final BlendMode mode;
    public BlendModeColorFilter(int color, BlendMode mode) {
        this.color = color; this.mode = mode;
        skia = org.jetbrains.skia.ColorFilter.Companion.makeBlend(color, mode.skia());
    }
    public int getColor() { return color; }
    public BlendMode getMode() { return mode; }
}
