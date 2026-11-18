package android.graphics;

public class Path {
    public enum FillType { WINDING, EVEN_ODD, INVERSE_WINDING, INVERSE_EVEN_ODD }
    public enum Direction { CW, CCW }
    public enum Op { DIFFERENCE, INTERSECT, UNION, XOR, REVERSE_DIFFERENCE }

    org.jetbrains.skia.PathBuilder b = new org.jetbrains.skia.PathBuilder();
    private FillType fill = FillType.WINDING;
    private org.jetbrains.skia.Path made;

    public Path() {}
    public Path(Path src) { if (src != null) set(src); }

    /** A path over a Skia one, for the desktop host. */
    public static Path of(org.jetbrains.skia.Path skia) {
        Path p = new Path();
        p.b = new org.jetbrains.skia.PathBuilder(skia);
        p.fill = fillOf(skia.getFillMode());
        return p;
    }

    private static FillType fillOf(org.jetbrains.skia.PathFillMode m) {
        switch (m) {
            case EVEN_ODD: return FillType.EVEN_ODD;
            case INVERSE_WINDING: return FillType.INVERSE_WINDING;
            case INVERSE_EVEN_ODD: return FillType.INVERSE_EVEN_ODD;
            default: return FillType.WINDING;
        }
    }
    private org.jetbrains.skia.PathFillMode fillMode() {
        switch (fill) {
            case EVEN_ODD: return org.jetbrains.skia.PathFillMode.EVEN_ODD;
            case INVERSE_WINDING: return org.jetbrains.skia.PathFillMode.INVERSE_WINDING;
            case INVERSE_EVEN_ODD: return org.jetbrains.skia.PathFillMode.INVERSE_EVEN_ODD;
            default: return org.jetbrains.skia.PathFillMode.WINDING;
        }
    }
    private static org.jetbrains.skia.PathDirection dir(Direction d) {
        return d == Direction.CCW ? org.jetbrains.skia.PathDirection.COUNTER_CLOCKWISE : org.jetbrains.skia.PathDirection.CLOCKWISE;
    }
    private Path changed() { made = null; return this; }

    public org.jetbrains.skia.Path skia() {
        if (made == null) { b.setFillType(fillMode()); made = b.snapshot(); }
        return made;
    }

    public void reset() { b.reset(); fill = FillType.WINDING; changed(); }
    public void rewind() { b.reset(); changed(); }
    public void set(Path src) { b = new org.jetbrains.skia.PathBuilder(src.skia()); fill = src.fill; changed(); }
    public FillType getFillType() { return fill; }
    public void setFillType(FillType ft) { fill = ft; changed(); }
    public boolean isEmpty() { return skia().isEmpty(); }
    public void computeBounds(RectF bounds, boolean exact) {
        org.jetbrains.skia.Rect r = skia().getBounds();
        bounds.set(r.getLeft(), r.getTop(), r.getRight(), r.getBottom());
    }

    public void moveTo(float x, float y) { b.moveTo(x, y); changed(); }
    public void rMoveTo(float dx, float dy) { b.rMoveTo(dx, dy); changed(); }
    public void lineTo(float x, float y) { b.lineTo(x, y); changed(); }
    public void rLineTo(float dx, float dy) { b.rLineTo(dx, dy); changed(); }
    public void quadTo(float x1, float y1, float x2, float y2) { b.quadTo(x1, y1, x2, y2); changed(); }
    public void rQuadTo(float dx1, float dy1, float dx2, float dy2) { b.rQuadTo(dx1, dy1, dx2, dy2); changed(); }
    public void cubicTo(float x1, float y1, float x2, float y2, float x3, float y3) { b.cubicTo(x1, y1, x2, y2, x3, y3); changed(); }
    public void rCubicTo(float x1, float y1, float x2, float y2, float x3, float y3) { b.rCubicTo(x1, y1, x2, y2, x3, y3); changed(); }
    public void conicTo(float x1, float y1, float x2, float y2, float weight) { b.conicTo(x1, y1, x2, y2, weight); changed(); }
    public void arcTo(RectF oval, float startAngle, float sweepAngle, boolean forceMoveTo) { b.arcTo(oval.skia(), startAngle, sweepAngle, forceMoveTo); changed(); }
    public void arcTo(RectF oval, float startAngle, float sweepAngle) { arcTo(oval, startAngle, sweepAngle, false); }
    public void arcTo(float left, float top, float right, float bottom, float startAngle, float sweepAngle, boolean forceMoveTo) {
        b.arcTo(left, top, right, bottom, startAngle, sweepAngle, forceMoveTo); changed();
    }
    public void close() { b.closePath(); changed(); }

