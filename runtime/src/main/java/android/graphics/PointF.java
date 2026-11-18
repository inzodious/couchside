package android.graphics;

public class PointF {
    public float x, y;
    public PointF() {}
    public PointF(float x, float y) { this.x = x; this.y = y; }
    public PointF(Point p) { this.x = p.x; this.y = p.y; }
    public final void set(float x, float y) { this.x = x; this.y = y; }
    public final void set(PointF p) { this.x = p.x; this.y = p.y; }
    public final void offset(float dx, float dy) { x += dx; y += dy; }
    public final float length() { return (float) Math.hypot(x, y); }
    @Override public boolean equals(Object o) { return o instanceof PointF p && p.x == x && p.y == y; }
    @Override public int hashCode() { return Float.hashCode(x) * 31 + Float.hashCode(y); }
    @Override public String toString() { return "PointF(" + x + ", " + y + ")"; }
}
