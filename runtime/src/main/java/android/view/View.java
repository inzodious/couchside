package android.view;

import android.content.Context;

/** A stand-in for Android's View: a Compose app draws with Compose, so views are only handles the app holds on to. */
public class View {
    public static final int VISIBLE = 0, INVISIBLE = 4, GONE = 8, NO_ID = -1;
    public static final int SYSTEM_UI_FLAG_VISIBLE = 0, SYSTEM_UI_FLAG_LOW_PROFILE = 1, SYSTEM_UI_FLAG_HIDE_NAVIGATION = 2, SYSTEM_UI_FLAG_FULLSCREEN = 4,
        SYSTEM_UI_FLAG_LAYOUT_STABLE = 0x100, SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION = 0x200, SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN = 0x400, SYSTEM_UI_FLAG_IMMERSIVE = 0x800,
        SYSTEM_UI_FLAG_IMMERSIVE_STICKY = 0x1000;
    public static final int LAYER_TYPE_NONE = 0, LAYER_TYPE_SOFTWARE = 1, LAYER_TYPE_HARDWARE = 2, FOCUS_DOWN = 130, FOCUS_UP = 33, FOCUS_LEFT = 17, FOCUS_RIGHT = 66,
        IMPORTANT_FOR_ACCESSIBILITY_NO = 2, IMPORTANT_FOR_ACCESSIBILITY_YES = 1, OVER_SCROLL_NEVER = 2, LAYOUT_DIRECTION_LTR = 0;

