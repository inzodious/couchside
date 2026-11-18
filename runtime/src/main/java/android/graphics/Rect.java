package android.graphics;

public final class Rect {
    public int left, top, right, bottom;

    public Rect() {}
    public Rect(int left, int top, int right, int bottom) { set(left, top, right, bottom); }
    public Rect(Rect r) { if (r != null) set(r.left, r.top, r.right, r.bottom); }

    public void set(int left, int top, int right, int bottom) { this.left = left; this.top = top; this.right = right; this.bottom = bottom; }
    public void set(Rect r) { set(r.left, r.top, r.right, r.bottom); }
    public void setEmpty() { set(0, 0, 0, 0); }
    public int width() { return right - left; }
    public int height() { return bottom - top; }
    public int centerX() { return (left + right) >> 1; }
    public int centerY() { return (top + bottom) >> 1; }
    public float exactCenterX() { return (left + right) * 0.5f; }
    public float exactCenterY() { return (top + bottom) * 0.5f; }
    public boolean isEmpty() { return left >= right || top >= bottom; }
    public void offset(int dx, int dy) { left += dx; right += dx; top += dy; bottom += dy; }
    public void offsetTo(int x, int y) { right += x - left; bottom += y - top; left = x; top = y; }
    public void inset(int dx, int dy) { left += dx; top += dy; right -= dx; bottom -= dy; }
    public boolean contains(int x, int y) { return left < right && top < bottom && x >= left && x < right && y >= top && y < bottom; }
    public boolean contains(Rect r) { return left < right && top < bottom && left <= r.left && top <= r.top && right >= r.right && bottom >= r.bottom; }
    public boolean intersects(int l, int t, int r, int b) { return left < r && l < right && top < b && t < bottom; }
    public static boolean intersects(Rect a, Rect b) { return a.intersects(b.left, b.top, b.right, b.bottom); }
    public boolean intersect(int l, int t, int r, int b) {
        if (!intersects(l, t, r, b)) return false;
        left = Math.max(left, l); top = Math.max(top, t); right = Math.min(right, r); bottom = Math.min(bottom, b);
        return true;
    }
    public boolean intersect(Rect r) { return intersect(r.left, r.top, r.right, r.bottom); }
    public void union(int l, int t, int r, int b) {
        if (l >= r || t >= b) return;
        if (isEmpty()) { set(l, t, r, b); return; }
        left = Math.min(left, l); top = Math.min(top, t); right = Math.max(right, r); bottom = Math.max(bottom, b);
    }
    public void union(Rect r) { union(r.left, r.top, r.right, r.bottom); }

    org.jetbrains.skia.Rect skia() { return new org.jetbrains.skia.Rect(left, top, right, bottom); }

    @Override public boolean equals(Object o) {
        return o instanceof Rect r && r.left == left && r.top == top && r.right == right && r.bottom == bottom;
    }
    @Override public int hashCode() { return 31 * (31 * (31 * left + top) + right) + bottom; }
    @Override public String toString() { return "Rect(" + left + ", " + top + " - " + right + ", " + bottom + ")"; }
}
