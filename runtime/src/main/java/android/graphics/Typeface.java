package android.graphics;

public class Typeface {
    public static final int NORMAL = 0, BOLD = 1, ITALIC = 2, BOLD_ITALIC = 3;
    public static final Typeface SANS_SERIF = Sk.family("sans-serif", NORMAL);
    public static final Typeface DEFAULT = SANS_SERIF;
    public static final Typeface DEFAULT_BOLD = Sk.family("sans-serif", BOLD);
    public static final Typeface SERIF = Sk.family("serif", NORMAL);
    public static final Typeface MONOSPACE = Sk.family("monospace", NORMAL);

    public final org.jetbrains.skia.Typeface skia;
    final String family;

    Typeface(org.jetbrains.skia.Typeface skia, String family) { this.skia = skia; this.family = family; }

    /** A typeface over a Skia one, for the desktop host (Compose's resolved fonts are Skia typefaces). */
    public static Typeface of(org.jetbrains.skia.Typeface skia) { return skia == null ? null : new Typeface(skia, null); }

    public static Typeface create(String familyName, int style) { return Sk.family(familyName, style); }
    public static Typeface create(Typeface family, int style) {
        if (family == null) return Sk.family(null, style);
        if (family.getStyle() == style) return family;
        return family.family != null ? Sk.family(family.family, style) : family;
    }
    public static Typeface create(Typeface family, int weight, boolean italic) {
        if (family == null || family.family != null) return Sk.familyWeight(family == null ? null : family.family, weight, italic);
        return family;
    }
    public static Typeface defaultFromStyle(int style) { return Sk.family(null, style); }
    public static Typeface createFromBytes(byte[] bytes) { return Sk.typefaceBytes(bytes); }
    public static Typeface createFromFile(java.io.File file) { return Sk.typefaceFile(file.getPath()); }
    public static Typeface createFromFile(String path) { return Sk.typefaceFile(path); }
    public static Typeface createFromAsset(android.content.res.AssetManager mgr, String path) {
        try (java.io.InputStream in = mgr.open(path)) { return Sk.typefaceBytes(in.readAllBytes()); }
        catch (java.io.IOException e) { throw new RuntimeException("Font asset not found " + path, e); }
    }

    public int getStyle() { return (isBold() ? BOLD : 0) | (isItalic() ? ITALIC : 0); }
    public final boolean isBold() { return skia.isBold(); }
    public final boolean isItalic() { return skia.isItalic(); }
    public int getWeight() { return skia.getFontStyle().getWeight(); }

    @Override public boolean equals(Object o) { return o instanceof Typeface t && t.skia.equals(skia); }
    @Override public int hashCode() { return skia.hashCode(); }
}