    public interface OnClickListener { void onClick(View v); }
    public interface OnKeyListener { boolean onKey(View v, int keyCode, KeyEvent event); }
    public interface OnFocusChangeListener { void onFocusChange(View v, boolean hasFocus); }
    public interface OnLayoutChangeListener { void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom); }
    public interface OnAttachStateChangeListener { void onViewAttachedToWindow(View v); void onViewDetachedFromWindow(View v); }
    public interface OnSystemUiVisibilityChangeListener { void onSystemUiVisibilityChange(int visibility); }

    private final Context context;
    private int visibility = VISIBLE, id = NO_ID;
    private Object tag;
    private boolean keepScreenOn;
    private float alpha = 1f, translationX, translationY, scaleX = 1f, scaleY = 1f;
    private ViewGroup.LayoutParams layoutParams;
    ViewGroup parent;

    public static final android.util.Property<View, Float> ALPHA = null, TRANSLATION_X = null, TRANSLATION_Y = null, SCALE_X = null, SCALE_Y = null, ROTATION = null, X = null, Y = null;

    public View(Context context) { this.context = context; }
    public View(Context context, android.util.AttributeSet attrs) { this(context); }
    public View(Context context, android.util.AttributeSet attrs, int defStyleAttr) { this(context); }

    public final Context getContext() { return context; }
    public android.content.res.Resources getResources() { return context.getResources(); }
    public View getRootView() { return this; }
    public ViewParent getParent() { return parent; }
    public ViewPropertyAnimator animate() { return null; }
    public float getTranslationX() { return translationX; }
    public void setTranslationX(float v) { translationX = v; }
    public float getTranslationY() { return translationY; }
    public void setTranslationY(float v) { translationY = v; }
    public float getScaleX() { return scaleX; }
    public void setScaleX(float v) { scaleX = v; }
    public float getScaleY() { return scaleY; }
    public void setScaleY(float v) { scaleY = v; }
    public void setPivotX(float v) {}
    public void setPivotY(float v) {}
    public void setRotation(float v) {}
    public void setElevation(float v) {}
    public void setPadding(int left, int top, int right, int bottom) {}
    public void setClipToOutline(boolean clip) {}
    public void setWillNotDraw(boolean willNotDraw) {}
    public void bringToFront() { if (parent != null) parent.bringChildToFront(this); }
    public ViewGroup.LayoutParams getLayoutParams() { return layoutParams; }
    public void setLayoutParams(ViewGroup.LayoutParams params) { layoutParams = params; }
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {}
    protected void onDraw(android.graphics.Canvas canvas) {}
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {}
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {}
    protected void onAttachedToWindow() {}
    protected void onDetachedFromWindow() {}
    protected void onVisibilityChanged(View changedView, int visibility) {}
    public void onWindowFocusChanged(boolean hasWindowFocus) {}
    public android.os.Handler getHandler() { return new android.os.Handler(android.os.Looper.getMainLooper()); }
    public Display getDisplay() { return couchside.shim.Device.display(); }
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public Object getTag() { return tag; }
    public void setTag(Object tag) { this.tag = tag; }
    public int getVisibility() { return visibility; }
    public void setVisibility(int visibility) { this.visibility = visibility; }
    public boolean isShown() { return visibility == VISIBLE; }
    public float getAlpha() { return alpha; }
    public void setAlpha(float alpha) { this.alpha = alpha; }
    public final int getWidth() { return couchside.shim.ScreenSize.width; }
    public final int getHeight() { return 1080; }
    public final int getLeft() { return 0; }
    public final int getTop() { return 0; }
    public final int getMeasuredWidth() { return couchside.shim.ScreenSize.width; }
    public final int getMeasuredHeight() { return 1080; }
    public boolean isAttachedToWindow() { return true; }
    public boolean isLaidOut() { return true; }
    public boolean hasWindowFocus() { return true; }
    public boolean isHardwareAccelerated() { return true; }
    public boolean isInEditMode() { return false; }
    public boolean getKeepScreenOn() { return keepScreenOn; }
    public void setKeepScreenOn(boolean keepScreenOn) { this.keepScreenOn = keepScreenOn; couchside.shim.Device.keepScreenOn(keepScreenOn); }
    public void setSystemUiVisibility(int visibility) {}
    public int getSystemUiVisibility() { return 0; }
    public void setOnSystemUiVisibilityChangeListener(OnSystemUiVisibilityChangeListener l) {}
    public void setLayerType(int layerType, android.graphics.Paint paint) {}
    public void setBackgroundColor(int color) {}
    public void setBackground(android.graphics.drawable.Drawable background) {}
    public void setFocusable(boolean focusable) {}
    public void setFocusableInTouchMode(boolean focusableInTouchMode) {}
    public final boolean requestFocus() { return true; }
    public boolean hasFocus() { return true; }
    public boolean isFocused() { return true; }
    public void clearFocus() {}
    public void setOnClickListener(OnClickListener l) {}
    public void setOnKeyListener(OnKeyListener l) {}
    private OnFocusChangeListener focusListener;
    public void setOnFocusChangeListener(OnFocusChangeListener l) { focusListener = l; }
    public OnFocusChangeListener getOnFocusChangeListener() { return focusListener; }
    public static class AccessibilityDelegate {
        public void onInitializeAccessibilityNodeInfo(View host, android.view.accessibility.AccessibilityNodeInfo info) {}
        public void onInitializeAccessibilityEvent(View host, android.view.accessibility.AccessibilityEvent event) {}
        public void sendAccessibilityEvent(View host, int eventType) {}
        public boolean performAccessibilityAction(View host, int action, android.os.Bundle args) { return false; }
    }
    public void setAccessibilityDelegate(AccessibilityDelegate delegate) {}
    private boolean enabled = true;
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public void addOnLayoutChangeListener(OnLayoutChangeListener listener) {}
    public void removeOnLayoutChangeListener(OnLayoutChangeListener listener) {}
    public void addOnAttachStateChangeListener(OnAttachStateChangeListener listener) {}
    public void removeOnAttachStateChangeListener(OnAttachStateChangeListener listener) {}
    public void setContentDescription(CharSequence contentDescription) {}
    public void setImportantForAccessibility(int mode) {}
    public void announceForAccessibility(CharSequence text) { couchside.shim.Device.announce(text); }
    public void sendAccessibilityEvent(int eventType) {}
    public boolean performHapticFeedback(int feedbackConstant) { return false; }
    public void playSoundEffect(int soundConstant) {}
    public void invalidate() {}
    public void postInvalidate() {}
    public void postInvalidateOnAnimation() {}
    public void requestLayout() {}
    public boolean post(Runnable action) { java.awt.EventQueue.invokeLater(action); return true; }
    public boolean postDelayed(Runnable action, long delayMillis) { return getHandler().postDelayed(action, delayMillis); }
    public void postOnAnimation(Runnable action) { java.awt.EventQueue.invokeLater(action); }
    public boolean removeCallbacks(Runnable action) { return true; }
    public void draw(android.graphics.Canvas canvas) { couchside.shim.Device.activities().drawScreen(canvas); }
    public void getLocationOnScreen(int[] outLocation) { outLocation[0] = 0; outLocation[1] = 0; }
    public void getLocationInWindow(int[] outLocation) { outLocation[0] = 0; outLocation[1] = 0; }
    public void getWindowVisibleDisplayFrame(android.graphics.Rect outRect) { outRect.set(0, 0, couchside.shim.ScreenSize.width, couchside.shim.ScreenSize.height); }
    public <T extends View> T findViewById(int id) { return null; }
    public WindowInsets getRootWindowInsets() { return null; }
    public android.os.IBinder getWindowToken() { return null; }
    public ViewTreeObserver getViewTreeObserver() { return null; }
    public boolean dispatchKeyEvent(KeyEvent event) { return false; }
    public void setRenderEffect(android.graphics.RenderEffect renderEffect) {}
}
