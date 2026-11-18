package android.graphics;

/** A 3x3 matrix, row-major like Android's: [scaleX skewX transX; skewY scaleY transY; persp0 persp1 persp2]. */
public class Matrix {
    public static final int MSCALE_X = 0, MSKEW_X = 1, MTRANS_X = 2, MSKEW_Y = 3, MSCALE_Y = 4, MTRANS_Y = 5, MPERSP_0 = 6, MPERSP_1 = 7, MPERSP_2 = 8;
    public enum ScaleToFit { FILL, START, CENTER, END }

    final float[] m = {1, 0, 0, 0, 1, 0, 0, 0, 1};

    public Matrix() {}
    public Matrix(Matrix src) { if (src != null) set(src); }

    public float[] values() { return m; }
    public boolean isIdentity() { return m[0] == 1 && m[1] == 0 && m[2] == 0 && m[3] == 0 && m[4] == 1 && m[5] == 0 && m[6] == 0 && m[7] == 0 && m[8] == 1; }
    public boolean isAffine() { return m[6] == 0 && m[7] == 0 && m[8] == 1; }
    public void set(Matrix src) { if (src == null) reset(); else System.arraycopy(src.m, 0, m, 0, 9); }
    public void reset() { setValues(new float[] {1, 0, 0, 0, 1, 0, 0, 0, 1}); }
    public void getValues(float[] values) { System.arraycopy(m, 0, values, 0, 9); }
    public void setValues(float[] values) { System.arraycopy(values, 0, m, 0, 9); }

    private static float[] mul(float[] a, float[] b) {
        float[] r = new float[9];
        for (int i = 0; i < 3; i++) for (int j = 0; j < 3; j++) r[i * 3 + j] = a[i * 3] * b[j] + a[i * 3 + 1] * b[3 + j] + a[i * 3 + 2] * b[6 + j];
        return r;
    }
    private static float[] translate(float dx, float dy) { return new float[] {1, 0, dx, 0, 1, dy, 0, 0, 1}; }
    private static float[] scale(float sx, float sy, float px, float py) { return new float[] {sx, 0, px - sx * px, 0, sy, py - sy * py, 0, 0, 1}; }
    private static float[] rotate(float degrees, float px, float py) {
        double a = Math.toRadians(degrees);
        float s = (float) Math.sin(a), c = (float) Math.cos(a);
        return new float[] {c, -s, px - c * px + s * py, s, c, py - s * px - c * py, 0, 0, 1};
    }
    private static float[] skew(float kx, float ky, float px, float py) { return new float[] {1, kx, -kx * py, ky, 1, -ky * px, 0, 0, 1}; }

    public void setTranslate(float dx, float dy) { setValues(translate(dx, dy)); }
    public void setScale(float sx, float sy, float px, float py) { setValues(scale(sx, sy, px, py)); }
    public void setScale(float sx, float sy) { setValues(scale(sx, sy, 0, 0)); }
    public void setRotate(float degrees, float px, float py) { setValues(rotate(degrees, px, py)); }
    public void setRotate(float degrees) { setValues(rotate(degrees, 0, 0)); }
    public void setSkew(float kx, float ky, float px, float py) { setValues(skew(kx, ky, px, py)); }
    public void setSkew(float kx, float ky) { setValues(skew(kx, ky, 0, 0)); }
    public boolean setConcat(Matrix a, Matrix b) { setValues(mul(a.m, b.m)); return true; }

