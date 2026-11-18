package android.graphics.drawable;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;

public class BitmapDrawable extends Drawable {
    private final Bitmap bitmap;
    private final Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.ANTI_ALIAS_FLAG);

    public BitmapDrawable(android.content.res.Resources res, Bitmap bitmap) { this.bitmap = bitmap; }
    public BitmapDrawable(Bitmap bitmap) { this.bitmap = bitmap; }

    public final Bitmap getBitmap() { return bitmap; }
    public final Paint getPaint() { return paint; }
    @Override public void draw(Canvas canvas) { if (bitmap != null) canvas.drawBitmap(bitmap, null, new android.graphics.RectF(getBounds()), paint); }
    @Override public void setAlpha(int alpha) { paint.setAlpha(alpha); }
    @Override public int getAlpha() { return paint.getAlpha(); }
    @Override public void setColorFilter(ColorFilter colorFilter) { paint.setColorFilter(colorFilter); }
    @Override public int getOpacity() { return -3; }
    @Override public int getIntrinsicWidth() { return bitmap == null ? -1 : bitmap.getWidth(); }
    @Override public int getIntrinsicHeight() { return bitmap == null ? -1 : bitmap.getHeight(); }
    public void setAntiAlias(boolean aa) { paint.setAntiAlias(aa); }
}
