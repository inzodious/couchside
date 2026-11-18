package android.graphics;

public class Shader {
    public enum TileMode { CLAMP, REPEAT, MIRROR, DECAL }

    org.jetbrains.skia.Shader base;
    private Matrix local;

    public void setLocalMatrix(Matrix localM) { local = localM == null || localM.isIdentity() ? null : new Matrix(localM); }
    public boolean getLocalMatrix(Matrix localM) {
        if (local == null) { localM.reset(); return false; }
        localM.set(local);
        return true;
    }

    public org.jetbrains.skia.Shader skiaShader() { return skia(); }

    org.jetbrains.skia.Shader skia() { return local == null || base == null ? base : Sk.withLocalMatrix(base, local.m); }
}
