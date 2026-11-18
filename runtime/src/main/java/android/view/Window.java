package android.view;

import android.content.Context;

/** The activity's window. On the desktop it's the simulator's one window; only what TV apps ask of it is here. */
public class Window {
    public interface Callback {
        boolean dispatchKeyEvent(KeyEvent event);
        default boolean dispatchKeyShortcutEvent(KeyEvent event) { return false; }
        default boolean dispatchTouchEvent(MotionEvent event) { return false; }
        default boolean dispatchGenericMotionEvent(MotionEvent event) { return false; }
        default void onWindowFocusChanged(boolean hasFocus) {}
        default void onAttachedToWindow() {}
        default void onDetachedFromWindow() {}
    }
    public interface OnFrameMetricsAvailableListener { void onFrameMetricsAvailable(Window window, FrameMetrics frameMetrics, int dropCountSinceLastInvocation); }
    public final void addOnFrameMetricsAvailableListener(OnFrameMetricsAvailableListener listener, android.os.Handler handler) {}
    public final void removeOnFrameMetricsAvailableListener(OnFrameMetricsAvailableListener listener) {}
    public static final int FEATURE_NO_TITLE = 1, FEATURE_ACTIVITY_TRANSITIONS = 13, FEATURE_CONTENT_TRANSITIONS = 12;

    private final Context context;
    private final View decor;
    private View content;
    private Callback callback;
    private final WindowManager.LayoutParams attributes = new WindowManager.LayoutParams();
    private int flags;

    public Window(Context context) { this.context = context; this.decor = new View(context); if (context instanceof Callback c) callback = c; }

    public final Context getContext() { return context; }
    public View getDecorView() { return decor; }
    public View peekDecorView() { return decor; }
    public View getCurrentFocus() { return decor; }
    public void setContentView(View view) { content = view; }
    public View getContentView() { return content; }
    public <T extends View> T findViewById(int id) { return null; }
    public Callback getCallback() { return callback; }
    public void setCallback(Callback callback) { this.callback = callback; }
    public WindowManager getWindowManager() { return (WindowManager) couchside.shim.Device.service(Context.WINDOW_SERVICE); }
    public final WindowManager.LayoutParams getAttributes() { return attributes; }
    public void setAttributes(WindowManager.LayoutParams a) { attributes.preferredDisplayModeId = a.preferredDisplayModeId; attributes.preferredRefreshRate = a.preferredRefreshRate; attributes.screenBrightness = a.screenBrightness; couchside.shim.Device.windowChanged(this); }
    public void addFlags(int flags) { setFlags(flags, flags); }
    public void clearFlags(int flags) { setFlags(0, flags); }
    public void setFlags(int flags, int mask) {
        this.flags = (this.flags & ~mask) | (flags & mask);
        attributes.flags = this.flags;
        if ((mask & WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) != 0) couchside.shim.Device.keepScreenOn((this.flags & WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) != 0);
    }
    public void setBackgroundDrawable(android.graphics.drawable.Drawable drawable) { couchside.shim.Device.windowBackground(this, drawable); }
    public void setBackgroundDrawableResource(int resId) {}
    public void setFormat(int format) {}
    public void setColorMode(int colorMode) {}
    public int getColorMode() { return 0; }
    public void setStatusBarColor(int color) {}
    public void setNavigationBarColor(int color) {}
    public void setDecorFitsSystemWindows(boolean decorFitsSystemWindows) {}
    public void setSoftInputMode(int mode) {}
    public void setLayout(int width, int height) {}
    public void setGravity(int gravity) {}
    public void setDimAmount(float amount) {}
    public void setWindowAnimations(int resId) {}
    public void setSustainedPerformanceMode(boolean enable) {}
    public void setPreferMinimalPostProcessing(boolean isPreferred) {}
    public boolean requestFeature(int featureId) { return true; }
    public WindowInsetsController getInsetsController() { return null; }
    public void setFrameRateBoostOnTouchEnabled(boolean enabled) {}
    public void setFrameRatePowerSavingsBalanced(boolean enabled) {}
}
