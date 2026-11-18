package android.graphics;

public class BlurMaskFilter extends MaskFilter {
    public enum Blur { NORMAL, SOLID, OUTER, INNER }

    public BlurMaskFilter(float radius, Blur style) {
        org.jetbrains.skia.FilterBlurMode mode = style == Blur.SOLID ? org.jetbrains.skia.FilterBlurMode.SOLID
            : style == Blur.OUTER ? org.jetbrains.skia.FilterBlurMode.OUTER
            : style == Blur.INNER ? org.jetbrains.skia.FilterBlurMode.INNER : org.jetbrains.skia.FilterBlurMode.NORMAL;
        // Android's own conversion from a blur radius to Skia's sigma.
        float sigma = radius > 0 ? 0.57735f * radius + 0.5f : 0f;
        skia = org.jetbrains.skia.MaskFilter.Companion.makeBlur(mode, sigma, true);
    }
}
