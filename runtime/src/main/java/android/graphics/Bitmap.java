package android.graphics;

public final class Bitmap {
    public enum Config { ALPHA_8, RGB_565, ARGB_4444, ARGB_8888, RGBA_F16, HARDWARE, RGBA_1010102 }
    public enum CompressFormat { JPEG, PNG, WEBP, WEBP_LOSSY, WEBP_LOSSLESS }
    public static final int DENSITY_NONE = 0;

    public final org.jetbrains.skia.Bitmap skia;
    private org.jetbrains.skia.Image image;
    private int imageGeneration = -1;
    private boolean recycled;
    private int density = 320;

    Bitmap(org.jetbrains.skia.Bitmap skia) { this.skia = skia; }

    /** The one wrapper for a Skia bitmap, so the picture made of it for drawing is made once. */
    public static Bitmap of(org.jetbrains.skia.Bitmap skia) { return Sk.wrap(skia); }

    public static Bitmap createBitmap(int width, int height, Config config) { return Sk.wrap(Sk.blank(width, height)); }
    public static Bitmap createBitmap(android.util.DisplayMetrics display, int width, int height, Config config) { return createBitmap(width, height, config); }
    public static Bitmap createBitmap(int width, int height, Config config, boolean hasAlpha) { return createBitmap(width, height, config); }
    public static Bitmap createBitmap(int[] colors, int width, int height, Config config) { return createBitmap(colors, 0, width, width, height, config); }
    public static Bitmap createBitmap(int[] colors, int offset, int stride, int width, int height, Config config) {
        Bitmap b = createBitmap(width, height, config);
        b.setPixels(colors, offset, stride, 0, 0, width, height);
        return b;
    }
    public static Bitmap createBitmap(Bitmap src) { return createBitmap(src, 0, 0, src.getWidth(), src.getHeight()); }
    public static Bitmap createBitmap(Bitmap src, int x, int y, int width, int height) { return createBitmap(src, x, y, width, height, null, false); }
    public static Bitmap createBitmap(Bitmap src, int x, int y, int width, int height, Matrix m, boolean filter) {
        RectF dst = new RectF(0, 0, width, height);
        if (m != null) m.mapRect(dst);
        Bitmap out = createBitmap(Math.max(1, Math.round(dst.width())), Math.max(1, Math.round(dst.height())), Config.ARGB_8888);
        Canvas c = new Canvas(out);
        c.translate(-dst.left, -dst.top);
        if (m != null) c.concat(m);
        Paint p = new Paint(filter ? Paint.FILTER_BITMAP_FLAG | Paint.ANTI_ALIAS_FLAG : 0);
        c.drawBitmap(src, new Rect(x, y, x + width, y + height), new RectF(0, 0, width, height), p);
        return out;
    }
    public static Bitmap createScaledBitmap(Bitmap src, int dstWidth, int dstHeight, boolean filter) {
        Bitmap out = createBitmap(dstWidth, dstHeight, Config.ARGB_8888);
        new Canvas(out).drawBitmap(src, null, new RectF(0, 0, dstWidth, dstHeight), new Paint(filter ? Paint.FILTER_BITMAP_FLAG : 0));
        return out;
    }

    /** The picture Skia draws for this bitmap, remade only after its pixels change. */
    org.jetbrains.skia.Image image() {
        int generation = skia.getGenerationId();
        if (image == null || generation != imageGeneration) {
            image = org.jetbrains.skia.Image.Companion.makeFromBitmap(skia);
            imageGeneration = generation;
        }
        return image;
    }

    public int getWidth() { return skia.getWidth(); }
    public int getHeight() { return skia.getHeight(); }
    public Config getConfig() { return Config.ARGB_8888; }
    public int getByteCount() { return getWidth() * getHeight() * 4; }
    public int getAllocationByteCount() { return getByteCount(); }
    public int getRowBytes() { return getWidth() * 4; }
    public boolean hasAlpha() { return !skia.isOpaque(); }
    public void setHasAlpha(boolean hasAlpha) {}
    public boolean isMutable() { return !skia.isImmutable(); }
    public boolean isRecycled() { return recycled; }
    public void recycle() { recycled = true; }
    public void prepareToDraw() {}
    public int getDensity() { return density; }
    public void setDensity(int density) { this.density = density; }
    public int getGenerationId() { return skia.getGenerationId(); }

    public Bitmap copy(Config config, boolean isMutable) {
        Bitmap out = createBitmap(getWidth(), getHeight(), Config.ARGB_8888);
        Paint p = new Paint();
        p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
        new Canvas(out).drawBitmap(this, 0f, 0f, p);
        return out;
    }
    public void eraseColor(int c) { skia.erase(c); }
    public int getPixel(int x, int y) { return skia.getColor(x, y); }
    public void setPixel(int x, int y, int color) { setPixels(new int[] {color}, 0, 1, x, y, 1, 1); }
    public void getPixels(int[] pixels, int offset, int stride, int x, int y, int width, int height) { Sk.readPixels(skia, pixels, offset, stride, x, y, width, height); }
    public void setPixels(int[] pixels, int offset, int stride, int x, int y, int width, int height) { Sk.writePixels(skia, pixels, offset, stride, x, y, width, height); }

    public boolean compress(CompressFormat format, int quality, java.io.OutputStream stream) {
        byte[] bytes = Sk.encode(image(), format.ordinal(), quality);
        if (bytes == null) return false;
        try { stream.write(bytes); return true; } catch (java.io.IOException e) { return false; }
    }
}
