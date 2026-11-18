package android.app;

import android.content.ComponentName;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import couchside.shim.Device;

/** An activity the desktop host runs: it owns the window while it's on top (see Activities.kt for the lifecycle). */
public class Activity extends ContextWrapper implements Window.Callback, KeyEvent.Callback {
    public static final int RESULT_CANCELED = 0, RESULT_OK = -1, RESULT_FIRST_USER = 1;

    private Intent intent = new Intent();
    private ComponentName component;
    private final Window window = new Window(this);
    private boolean finishing, destroyed, resumed;
    private CharSequence title = "";
    private int resultCode;
    private Intent resultData;

    public Activity() { super(null); }

    /** Called by the host right after it constructs the activity. */
    public final void attach(Intent intent, ComponentName component) { this.intent = intent; this.component = component; }

    public Application getApplication() { return Device.application(); }
    public Intent getIntent() { return intent; }
    public void setIntent(Intent newIntent) { intent = newIntent; }
    public ComponentName getComponentName() { return component; }
    public String getLocalClassName() { return getClass().getName(); }
    public Window getWindow() { return window; }
    public android.view.WindowManager getWindowManager() { return (android.view.WindowManager) Device.service(WINDOW_SERVICE); }
    public View getCurrentFocus() { return window.getDecorView(); }
    private final android.view.ViewGroup contentRoot = new android.view.ViewGroup(this) {};
    /** `android.R.id.content` is an empty holder: what the activity shows is its Compose content, which isn't a view here. */
    @SuppressWarnings("unchecked") public <T extends View> T findViewById(int id) { return id == android.R.id.content ? (T) contentRoot : null; }
    public boolean onSearchRequested() { return false; }
    public boolean onSearchRequested(android.view.SearchEvent searchEvent) { return onSearchRequested(); }
    public final <T extends View> T requireViewById(int id) { throw new IllegalArgumentException("ID does not reference a View inside this Activity"); }
    public void setContentView(View view) { window.setContentView(view); }
    public void setContentView(View view, android.view.ViewGroup.LayoutParams params) { window.setContentView(view); }
    public void setContentView(int layoutResID) {}
    public void addContentView(View view, android.view.ViewGroup.LayoutParams params) {}
    public CharSequence getTitle() { return title; }
    public void setTitle(CharSequence title) { this.title = title; }
    public int getTaskId() { return 1; }
    public boolean isTaskRoot() { return Device.activities().isRoot(this); }
    public boolean isFinishing() { return finishing; }
    public boolean isDestroyed() { return destroyed; }
    public final boolean isResumed() { return resumed; }
    public boolean isChangingConfigurations() { return false; }
    public boolean isInMultiWindowMode() { return false; }
    public boolean isInPictureInPictureMode() { return false; }
    public boolean hasWindowFocus() { return resumed; }
    public final void runOnUiThread(Runnable action) { if (java.awt.EventQueue.isDispatchThread()) action.run(); else java.awt.EventQueue.invokeLater(action); }
    public void setRequestedOrientation(int requestedOrientation) {}
    public int getRequestedOrientation() { return 0; }
    public void setTheme(int resid) {}
    public void overridePendingTransition(int enterAnim, int exitAnim) {}
    public void overrideActivityTransition(int overrideType, int enterAnim, int exitAnim) {}
    public void setTurnScreenOn(boolean turnScreenOn) {}
    public void setShowWhenLocked(boolean showWhenLocked) {}
    public final boolean requestWindowFeature(int featureId) { return true; }
    public void reportFullyDrawn() {}
    public void setTaskDescription(ActivityManager.TaskDescription taskDescription) {}
    public final void setVolumeControlStream(int streamType) {}
    public final void setMediaController(android.media.session.MediaController controller) {}
    public void recreate() { Device.activities().recreate(this); }

    public void finish() { if (!finishing) { finishing = true; Device.activities().finish(this); } }
    public void finishAffinity() { finish(); }
    public void finishAndRemoveTask() { finish(); }
    public boolean moveTaskToBack(boolean nonRoot) { return Device.activities().toBack(this); }
    public final void setResult(int resultCode) { this.resultCode = resultCode; }
    public final void setResult(int resultCode, Intent data) { this.resultCode = resultCode; this.resultData = data; }
    public void startActivityForResult(Intent intent, int requestCode) { startActivity(intent); }
    public void startActivityForResult(Intent intent, int requestCode, Bundle options) { startActivity(intent); }
    public final void requestPermissions(String[] permissions, int requestCode) {
        int[] results = new int[permissions.length];
        for (int i = 0; i < permissions.length; i++) results[i] = Device.permission(permissions[i]);
        java.awt.EventQueue.invokeLater(() -> onRequestPermissionsResult(requestCode, permissions, results));
    }
    public boolean shouldShowRequestPermissionRationale(String permission) { return false; }
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {}
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {}

    // ---- Lifecycle: the host calls the perform* methods ----

    protected void onCreate(Bundle savedInstanceState) {}
    protected void onPostCreate(Bundle savedInstanceState) {}
    protected void onStart() {}
    protected void onRestart() {}
    protected void onResume() {}
    protected void onPostResume() {}
    protected void onPause() {}
    protected void onStop() {}
    protected void onDestroy() {}
    protected void onNewIntent(Intent intent) {}
    protected void onSaveInstanceState(Bundle outState) {}
    protected void onRestoreInstanceState(Bundle savedInstanceState) {}
    protected void onUserLeaveHint() {}
    public void onUserInteraction() {}
    public void onConfigurationChanged(Configuration newConfig) {}
    public void onLowMemory() {}
    public void onTrimMemory(int level) {}
    public void onWindowFocusChanged(boolean hasFocus) {}
    public void onAttachedToWindow() {}
    public void onDetachedFromWindow() {}
    public void onBackPressed() { finish(); }
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode, Configuration newConfig) {}
    public void onTopResumedActivityChanged(boolean isTopResumedActivity) {}

    public final void performCreate() { onCreate(null); onPostCreate(null); }
    public final void performStart() { onStart(); }
    public final void performRestart() { onRestart(); }
    public final void performResume() { resumed = true; onResume(); onPostResume(); onWindowFocusChanged(true); }
    public final void performPause() { onWindowFocusChanged(false); resumed = false; onPause(); }
    public final void performStop() { onStop(); }
    public final void performDestroy() { destroyed = true; onDestroy(); }
    public final void performNewIntent(Intent intent) { onNewIntent(intent); }
    public final void performUserLeaving() { onUserLeaveHint(); }
    public final void markFinishing() { finishing = true; }

    // ---- Keys: the host sends each key here; the default passes it to what the activity shows ----

    public boolean dispatchKeyEvent(KeyEvent event) {
        onUserInteraction();
        if (Device.activities().toContent(this, event)) return true;
        return event.dispatch(this, null, this);
    }
    public boolean dispatchKeyShortcutEvent(KeyEvent event) { return false; }
    public boolean dispatchTouchEvent(android.view.MotionEvent ev) { return false; }
    public boolean dispatchGenericMotionEvent(android.view.MotionEvent ev) { return false; }
    @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) { event.startTracking(); return true; }
        return false;
    }
    @Override public boolean onKeyLongPress(int keyCode, KeyEvent event) { return false; }
    @Override public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.isTracking() && !event.isCanceled()) { onBackPressed(); return true; }
        return false;
    }
    @Override public boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent event) { return false; }
}
