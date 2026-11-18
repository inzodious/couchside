package android.graphics;

/** Android's Canvas over a Skia canvas: the same engine underneath, so each call maps straight across. */
public class Canvas {
    public static final int ALL_SAVE_FLAG = 0x1F;

    public org.jetbrains.skia.Canvas skia;
    private Bitmap target;
    private final Paint plain = new Paint();

    public Canvas() {}
    public Canvas(Bitmap bitmap) { setBitmap(bitmap); }
    /** Over a canvas Compose is drawing to, for `nativeCanvas`. */
    public Canvas(org.jetbrains.skia.Canvas skia) { this.skia = skia; }

    public void setBitmap(Bitmap bitmap) { target = bitmap; skia = bitmap == null ? null : Sk.canvas(bitmap.skia); }
    public boolean isHardwareAccelerated() { return target == null; }
    public int getWidth() { return target != null ? target.getWidth() : couchside.shim.ScreenSize.width; }
    public int getHeight() { return target != null ? target.getHeight() : 1080; }
    public int getDensity() { return 320; }
    public void setDensity(int density) {}

    private org.jetbrains.skia.Paint p(Paint paint) { return (paint == null ? plain : paint).skia(); }

    // ---- State ----

    public int save() { return skia.save(); }
    public int saveLayer(RectF bounds, Paint paint) { return skia.saveLayer(bounds == null ? null : bounds.skia(), paint == null ? null : paint.skia()); }
    public int saveLayer(RectF bounds, Paint paint, int saveFlags) { return saveLayer(bounds, paint); }
    public int saveLayer(float left, float top, float right, float bottom, Paint paint) { return saveLayer(new RectF(left, top, right, bottom), paint); }
    public int saveLayer(float left, float top, float right, float bottom, Paint paint, int saveFlags) { return saveLayer(new RectF(left, top, right, bottom), paint); }
    public int saveLayerAlpha(RectF bounds, int alpha) { Paint p = new Paint(); p.setAlpha(alpha); return saveLayer(bounds, p); }
    public int saveLayerAlpha(float left, float top, float right, float bottom, int alpha) { return saveLayerAlpha(new RectF(left, top, right, bottom), alpha); }
    public void restore() { skia.restore(); }
    public int getSaveCount() { return skia.getSaveCount(); }
    public void restoreToCount(int saveCount) { skia.restoreToCount(saveCount); }

    public void translate(float dx, float dy) { skia.translate(dx, dy); }
    public void scale(float sx, float sy) { skia.scale(sx, sy); }
    public final void scale(float sx, float sy, float px, float py) { skia.translate(px, py); skia.scale(sx, sy); skia.translate(-px, -py); }
    public void rotate(float degrees) { skia.rotate(degrees); }
    public final void rotate(float degrees, float px, float py) { skia.rotate(degrees, px, py); }
    public void skew(float sx, float sy) { skia.skew(sx, sy); }
    public void concat(Matrix matrix) { if (matrix != null) Sk.concat(skia, matrix.m); }
    public void setMatrix(Matrix matrix) { if (matrix == null) skia.resetMatrix(); else Sk.setMatrix(skia, matrix.m); }
    public void getMatrix(Matrix ctm) { ctm.setValues(Sk.matrix(skia)); }
    public final Matrix getMatrix() { Matrix m = new Matrix(); getMatrix(m); return m; }

    public boolean clipRect(RectF rect) { skia.clipRect(rect.skia(), org.jetbrains.skia.ClipMode.INTERSECT, false); return true; }
    public boolean clipRect(Rect rect) { skia.clipRect(rect.skia(), org.jetbrains.skia.ClipMode.INTERSECT, false); return true; }
    public boolean clipRect(float left, float top, float right, float bottom) { return clipRect(new RectF(left, top, right, bottom)); }
    public boolean clipRect(int left, int top, int right, int bottom) { return clipRect(new RectF(left, top, right, bottom)); }
    public boolean clipOutRect(RectF rect) { skia.clipRect(rect.skia(), org.jetbrains.skia.ClipMode.DIFFERENCE, false); return true; }
    public boolean clipOutRect(Rect rect) { skia.clipRect(rect.skia(), org.jetbrains.skia.ClipMode.DIFFERENCE, false); return true; }
    public boolean clipOutRect(float left, float top, float right, float bottom) { return clipOutRect(new RectF(left, top, right, bottom)); }
    public boolean clipOutRect(int left, int top, int right, int bottom) { return clipOutRect(new RectF(left, top, right, bottom)); }
    public boolean clipPath(Path path) { skia.clipPath(path.skia(), org.jetbrains.skia.ClipMode.INTERSECT, true); return true; }
    public boolean clipOutPath(Path path) { skia.clipPath(path.skia(), org.jetbrains.skia.ClipMode.DIFFERENCE, true); return true; }
    public boolean getClipBounds(Rect bounds) { Rect r = Sk.clipBounds(skia); bounds.set(r); return !r.isEmpty(); }
    public final Rect getClipBounds() { return Sk.clipBounds(skia); }
    public boolean quickReject(RectF rect) { return false; }
    public boolean quickReject(float left, float top, float right, float bottom) { return false; }

    // ---- Fills ----

