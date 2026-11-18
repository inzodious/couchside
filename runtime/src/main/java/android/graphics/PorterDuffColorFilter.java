package android.graphics;

public class PorterDuffColorFilter extends ColorFilter {
    public PorterDuffColorFilter(int color, PorterDuff.Mode mode) {
        skia = org.jetbrains.skia.ColorFilter.Companion.makeBlend(color, mode.skia());
    }
}