    private boolean pre(float[] other) { setValues(mul(m, other)); return true; }
    private boolean post(float[] other) { setValues(mul(other, m)); return true; }
    public boolean preTranslate(float dx, float dy) { return pre(translate(dx, dy)); }
    public boolean preScale(float sx, float sy, float px, float py) { return pre(scale(sx, sy, px, py)); }
    public boolean preScale(float sx, float sy) { return pre(scale(sx, sy, 0, 0)); }
    public boolean preRotate(float degrees, float px, float py) { return pre(rotate(degrees, px, py)); }
    public boolean preRotate(float degrees) { return pre(rotate(degrees, 0, 0)); }
    public boolean preSkew(float kx, float ky, float px, float py) { return pre(skew(kx, ky, px, py)); }
    public boolean preSkew(float kx, float ky) { return pre(skew(kx, ky, 0, 0)); }
    public boolean preConcat(Matrix other) { return pre(other.m); }
    public boolean postTranslate(float dx, float dy) { return post(translate(dx, dy)); }
    public boolean postScale(float sx, float sy, float px, float py) { return post(scale(sx, sy, px, py)); }
    public boolean postScale(float sx, float sy) { return post(scale(sx, sy, 0, 0)); }
    public boolean postRotate(float degrees, float px, float py) { return post(rotate(degrees, px, py)); }
    public boolean postRotate(float degrees) { return post(rotate(degrees, 0, 0)); }
    public boolean postSkew(float kx, float ky, float px, float py) { return post(skew(kx, ky, px, py)); }
    public boolean postSkew(float kx, float ky) { return post(skew(kx, ky, 0, 0)); }
    public boolean postConcat(Matrix other) { return post(other.m); }

    public boolean setRectToRect(RectF src, RectF dst, ScaleToFit stf) {
        if (src.isEmpty()) { reset(); return false; }
        float sx = dst.width() / src.width(), sy = dst.height() / src.height();
        float tx, ty;
        if (stf == ScaleToFit.FILL) { tx = dst.left - src.left * sx; ty = dst.top - src.top * sy; }
        else {
            float s = Math.min(sx, sy);
            float fx = stf == ScaleToFit.START ? 0f : stf == ScaleToFit.CENTER ? 0.5f : 1f;
            tx = dst.left - src.left * s + (dst.width() - src.width() * s) * fx;
            ty = dst.top - src.top * s + (dst.height() - src.height() * s) * fx;
            sx = sy = s;
        }
        setValues(new float[] {sx, 0, tx, 0, sy, ty, 0, 0, 1});
        return true;
    }

    /** Maps `pointCount` source points onto the destination points; four points give a perspective warp. */
    public boolean setPolyToPoly(float[] src, int srcIndex, float[] dst, int dstIndex, int pointCount) {
        if (pointCount == 0) { reset(); return true; }
        if (pointCount == 1) { setTranslate(dst[dstIndex] - src[srcIndex], dst[dstIndex + 1] - src[srcIndex + 1]); return true; }
        int n = pointCount == 4 ? 8 : pointCount == 3 ? 6 : 4;
        double[][] a = new double[n][n + 1];
        for (int i = 0; i < pointCount; i++) {
            double x = src[srcIndex + i * 2], y = src[srcIndex + i * 2 + 1], u = dst[dstIndex + i * 2], v = dst[dstIndex + i * 2 + 1];
            if (pointCount == 4) {
                a[i * 2] = new double[] {x, y, 1, 0, 0, 0, -u * x, -u * y, u};
                a[i * 2 + 1] = new double[] {0, 0, 0, x, y, 1, -v * x, -v * y, v};
            } else if (pointCount == 3) {
                a[i * 2] = new double[] {x, y, 1, 0, 0, 0, u};
                a[i * 2 + 1] = new double[] {0, 0, 0, x, y, 1, v};
            } else {
                // Two points: rotation + uniform scale + translation (a, b, tx, ty with [a -b; b a]).
                a[i * 2] = new double[] {x, -y, 1, 0, u};
                a[i * 2 + 1] = new double[] {y, x, 0, 1, v};
            }
        }
        for (int col = 0; col < n; col++) {
            int pivot = col;
            for (int r = col + 1; r < n; r++) if (Math.abs(a[r][col]) > Math.abs(a[pivot][col])) pivot = r;
            if (Math.abs(a[pivot][col]) < 1e-12) return false;
            double[] t = a[col]; a[col] = a[pivot]; a[pivot] = t;
            for (int r = 0; r < n; r++) {
                if (r == col) continue;
                double f = a[r][col] / a[col][col];
                for (int c = col; c <= n; c++) a[r][c] -= f * a[col][c];
            }
        }
        double[] s = new double[n];
        for (int i = 0; i < n; i++) s[i] = a[i][n] / a[i][i];
        if (pointCount == 4) setValues(new float[] {(float) s[0], (float) s[1], (float) s[2], (float) s[3], (float) s[4], (float) s[5], (float) s[6], (float) s[7], 1});
        else if (pointCount == 3) setValues(new float[] {(float) s[0], (float) s[1], (float) s[2], (float) s[3], (float) s[4], (float) s[5], 0, 0, 1});
        else setValues(new float[] {(float) s[0], (float) -s[1], (float) s[2], (float) s[1], (float) s[0], (float) s[3], 0, 0, 1});
        return true;
    }

