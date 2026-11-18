package android.graphics;

public class Paint {
    public static final int ANTI_ALIAS_FLAG = 0x01, FILTER_BITMAP_FLAG = 0x02, DITHER_FLAG = 0x04, UNDERLINE_TEXT_FLAG = 0x08,
        STRIKE_THRU_TEXT_FLAG = 0x10, FAKE_BOLD_TEXT_FLAG = 0x20, LINEAR_TEXT_FLAG = 0x40, SUBPIXEL_TEXT_FLAG = 0x80, EMBEDDED_BITMAP_TEXT_FLAG = 0x400;

    public enum Style { FILL, STROKE, FILL_AND_STROKE }
    public enum Cap { BUTT, ROUND, SQUARE }
    public enum Join { MITER, ROUND, BEVEL }
    public enum Align { LEFT, CENTER, RIGHT }

    public static class FontMetrics { public float top, ascent, descent, bottom, leading; }
    public static class FontMetricsInt { public int top, ascent, descent, bottom, leading; }

    private int flags;
    private int color = Color.BLACK;
    private Style style = Style.FILL;
    private float strokeWidth, strokeMiter = 4f;
    private Cap cap = Cap.BUTT;
    private Join join = Join.MITER;
    private Shader shader;
    private MaskFilter maskFilter;
    private ColorFilter colorFilter;
    private PathEffect pathEffect;
    private Xfermode xfermode;
    private BlendMode blendMode;
    private Typeface typeface;
    private float textSize = 12f, textScaleX = 1f, textSkewX, letterSpacing;
    private Align align = Align.LEFT;
    private float shadowRadius, shadowDx, shadowDy;
    private int shadowColor;

    public Paint() { this(0); }
    public Paint(int flags) { this.flags = flags; }
    public Paint(Paint paint) { set(paint); }

    public void set(Paint p) {
        flags = p.flags; color = p.color; style = p.style; strokeWidth = p.strokeWidth; strokeMiter = p.strokeMiter; cap = p.cap; join = p.join;
        shader = p.shader; maskFilter = p.maskFilter; colorFilter = p.colorFilter; pathEffect = p.pathEffect; xfermode = p.xfermode; blendMode = p.blendMode;
        typeface = p.typeface; textSize = p.textSize; textScaleX = p.textScaleX; textSkewX = p.textSkewX; letterSpacing = p.letterSpacing; align = p.align;
        shadowRadius = p.shadowRadius; shadowDx = p.shadowDx; shadowDy = p.shadowDy; shadowColor = p.shadowColor;
    }
    public void reset() { set(new Paint()); }

    public int getFlags() { return flags; }
    public void setFlags(int flags) { this.flags = flags; }
    private void flag(int bit, boolean on) { flags = on ? flags | bit : flags & ~bit; }
    public final boolean isAntiAlias() { return (flags & ANTI_ALIAS_FLAG) != 0; }
    public void setAntiAlias(boolean aa) { flag(ANTI_ALIAS_FLAG, aa); }
    public final boolean isDither() { return (flags & DITHER_FLAG) != 0; }
    public void setDither(boolean dither) { flag(DITHER_FLAG, dither); }
    public final boolean isFilterBitmap() { return (flags & FILTER_BITMAP_FLAG) != 0; }
    public void setFilterBitmap(boolean filter) { flag(FILTER_BITMAP_FLAG, filter); }
    public final boolean isFakeBoldText() { return (flags & FAKE_BOLD_TEXT_FLAG) != 0; }
    public void setFakeBoldText(boolean fake) { flag(FAKE_BOLD_TEXT_FLAG, fake); }
    public final boolean isSubpixelText() { return (flags & SUBPIXEL_TEXT_FLAG) != 0; }
    public void setSubpixelText(boolean subpixel) { flag(SUBPIXEL_TEXT_FLAG, subpixel); }
    public final boolean isLinearText() { return (flags & LINEAR_TEXT_FLAG) != 0; }
    public void setLinearText(boolean linear) { flag(LINEAR_TEXT_FLAG, linear); }
    public final boolean isUnderlineText() { return (flags & UNDERLINE_TEXT_FLAG) != 0; }
    public void setUnderlineText(boolean underline) { flag(UNDERLINE_TEXT_FLAG, underline); }
    public final boolean isStrikeThruText() { return (flags & STRIKE_THRU_TEXT_FLAG) != 0; }
    public void setStrikeThruText(boolean strike) { flag(STRIKE_THRU_TEXT_FLAG, strike); }

