package android.graphics;

public class Point {
    public int x, y;
    public Point() {}
    public Point(int x, int y) { this.x = x; this.y = y; }
    public Point(Point p) { this.x = p.x; this.y = p.y; }
    public void set(int x, int y) { this.x = x; this.y = y; }
    public final void offset(int dx, int dy) { x += dx; y += dy; }
    @Override public boolean equals(Object o) { return o instanceof Point p && p.x == x && p.y == y; }
    @Override public int hashCode() { return 31 * x + y; }
    @Override public String toString() { return "Point(" + x + ", " + y + ")"; }
}
