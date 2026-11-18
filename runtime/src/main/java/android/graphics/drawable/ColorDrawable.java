package android.graphics.drawable;

import android.graphics.Canvas;
import android.graphics.ColorFilter;

public class ColorDrawable extends Drawable {
    private int color;
    public ColorDrawable() {}
    public ColorDrawable(int color) { this.color = color; }
    public int getColor() { return color; }
    public void setColor(int color) { this.color = color; }
    @Override public void draw(Canvas canvas) {
        if ((color >>> 24) == 0) return;
        android.graphics.Paint p = new android.graphics.Paint();
        p.setColor(color);
        canvas.drawRect(new android.graphics.RectF(getBounds()), p);
    }
    @Override public void setAlpha(int alpha) { color = (color & 0x00FFFFFF) | (alpha << 24); }
    @Override public int getAlpha() { return color >>> 24; }
    @Override public void setColorFilter(ColorFilter colorFilter) {}
    @Override public int getOpacity() { return (color >>> 24) == 255 ? -1 : (color >>> 24) == 0 ? -2 : -3; }
}