    public Style getStyle() { return style; }
    public void setStyle(Style style) { this.style = style; }
    public int getColor() { return color; }
    public void setColor(int color) { this.color = color; }
    public int getAlpha() { return color >>> 24; }
    public void setAlpha(int a) { color = (color & 0x00FFFFFF) | (a << 24); }
    public void setARGB(int a, int r, int g, int b) { color = Color.argb(a, r, g, b); }
    public float getStrokeWidth() { return strokeWidth; }
    public void setStrokeWidth(float width) { strokeWidth = width; }
    public float getStrokeMiter() { return strokeMiter; }
    public void setStrokeMiter(float miter) { strokeMiter = miter; }
    public Cap getStrokeCap() { return cap; }
    public void setStrokeCap(Cap cap) { this.cap = cap; }
    public Join getStrokeJoin() { return join; }
    public void setStrokeJoin(Join join) { this.join = join; }
    public Shader getShader() { return shader; }
    public Shader setShader(Shader shader) { this.shader = shader; return shader; }
    public ColorFilter getColorFilter() { return colorFilter; }
    public ColorFilter setColorFilter(ColorFilter filter) { colorFilter = filter; return filter; }
    public Xfermode getXfermode() { return xfermode; }
    public Xfermode setXfermode(Xfermode xfermode) { this.xfermode = xfermode; this.blendMode = null; return xfermode; }
    public BlendMode getBlendMode() { return blendMode; }
    public void setBlendMode(BlendMode mode) { blendMode = mode; xfermode = null; }
    public PathEffect getPathEffect() { return pathEffect; }
    public PathEffect setPathEffect(PathEffect effect) { pathEffect = effect; return effect; }
    public MaskFilter getMaskFilter() { return maskFilter; }
    public MaskFilter setMaskFilter(MaskFilter filter) { maskFilter = filter; return filter; }
    public Typeface getTypeface() { return typeface; }
    public Typeface setTypeface(Typeface typeface) { this.typeface = typeface; return typeface; }
    public void setShadowLayer(float radius, float dx, float dy, int shadowColor) { shadowRadius = radius; shadowDx = dx; shadowDy = dy; this.shadowColor = shadowColor; }
    public void clearShadowLayer() { shadowRadius = 0f; }
    public float getShadowLayerRadius() { return shadowRadius; }
    public Align getTextAlign() { return align; }
    public void setTextAlign(Align align) { this.align = align; }
    public float getTextSize() { return textSize; }
    public void setTextSize(float textSize) { this.textSize = textSize; }
    public float getTextScaleX() { return textScaleX; }
    public void setTextScaleX(float scaleX) { textScaleX = scaleX; }
    public float getTextSkewX() { return textSkewX; }
    public void setTextSkewX(float skewX) { textSkewX = skewX; }
    public float getLetterSpacing() { return letterSpacing; }
    public void setLetterSpacing(float letterSpacing) { this.letterSpacing = letterSpacing; }

    // ---- Text ----

    org.jetbrains.skia.Font font() { return Sk.font(typeface, textSize, textScaleX, textSkewX, isFakeBoldText()); }

    public float ascent() { return font().getMetrics().getAscent(); }
    public float descent() { return font().getMetrics().getDescent(); }
    public float getFontMetrics(FontMetrics metrics) {
        org.jetbrains.skia.FontMetrics m = font().getMetrics();
        if (metrics != null) { metrics.top = m.getTop(); metrics.ascent = m.getAscent(); metrics.descent = m.getDescent(); metrics.bottom = m.getBottom(); metrics.leading = m.getLeading(); }
        return m.getDescent() - m.getAscent() + m.getLeading();
    }
    public FontMetrics getFontMetrics() { FontMetrics m = new FontMetrics(); getFontMetrics(m); return m; }
    public int getFontMetricsInt(FontMetricsInt fmi) {
        org.jetbrains.skia.FontMetrics m = font().getMetrics();
        if (fmi != null) { fmi.top = (int) Math.floor(m.getTop()); fmi.ascent = Math.round(m.getAscent()); fmi.descent = Math.round(m.getDescent()); fmi.bottom = (int) Math.ceil(m.getBottom()); fmi.leading = Math.round(m.getLeading()); }
        return Math.round(m.getDescent()) - Math.round(m.getAscent()) + Math.round(m.getLeading());
    }
    public FontMetricsInt getFontMetricsInt() { FontMetricsInt m = new FontMetricsInt(); getFontMetricsInt(m); return m; }
    public float getFontSpacing() { return getFontMetrics(null); }

