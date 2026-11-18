package android.graphics.drawable;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Rect;

public abstract class Drawable {
    public interface Callback {
        void invalidateDrawable(Drawable who);
        void scheduleDrawable(Drawable who, Runnable what, long when);
        void unscheduleDrawable(Drawable who, Runnable what);
    }
    public abstract static class ConstantState {
        public abstract Drawable newDrawable();
        public Drawable newDrawable(android.content.res.Resources res) { return newDrawable(); }
        public abstract int getChangingConfigurations();
    }

    private final Rect bounds = new Rect();
    private Callback callback;
    private int level;
    private boolean visible = true;

    public abstract void draw(Canvas canvas);
    public abstract void setAlpha(int alpha);
    public abstract void setColorFilter(ColorFilter colorFilter);
    public abstract int getOpacity();

    public int getAlpha() { return 255; }
    public void setBounds(int left, int top, int right, int bottom) {
        if (bounds.left != left || bounds.top != top || bounds.right != right || bounds.bottom != bottom) { bounds.set(left, top, right, bottom); onBoundsChange(bounds); }
    }
    public void setBounds(Rect bounds) { setBounds(bounds.left, bounds.top, bounds.right, bounds.bottom); }
    public final Rect getBounds() { return bounds; }
    public final void copyBounds(Rect out) { out.set(bounds); }
    public final Rect copyBounds() { return new Rect(bounds); }
    protected void onBoundsChange(Rect bounds) {}
    public int getIntrinsicWidth() { return -1; }
    public int getIntrinsicHeight() { return -1; }
    public int getMinimumWidth() { return Math.max(0, getIntrinsicWidth()); }
    public int getMinimumHeight() { return Math.max(0, getIntrinsicHeight()); }
    public boolean getPadding(Rect padding) { padding.set(0, 0, 0, 0); return false; }
    public Drawable mutate() { return this; }
    public ConstantState getConstantState() { return null; }
    public void setCallback(Callback cb) { callback = cb; }
    public Callback getCallback() { return callback; }
    public void invalidateSelf() { if (callback != null) callback.invalidateDrawable(this); }
    public void setTint(int tintColor) {}
    public void setTintList(android.content.res.ColorStateList tint) {}
    public void setTintMode(android.graphics.PorterDuff.Mode tintMode) {}
    public void setColorFilter(int color, android.graphics.PorterDuff.Mode mode) { setColorFilter(new android.graphics.PorterDuffColorFilter(color, mode)); }
    public void clearColorFilter() { setColorFilter(null); }
    public void setDither(boolean dither) {}
    public void setFilterBitmap(boolean filter) {}
    public boolean isStateful() { return false; }
    public boolean setState(int[] stateSet) { return false; }
    public int[] getState() { return new int[0]; }
    public Drawable getCurrent() { return this; }
    public final boolean setLevel(int level) { this.level = level; return false; }
    public final int getLevel() { return level; }
    public boolean setVisible(boolean visible, boolean restart) { boolean changed = this.visible != visible; this.visible = visible; return changed; }
    public final boolean isVisible() { return visible; }
    public void setAutoMirrored(boolean mirrored) {}
    public boolean isAutoMirrored() { return false; }
    public int getChangingConfigurations() { return 0; }
    public void jumpToCurrentState() {}
}
