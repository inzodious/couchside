package android.graphics;

public class RectF {
    public float left, top, right, bottom;

    public RectF() {}
    public RectF(float left, float top, float right, float bottom) { set(left, top, right, bottom); }
    public RectF(RectF r) { if (r != null) set(r.left, r.top, r.right, r.bottom); }
    public RectF(Rect r) { if (r != null) set(r.left, r.top, r.right, r.bottom); }

    public void set(float left, float top, float right, float bottom) { this.left = left; this.top = top; this.right = right; this.bottom = bottom; }
    public void set(RectF r) { set(r.left, r.top, r.right, r.bottom); }
    public void set(Rect r) { set(r.left, r.top, r.right, r.bottom); }
    public void setEmpty() { set(0, 0, 0, 0); }
    public final float width() { return right - left; }
    public final float height() { return bottom - top; }
    public final float centerX() { return (left + right) * 0.5f; }
    public final float centerY() { return (top + bottom) * 0.5f; }
    public final boolean isEmpty() { return left >= right || top >= bottom; }
    public void offset(float dx, float dy) { left += dx; right += dx; top += dy; bottom += dy; }
    public void offsetTo(float x, float y) { right += x - left; bottom += y - top; left = x; top = y; }
    public void inset(float dx, float dy) { left += dx; top += dy; right -= dx; bottom -= dy; }
    public boolean contains(float x, float y) { return left < right && top < bottom && x >= left && x < right && y >= top && y < bottom; }
    public boolean contains(RectF r) { return left < right && top < bottom && left <= r.left && top <= r.top && right >= r.right && bottom >= r.bottom; }
    public boolean intersects(float l, float t, float r, float b) { return left < r && l < right && top < b && t < bottom; }
    public static boolean intersects(RectF a, RectF b) { return a.left < b.right && b.left < a.right && a.top < b.bottom && b.top < a.bottom; }
    public boolean intersect(float l, float t, float r, float b) {
        if (!intersects(l, t, r, b)) return false;
        left = Math.max(left, l); top = Math.max(top, t); right = Math.min(right, r); bottom = Math.min(bottom, b);
        return true;
    }
    public boolean intersect(RectF r) { return intersect(r.left, r.top, r.right, r.bottom); }
    public void union(float l, float t, float r, float b) {
        if (l >= r || t >= b) return;
        if (isEmpty()) { set(l, t, r, b); return; }
        left = Math.min(left, l); top = Math.min(top, t); right = Math.max(right, r); bottom = Math.max(bottom, b);
    }
    public void union(RectF r) { union(r.left, r.top, r.right, r.bottom); }
    public void round(Rect dst) { dst.set(Math.round(left), Math.round(top), Math.round(right), Math.round(bottom)); }
    public void roundOut(Rect dst) { dst.set((int) Math.floor(left), (int) Math.floor(top), (int) Math.ceil(right), (int) Math.ceil(bottom)); }
    public void sort() {
        if (left > right) { float t = left; left = right; right = t; }
        if (top > bottom) { float t = top; top = bottom; bottom = t; }
    }

    org.jetbrains.skia.Rect skia() { return new org.jetbrains.skia.Rect(left, top, right, bottom); }

    @Override public boolean equals(Object o) {
        return o instanceof RectF r && r.left == left && r.top == top && r.right == right && r.bottom == bottom;
    }
    @Override public int hashCode() { return java.util.Arrays.hashCode(new float[] {left, top, right, bottom}); }
    @Override public String toString() { return "RectF(" + left + ", " + top + ", " + right + ", " + bottom + ")"; }
}
