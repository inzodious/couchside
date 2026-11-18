package android.graphics.drawable;

import android.graphics.Canvas;
import android.graphics.ColorFilter;

/** An app icon in two layers, each 108 dp with the middle 72 dp showing, like Android 8's. */
public class AdaptiveIconDrawable extends Drawable {
    private final Drawable background, foreground, monochrome;
    public AdaptiveIconDrawable(Drawable backgroundDrawable, Drawable foregroundDrawable) { this(backgroundDrawable, foregroundDrawable, null); }
    public AdaptiveIconDrawable(Drawable backgroundDrawable, Drawable foregroundDrawable, Drawable monochromeDrawable) {
        background = backgroundDrawable; foreground = foregroundDrawable; monochrome = monochromeDrawable;
    }
    public static float getExtraInsetFraction() { return 0.25f; }
    public Drawable getBackground() { return background; }
    public Drawable getForeground() { return foreground; }
    public Drawable getMonochrome() { return monochrome; }
    public android.graphics.Path getIconMask() {
        android.graphics.Path p = new android.graphics.Path();
        p.addRoundRect(new android.graphics.RectF(getBounds()), getBounds().width() * 0.2f, getBounds().height() * 0.2f, android.graphics.Path.Direction.CW);
        return p;
    }
    @Override public void draw(Canvas canvas) {
        android.graphics.Rect b = getBounds();
        int dx = Math.round(b.width() * 0.25f), dy = Math.round(b.height() * 0.25f);
        canvas.save();
        canvas.clipPath(getIconMask());
        for (Drawable layer : new Drawable[] {background, foreground}) {
            if (layer == null) continue;
            layer.setBounds(b.left - dx, b.top - dy, b.right + dx, b.bottom + dy);
            layer.draw(canvas);
        }
        canvas.restore();
    }
    @Override public void setAlpha(int alpha) {}
    @Override public void setColorFilter(ColorFilter colorFilter) {}
    @Override public int getOpacity() { return -3; }
    @Override public int getIntrinsicWidth() { return 216; }
    @Override public int getIntrinsicHeight() { return 216; }
}
