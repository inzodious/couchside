package android.content;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import couchside.shim.Device;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.concurrent.Executor;

/** Every context is the one simulated TV, so the answers come from [Device]. */
public abstract class Context {
    public static final int MODE_PRIVATE = 0, MODE_WORLD_READABLE = 1, MODE_WORLD_WRITEABLE = 2, MODE_APPEND = 0x8000, MODE_MULTI_PROCESS = 4;
    public static final int BIND_AUTO_CREATE = 1, BIND_IMPORTANT = 0x40, BIND_ABOVE_CLIENT = 8, BIND_NOT_FOREGROUND = 4;
    public static final int RECEIVER_EXPORTED = 2, RECEIVER_NOT_EXPORTED = 4, RECEIVER_VISIBLE_TO_INSTANT_APPS = 1;
    public static final int CONTEXT_IGNORE_SECURITY = 2, CONTEXT_INCLUDE_CODE = 1;
    public static final String ACTIVITY_SERVICE = "activity", ALARM_SERVICE = "alarm", AUDIO_SERVICE = "audio", CONNECTIVITY_SERVICE = "connectivity", POWER_SERVICE = "power",
        WINDOW_SERVICE = "window", WIFI_SERVICE = "wifi", INPUT_METHOD_SERVICE = "input_method", INPUT_SERVICE = "input", ACCESSIBILITY_SERVICE = "accessibility",
        NOTIFICATION_SERVICE = "notification", UI_MODE_SERVICE = "uimode", USAGE_STATS_SERVICE = "usagestats", TV_INPUT_SERVICE = "tv_input", DISPLAY_SERVICE = "display",
        STORAGE_SERVICE = "storage", STORAGE_STATS_SERVICE = "storagestats", MEDIA_SESSION_SERVICE = "media_session", CLIPBOARD_SERVICE = "clipboard", USB_SERVICE = "usb",
        BLUETOOTH_SERVICE = "bluetooth", DOWNLOAD_SERVICE = "download", KEYGUARD_SERVICE = "keyguard", LAYOUT_INFLATER_SERVICE = "layout_inflater", LOCATION_SERVICE = "location",
        SEARCH_SERVICE = "search", SENSOR_SERVICE = "sensor", TELEPHONY_SERVICE = "phone", VIBRATOR_SERVICE = "vibrator", CAPTIONING_SERVICE = "captioning", NSD_SERVICE = "servicediscovery",
        MEDIA_ROUTER_SERVICE = "media_router", JOB_SCHEDULER_SERVICE = "jobscheduler", BATTERY_SERVICE = "batterymanager", HDMI_CONTROL_SERVICE = "hdmi_control",
        DEVICE_POLICY_SERVICE = "device_policy", USER_SERVICE = "user", APP_OPS_SERVICE = "appops", MEDIA_PROJECTION_SERVICE = "media_projection", LAUNCHER_APPS_SERVICE = "launcherapps",
        DROPBOX_SERVICE = "dropbox", TELECOM_SERVICE = "telecom", MIDI_SERVICE = "midi", CAMERA_SERVICE = "camera", PRINT_SERVICE = "print", SHORTCUT_SERVICE = "shortcut",
        MEDIA_METRICS_SERVICE = "media_metrics", PERFORMANCE_HINT_SERVICE = "performance_hint", ROLE_SERVICE = "role", TV_INTERACTIVE_APP_SERVICE = "tv_interactive_app",
        DREAM_SERVICE = "dream", AUDIO_DEVICE_VOLUME_SERVICE = "audio_device_volume";

    public Context getApplicationContext() { return Device.application(); }
    public Context getBaseContext() { return this; }
    public String getPackageName() { return Device.PACKAGE; }
    public String getOpPackageName() { return Device.PACKAGE; }
    public String getAttributionTag() { return null; }
    public String getPackageCodePath() { return Device.codePath(); }
    public String getPackageResourcePath() { return Device.codePath(); }
    public ApplicationInfo getApplicationInfo() { return Device.packages().own(); }
    public PackageManager getPackageManager() { return Device.packages(); }
    public ContentResolver getContentResolver() { return Device.resolver(); }
    public Resources getResources() { return Device.resources(); }
    public AssetManager getAssets() { return Device.resources().getAssets(); }
    public Resources.Theme getTheme() { return getResources().newTheme(); }
    public void setTheme(int resid) {}
    public ClassLoader getClassLoader() { return Context.class.getClassLoader(); }
    public Looper getMainLooper() { return Looper.getMainLooper(); }
    public Executor getMainExecutor() { return java.awt.EventQueue::invokeLater; }
    public android.view.Display getDisplay() { return Device.display(); }
    public int getDeviceId() { return 0; }
    public void registerDeviceIdChangeListener(Executor executor, java.util.function.IntConsumer listener) {}
    public void unregisterDeviceIdChangeListener(java.util.function.IntConsumer listener) {}
    public void registerComponentCallbacks(ComponentCallbacks callback) {}
    public void unregisterComponentCallbacks(ComponentCallbacks callback) {}
    public Context createDeviceProtectedStorageContext() { return this; }
    public Context createConfigurationContext(android.content.res.Configuration overrideConfiguration) { return this; }
    public Context createAttributionContext(String attributionTag) { return this; }
    public Context createPackageContext(String packageName, int flags) throws PackageManager.NameNotFoundException { return this; }
    public boolean isDeviceProtectedStorage() { return false; }
    public boolean isRestricted() { return false; }