    public boolean invert(Matrix inverse) {
        float a = m[0], b = m[1], c = m[2], d = m[3], e = m[4], f = m[5], g = m[6], h = m[7], i = m[8];
        float A = e * i - f * h, B = -(d * i - f * g), C = d * h - e * g;
        float det = a * A + b * B + c * C;
        if (Math.abs(det) < 1e-12f) return false;
        float inv = 1f / det;
        if (inverse != null) inverse.setValues(new float[] {
            A * inv, -(b * i - c * h) * inv, (b * f - c * e) * inv,
            B * inv, (a * i - c * g) * inv, -(a * f - c * d) * inv,
            C * inv, -(a * h - b * g) * inv, (a * e - b * d) * inv});
        return true;
    }

    public void mapPoints(float[] dst, int dstIndex, float[] src, int srcIndex, int pointCount) {
        for (int k = 0; k < pointCount; k++) {
            float x = src[srcIndex + k * 2], y = src[srcIndex + k * 2 + 1];
            float w = m[6] * x + m[7] * y + m[8];
            if (w == 0f) w = 1f;
            dst[dstIndex + k * 2] = (m[0] * x + m[1] * y + m[2]) / w;
            dst[dstIndex + k * 2 + 1] = (m[3] * x + m[4] * y + m[5]) / w;
        }
    }
    public void mapPoints(float[] dst, float[] src) { mapPoints(dst, 0, src, 0, src.length / 2); }
    public void mapPoints(float[] pts) { mapPoints(pts, 0, pts.clone(), 0, pts.length / 2); }
    public void mapVectors(float[] vecs) {
        for (int k = 0; k < vecs.length / 2; k++) {
            float x = vecs[k * 2], y = vecs[k * 2 + 1];
            vecs[k * 2] = m[0] * x + m[1] * y; vecs[k * 2 + 1] = m[3] * x + m[4] * y;
        }
    }
    public boolean mapRect(RectF dst, RectF src) {
        float[] p = {src.left, src.top, src.right, src.top, src.right, src.bottom, src.left, src.bottom};
        mapPoints(p);
        dst.set(Math.min(Math.min(p[0], p[2]), Math.min(p[4], p[6])), Math.min(Math.min(p[1], p[3]), Math.min(p[5], p[7])),
            Math.max(Math.max(p[0], p[2]), Math.max(p[4], p[6])), Math.max(Math.max(p[1], p[3]), Math.max(p[5], p[7])));
        return m[1] == 0 && m[3] == 0 && isAffine();
    }
    public boolean mapRect(RectF rect) { return mapRect(rect, new RectF(rect)); }
    public float mapRadius(float radius) { return radius * (float) Math.sqrt(Math.abs(m[0] * m[4] - m[1] * m[3])); }

    @Override public boolean equals(Object o) { return o instanceof Matrix x && java.util.Arrays.equals(x.m, m); }
    @Override public int hashCode() { return java.util.Arrays.hashCode(m); }
    @Override public String toString() { return "Matrix" + java.util.Arrays.toString(m); }
}