    public void addRect(RectF rect, Direction dir) { addRect(rect.left, rect.top, rect.right, rect.bottom, dir); }
    public void addRect(float left, float top, float right, float bottom, Direction dir) { b.addRect(left, top, right, bottom, dir(dir), 0); changed(); }
    public void addOval(RectF oval, Direction dir) { addOval(oval.left, oval.top, oval.right, oval.bottom, dir); }
    public void addOval(float left, float top, float right, float bottom, Direction dir) { b.addOval(left, top, right, bottom, dir(dir), 1); changed(); }
    public void addCircle(float x, float y, float radius, Direction dir) { b.addCircle(x, y, radius, dir(dir)); changed(); }
    public void addArc(RectF oval, float startAngle, float sweepAngle) { b.addArc(oval.skia(), startAngle, sweepAngle); changed(); }
    public void addArc(float left, float top, float right, float bottom, float startAngle, float sweepAngle) { b.addArc(left, top, right, bottom, startAngle, sweepAngle); changed(); }
    public void addRoundRect(RectF rect, float rx, float ry, Direction dir) { addRoundRect(rect.left, rect.top, rect.right, rect.bottom, rx, ry, dir); }
    public void addRoundRect(float left, float top, float right, float bottom, float rx, float ry, Direction dir) {
        b.addRRect(left, top, right, bottom, new float[] {rx, ry}, dir(dir), 0); changed();
    }
    public void addRoundRect(RectF rect, float[] radii, Direction dir) { addRoundRect(rect.left, rect.top, rect.right, rect.bottom, radii, dir); }
    public void addRoundRect(float left, float top, float right, float bottom, float[] radii, Direction dir) {
        b.addRRect(left, top, right, bottom, radii, dir(dir), 0); changed();
    }
    public void addPath(Path src) { b.addPath(src.skia(), org.jetbrains.skia.PathAddMode.APPEND); changed(); }
    public void addPath(Path src, float dx, float dy) { b.addPath(src.skia(), dx, dy, org.jetbrains.skia.PathAddMode.APPEND); changed(); }
    public void addPath(Path src, Matrix matrix) { Path t = new Path(src); t.transform(matrix); addPath(t); }

    public void offset(float dx, float dy) { b.offset(dx, dy); changed(); }
    public void offset(float dx, float dy, Path dst) { if (dst == null) offset(dx, dy); else { dst.set(this); dst.offset(dx, dy); } }
    public void transform(Matrix matrix) { Sk.transform(b, matrix.m); changed(); }
    public void transform(Matrix matrix, Path dst) { if (dst == null) transform(matrix); else { dst.set(this); dst.transform(matrix); } }
    public void setLastPoint(float dx, float dy) { b.setLastPt(dx, dy); changed(); }
    public boolean op(Path path, Op op) { return op(this, path, op); }
    public boolean op(Path path1, Path path2, Op op) {
        org.jetbrains.skia.Path r = Sk.pathOp(path1.skia(), path2.skia(), op.ordinal());
        if (r == null) return false;
        b = new org.jetbrains.skia.PathBuilder(r); fill = fillOf(r.getFillMode()); changed();
        return true;
    }
}