    public float measureText(String text) { return Sk.textWidth(this, text); }
    public float measureText(String text, int start, int end) { return Sk.textWidth(this, text.substring(start, end)); }
    public float measureText(CharSequence text, int start, int end) { return Sk.textWidth(this, text.subSequence(start, end).toString()); }
    public float measureText(char[] text, int index, int count) { return Sk.textWidth(this, new String(text, index, count)); }
    public void getTextBounds(String text, int start, int end, Rect bounds) { Sk.textBounds(this, text.substring(start, end), bounds); }
    public void getTextBounds(CharSequence text, int start, int end, Rect bounds) { Sk.textBounds(this, text.subSequence(start, end).toString(), bounds); }
    public void getTextBounds(char[] text, int index, int count, Rect bounds) { Sk.textBounds(this, new String(text, index, count), bounds); }
    public int getTextWidths(String text, float[] widths) {
        for (int i = 0; i < text.length(); i++) widths[i] = Sk.textWidth(this, text.substring(i, i + 1));
        return text.length();
    }
    public int breakText(String text, boolean measureForwards, float maxWidth, float[] measuredWidth) {
        int n = 0;
        float w = 0f;
        while (n < text.length()) {
            float next = Sk.textWidth(this, text.substring(0, n + 1));
            if (next > maxWidth) break;
            w = next; n++;
        }
        if (measuredWidth != null) measuredWidth[0] = w;
        return n;
    }
    public void getTextPath(String text, int start, int end, float x, float y, Path path) { Sk.textPath(this, text.substring(start, end), x, y, path); }

    // ---- To Skia ----

    /** The Skia paint for a shape or a picture; `image` says a bitmap is being drawn, where only FILTER_BITMAP_FLAG picks the sampling. */
    org.jetbrains.skia.Paint skia() {
        org.jetbrains.skia.Paint p = new org.jetbrains.skia.Paint();
        p.setAntiAlias(isAntiAlias());
        p.setDither(isDither());
        p.setColor(color);
        p.setMode(style == Style.FILL ? org.jetbrains.skia.PaintMode.FILL : style == Style.STROKE ? org.jetbrains.skia.PaintMode.STROKE : org.jetbrains.skia.PaintMode.STROKE_AND_FILL);
        p.setStrokeWidth(strokeWidth);
        p.setStrokeMiter(strokeMiter);
        p.setStrokeCap(cap == Cap.ROUND ? org.jetbrains.skia.PaintStrokeCap.ROUND : cap == Cap.SQUARE ? org.jetbrains.skia.PaintStrokeCap.SQUARE : org.jetbrains.skia.PaintStrokeCap.BUTT);
        p.setStrokeJoin(join == Join.ROUND ? org.jetbrains.skia.PaintStrokeJoin.ROUND : join == Join.BEVEL ? org.jetbrains.skia.PaintStrokeJoin.BEVEL : org.jetbrains.skia.PaintStrokeJoin.MITER);
        if (shader != null) p.setShader(shader.skia());
        if (maskFilter != null) p.setMaskFilter(maskFilter.skia);
        if (colorFilter != null) p.setColorFilter(colorFilter.skia);
        if (pathEffect != null) p.setPathEffect(pathEffect.skia);
        if (blendMode != null) p.setBlendMode(blendMode.skia());
        else if (xfermode != null) p.setBlendMode(xfermode.mode);
        if (shadowRadius > 0f) p.setImageFilter(Sk.dropShadow(shadowDx, shadowDy, shadowRadius, shadowColor));
        return p;
    }
}
