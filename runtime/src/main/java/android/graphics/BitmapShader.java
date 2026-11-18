package android.graphics;

public class BitmapShader extends Shader {
    public BitmapShader(Bitmap bitmap, TileMode tileX, TileMode tileY) { base = Sk.bitmapShader(bitmap, tileX, tileY); }
}