    public final String getString(int resId) { return getResources().getString(resId); }
    public final String getString(int resId, Object... formatArgs) { return getResources().getString(resId, formatArgs); }
    public final CharSequence getText(int resId) { return getResources().getText(resId); }
    public final int getColor(int id) { return getResources().getColor(id, null); }
    public final android.graphics.drawable.Drawable getDrawable(int id) { return getResources().getDrawable(id, null); }

    public Object getSystemService(String name) { return Device.service(name); }
    @SuppressWarnings("unchecked") public final <T> T getSystemService(Class<T> serviceClass) { return (T) Device.service(serviceClass); }
    public String getSystemServiceName(Class<?> serviceClass) { return Device.serviceName(serviceClass); }

    public SharedPreferences getSharedPreferences(String name, int mode) { return Device.prefs(name); }
    public boolean deleteSharedPreferences(String name) { return Device.deletePrefs(name); }
    public File getDataDir() { return Device.dir(""); }
    public File getFilesDir() { return Device.dir("files"); }
    public File getNoBackupFilesDir() { return Device.dir("no_backup"); }
    public File getCacheDir() { return Device.dir("cache"); }
    public File getCodeCacheDir() { return Device.dir("code_cache"); }
    public File getExternalCacheDir() { return Device.dir("external/cache"); }
    public File[] getExternalCacheDirs() { return new File[] {getExternalCacheDir()}; }
    public File getExternalFilesDir(String type) { return Device.dir(type == null ? "external/files" : "external/files/" + type); }
    public File[] getExternalFilesDirs(String type) { return new File[] {getExternalFilesDir(type)}; }
    public File[] getExternalMediaDirs() { return new File[] {Device.dir("external/media")}; }
    public File getObbDir() { return Device.dir("external/obb"); }
    public File getDir(String name, int mode) { return Device.dir("app_" + name); }
    public File getFileStreamPath(String name) { return new File(getFilesDir(), name); }
    public File getDatabasePath(String name) { return new File(Device.dir("databases"), name); }
    public String[] fileList() { String[] l = getFilesDir().list(); return l == null ? new String[0] : l; }
    public String[] databaseList() { String[] l = Device.dir("databases").list(); return l == null ? new String[0] : l; }
    public boolean deleteFile(String name) { return new File(getFilesDir(), name).delete(); }
    public boolean deleteDatabase(String name) { return getDatabasePath(name).delete(); }
    public FileInputStream openFileInput(String name) throws FileNotFoundException { return new FileInputStream(new File(getFilesDir(), name)); }
    public FileOutputStream openFileOutput(String name, int mode) throws FileNotFoundException { return new FileOutputStream(new File(getFilesDir(), name), (mode & MODE_APPEND) != 0); }

    public void startActivity(Intent intent) { Device.startActivity(this, intent, null); }
    public void startActivity(Intent intent, Bundle options) { Device.startActivity(this, intent, options); }
    public void startActivities(Intent[] intents) { for (Intent i : intents) startActivity(i); }
    public ComponentName startService(Intent service) { return Device.startService(this, service); }
    public ComponentName startForegroundService(Intent service) { return Device.startService(this, service); }
    public boolean stopService(Intent service) { return Device.stopService(service); }
    public boolean bindService(Intent service, ServiceConnection conn, int flags) { return false; }
    public void unbindService(ServiceConnection conn) {}

    public void sendBroadcast(Intent intent) { Device.broadcast(this, intent); }
    public void sendBroadcast(Intent intent, String receiverPermission) { Device.broadcast(this, intent); }
    public void sendOrderedBroadcast(Intent intent, String receiverPermission) { Device.broadcast(this, intent); }
    public void sendStickyBroadcast(Intent intent) { Device.broadcast(this, intent); }
    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter) { return Device.register(receiver, filter); }
    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter, int flags) { return Device.register(receiver, filter); }
    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter, String broadcastPermission, Handler scheduler) { return Device.register(receiver, filter); }
    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter, String broadcastPermission, Handler scheduler, int flags) { return Device.register(receiver, filter); }
    public void unregisterReceiver(BroadcastReceiver receiver) { Device.unregister(receiver); }

    public int checkSelfPermission(String permission) { return Device.permission(permission); }
    public int checkPermission(String permission, int pid, int uid) { return Device.permission(permission); }
    public int checkCallingPermission(String permission) { return Device.permission(permission); }
    public int checkCallingOrSelfPermission(String permission) { return Device.permission(permission); }
    public void enforceCallingOrSelfPermission(String permission, String message) {}
    public void grantUriPermission(String toPackage, android.net.Uri uri, int modeFlags) {}
    public void revokeUriPermission(android.net.Uri uri, int modeFlags) {}
}