    public void drawColor(int color) { drawColor(color, PorterDuff.Mode.SRC_OVER); }
    public void drawColor(int color, PorterDuff.Mode mode) {
        org.jetbrains.skia.Paint p = new org.jetbrains.skia.Paint();
        p.setColor(color); p.setBlendMode(mode.skia());
        skia.drawPaint(p);
    }
    public void drawColor(int color, BlendMode mode) {
        org.jetbrains.skia.Paint p = new org.jetbrains.skia.Paint();
        p.setColor(color); p.setBlendMode(mode.skia());
        skia.drawPaint(p);
    }
    public void drawARGB(int a, int r, int g, int b) { drawColor(Color.argb(a, r, g, b)); }
    public void drawRGB(int r, int g, int b) { drawColor(Color.rgb(r, g, b)); }
    public void drawPaint(Paint paint) { skia.drawPaint(p(paint)); }

    // ---- Shapes ----

    public void drawPoint(float x, float y, Paint paint) { skia.drawPoints(new float[] {x, y}, p(paint)); }
    public void drawPoints(float[] pts, Paint paint) { skia.drawPoints(pts, p(paint)); }
    public void drawPoints(float[] pts, int offset, int count, Paint paint) { skia.drawPoints(java.util.Arrays.copyOfRange(pts, offset, offset + count), p(paint)); }
    public void drawLine(float startX, float startY, float stopX, float stopY, Paint paint) { skia.drawLine(startX, startY, stopX, stopY, p(paint)); }
    public void drawLines(float[] pts, Paint paint) { skia.drawLines(pts, p(paint)); }
    public void drawLines(float[] pts, int offset, int count, Paint paint) { skia.drawLines(java.util.Arrays.copyOfRange(pts, offset, offset + count), p(paint)); }
    public void drawRect(RectF rect, Paint paint) { skia.drawRect(rect.skia(), p(paint)); }
    public void drawRect(Rect r, Paint paint) { skia.drawRect(r.skia(), p(paint)); }
    public void drawRect(float left, float top, float right, float bottom, Paint paint) { skia.drawRect(new org.jetbrains.skia.Rect(left, top, right, bottom), p(paint)); }
    public void drawOval(RectF oval, Paint paint) { skia.drawOval(oval.skia(), p(paint)); }
    public void drawOval(float left, float top, float right, float bottom, Paint paint) { skia.drawOval(new org.jetbrains.skia.Rect(left, top, right, bottom), p(paint)); }
    public void drawCircle(float cx, float cy, float radius, Paint paint) { skia.drawCircle(cx, cy, radius, p(paint)); }
    public void drawArc(RectF oval, float startAngle, float sweepAngle, boolean useCenter, Paint paint) { drawArc(oval.left, oval.top, oval.right, oval.bottom, startAngle, sweepAngle, useCenter, paint); }
    public void drawArc(float left, float top, float right, float bottom, float startAngle, float sweepAngle, boolean useCenter, Paint paint) {
        skia.drawArc(left, top, right, bottom, startAngle, sweepAngle, useCenter, p(paint));
    }
    public void drawRoundRect(RectF rect, float rx, float ry, Paint paint) { drawRoundRect(rect.left, rect.top, rect.right, rect.bottom, rx, ry, paint); }
    public void drawRoundRect(float left, float top, float right, float bottom, float rx, float ry, Paint paint) {
        skia.drawRRect(left, top, right, bottom, new float[] {rx, ry}, p(paint));
    }
    public void drawPath(Path path, Paint paint) { skia.drawPath(path.skia(), p(paint)); }

    // ---- Pictures ----

    private org.jetbrains.skia.Paint image(Paint paint) {
        org.jetbrains.skia.Paint p = p(paint);
        // A picture is always filled, whatever style the paint was last used with.
        p.setMode(org.jetbrains.skia.PaintMode.FILL);
        return p;
    }
    private static boolean smooth(Paint paint) { return paint != null && paint.isFilterBitmap(); }

    public void drawBitmap(Bitmap bitmap, float left, float top, Paint paint) {
        Sk.drawImage(skia, bitmap.image(), 0, 0, bitmap.getWidth(), bitmap.getHeight(), left, top, left + bitmap.getWidth(), top + bitmap.getHeight(), smooth(paint), image(paint));
    }
    public void drawBitmap(Bitmap bitmap, Rect src, RectF dst, Paint paint) {
        float l = src == null ? 0 : src.left, t = src == null ? 0 : src.top, r = src == null ? bitmap.getWidth() : src.right, b = src == null ? bitmap.getHeight() : src.bottom;
        Sk.drawImage(skia, bitmap.image(), l, t, r, b, dst.left, dst.top, dst.right, dst.bottom, smooth(paint), image(paint));
    }
    public void drawBitmap(Bitmap bitmap, Rect src, Rect dst, Paint paint) { drawBitmap(bitmap, src, new RectF(dst), paint); }
    public void drawBitmap(Bitmap bitmap, Matrix matrix, Paint paint) {
        skia.save();
        concat(matrix);
        drawBitmap(bitmap, 0f, 0f, paint);
        skia.restore();
    }

    // ---- Text ----

    public void drawText(String text, float x, float y, Paint paint) { Sk.drawText(skia, text, x, y, paint, paint.skia()); }
    public void drawText(String text, int start, int end, float x, float y, Paint paint) { drawText(text.substring(start, end), x, y, paint); }
    public void drawText(CharSequence text, int start, int end, float x, float y, Paint paint) { drawText(text.subSequence(start, end).toString(), x, y, paint); }
    public void drawText(char[] text, int index, int count, float x, float y, Paint paint) { drawText(new String(text, index, count), x, y, paint); }
}
