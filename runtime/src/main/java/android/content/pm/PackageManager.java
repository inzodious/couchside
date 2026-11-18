package android.content.pm;

import android.content.ComponentName;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import couchside.shim.Apps;
import java.util.List;

/** The simulated TV's installed apps (see Apps.kt): the app itself, plus stand-ins for what a TV has. */
public class PackageManager {
    public static class NameNotFoundException extends android.util.AndroidException {
        public NameNotFoundException() {}
        public NameNotFoundException(String name) { super(name); }
    }
    public static final class ApplicationInfoFlags { private ApplicationInfoFlags() {} public static ApplicationInfoFlags of(long value) { return new ApplicationInfoFlags(); } public long getValue() { return 0; } }
    public static final class PackageInfoFlags { private PackageInfoFlags() {} public static PackageInfoFlags of(long value) { return new PackageInfoFlags(); } public long getValue() { return 0; } }
    public static final class ResolveInfoFlags { private ResolveInfoFlags() {} public static ResolveInfoFlags of(long value) { return new ResolveInfoFlags(); } public long getValue() { return 0; } }
    public static final class ComponentInfoFlags { private ComponentInfoFlags() {} public static ComponentInfoFlags of(long value) { return new ComponentInfoFlags(); } public long getValue() { return 0; } }

    public static final int PERMISSION_GRANTED = 0, PERMISSION_DENIED = -1;
    public static final int GET_ACTIVITIES = 1, GET_RECEIVERS = 2, GET_SERVICES = 4, GET_PROVIDERS = 8, GET_META_DATA = 0x80, GET_PERMISSIONS = 0x1000, GET_SIGNATURES = 0x40,
        GET_SIGNING_CERTIFICATES = 0x08000000, GET_CONFIGURATIONS = 0x4000, GET_INTENT_FILTERS = 0x20, GET_SHARED_LIBRARY_FILES = 0x400, GET_UNINSTALLED_PACKAGES = 0x2000,
        MATCH_UNINSTALLED_PACKAGES = 0x2000, MATCH_DEFAULT_ONLY = 0x10000, MATCH_ALL = 0x20000, MATCH_DISABLED_COMPONENTS = 0x200, MATCH_SYSTEM_ONLY = 0x100000,
        MATCH_DIRECT_BOOT_AWARE = 0x80000, MATCH_DIRECT_BOOT_UNAWARE = 0x40000, MATCH_DISABLED_UNTIL_USED_COMPONENTS = 0x8000, MATCH_APEX = 0x40000000, GET_DISABLED_COMPONENTS = 0x200;
    public static final int COMPONENT_ENABLED_STATE_DEFAULT = 0, COMPONENT_ENABLED_STATE_ENABLED = 1, COMPONENT_ENABLED_STATE_DISABLED = 2, COMPONENT_ENABLED_STATE_DISABLED_USER = 3,
        COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED = 4, DONT_KILL_APP = 1, SYNCHRONOUS = 2;
    public static final String FEATURE_LEANBACK = "android.software.leanback", FEATURE_LEANBACK_ONLY = "android.software.leanback_only", FEATURE_TELEVISION = "android.hardware.type.television",
        FEATURE_TOUCHSCREEN = "android.hardware.touchscreen", FEATURE_MICROPHONE = "android.hardware.microphone", FEATURE_LIVE_TV = "android.software.live_tv",
        FEATURE_WIFI = "android.hardware.wifi", FEATURE_ETHERNET = "android.hardware.ethernet", FEATURE_BLUETOOTH = "android.hardware.bluetooth", FEATURE_USB_HOST = "android.hardware.usb.host",
        FEATURE_PICTURE_IN_PICTURE = "android.software.picture_in_picture", FEATURE_AUTOMOTIVE = "android.hardware.type.automotive", FEATURE_WATCH = "android.hardware.type.watch",
        FEATURE_PC = "android.hardware.type.pc", FEATURE_HDMI_CEC = "android.hardware.hdmi.cec", FEATURE_WEBVIEW = "android.software.webview", FEATURE_CAMERA_ANY = "android.hardware.camera.any",
        FEATURE_AUDIO_OUTPUT = "android.hardware.audio.output", FEATURE_AUDIO_LOW_LATENCY = "android.hardware.audio.low_latency", FEATURE_SCREEN_LANDSCAPE = "android.hardware.screen.landscape",
        FEATURE_FAKETOUCH = "android.hardware.faketouch", FEATURE_TELEPHONY = "android.hardware.telephony", FEATURE_GAMEPAD = "android.hardware.gamepad";

    public boolean hasSystemFeature(String featureName) { return Apps.feature(featureName); }
    public boolean hasSystemFeature(String featureName, int version) { return Apps.feature(featureName); }
    public FeatureInfo[] getSystemAvailableFeatures() { return new FeatureInfo[0]; }
    public String[] getSystemSharedLibraryNames() { return new String[0]; }

    public ApplicationInfo own() { return Apps.own(); }
    public CharSequence labelOf(String pkg, CharSequence fallback) { return Apps.label(pkg, fallback); }
    public Drawable iconOf(String pkg) { return Apps.icon(pkg); }
    public Drawable bannerOf(String pkg) { return Apps.banner(pkg); }

    public ApplicationInfo getApplicationInfo(String packageName, int flags) throws NameNotFoundException { return Apps.application(packageName); }
    public ApplicationInfo getApplicationInfo(String packageName, ApplicationInfoFlags flags) throws NameNotFoundException { return Apps.application(packageName); }
    public PackageInfo getPackageInfo(String packageName, int flags) throws NameNotFoundException { return Apps.pkg(packageName); }
    public PackageInfo getPackageInfo(String packageName, PackageInfoFlags flags) throws NameNotFoundException { return Apps.pkg(packageName); }
    public ActivityInfo getActivityInfo(ComponentName component, int flags) throws NameNotFoundException { return Apps.activity(component); }
    public ActivityInfo getActivityInfo(ComponentName component, ComponentInfoFlags flags) throws NameNotFoundException { return Apps.activity(component); }
    public ActivityInfo getReceiverInfo(ComponentName component, int flags) throws NameNotFoundException { return Apps.activity(component); }
    public ProviderInfo getProviderInfo(ComponentName component, int flags) throws NameNotFoundException { throw new NameNotFoundException(component.flattenToString()); }
    public ServiceInfo getServiceInfo(ComponentName component, int flags) throws NameNotFoundException { throw new NameNotFoundException(component.flattenToString()); }
    public List<ApplicationInfo> getInstalledApplications(int flags) { return Apps.applications(); }
    public List<ApplicationInfo> getInstalledApplications(ApplicationInfoFlags flags) { return Apps.applications(); }
    public List<PackageInfo> getInstalledPackages(int flags) { return Apps.packages(); }
    public List<PackageInfo> getInstalledPackages(PackageInfoFlags flags) { return Apps.packages(); }
    public int getPackageUid(String packageName, int flags) throws NameNotFoundException { return Apps.application(packageName).uid; }
    public String[] getPackagesForUid(int uid) { return Apps.packagesForUid(uid); }
    public String getNameForUid(int uid) { String[] p = Apps.packagesForUid(uid); return p == null || p.length == 0 ? null : p[0]; }
    public String getInstallerPackageName(String packageName) { return null; }
    public InstallSourceInfo getInstallSourceInfo(String packageName) throws NameNotFoundException { throw new NameNotFoundException(packageName); }
    public boolean isPackageSuspended(String packageName) { return false; }

    public Intent getLaunchIntentForPackage(String packageName) { return Apps.launch(packageName, false); }
    public Intent getLeanbackLaunchIntentForPackage(String packageName) { return Apps.launch(packageName, true); }
    public ResolveInfo resolveActivity(Intent intent, int flags) { List<ResolveInfo> all = Apps.query(intent); return all.isEmpty() ? null : all.get(0); }
    public ResolveInfo resolveActivity(Intent intent, ResolveInfoFlags flags) { return resolveActivity(intent, 0); }
    public List<ResolveInfo> queryIntentActivities(Intent intent, int flags) { return Apps.query(intent); }
    public List<ResolveInfo> queryIntentActivities(Intent intent, ResolveInfoFlags flags) { return Apps.query(intent); }
    public ResolveInfo resolveService(Intent intent, int flags) { return null; }
    public List<ResolveInfo> queryIntentServices(Intent intent, int flags) { return new java.util.ArrayList<>(); }
    public List<ResolveInfo> queryIntentServices(Intent intent, ResolveInfoFlags flags) { return new java.util.ArrayList<>(); }
    public List<ResolveInfo> queryBroadcastReceivers(Intent intent, int flags) { return new java.util.ArrayList<>(); }
    public List<ResolveInfo> queryIntentContentProviders(Intent intent, int flags) { return new java.util.ArrayList<>(); }
    public ProviderInfo resolveContentProvider(String authority, int flags) { return null; }

    public Drawable getApplicationIcon(String packageName) throws NameNotFoundException { Apps.application(packageName); return Apps.icon(packageName); }
    public Drawable getApplicationIcon(ApplicationInfo info) { return Apps.icon(info.packageName); }
    public Drawable getApplicationBanner(String packageName) throws NameNotFoundException { Apps.application(packageName); return Apps.banner(packageName); }
    public Drawable getApplicationBanner(ApplicationInfo info) { return Apps.banner(info.packageName); }
    public Drawable getApplicationLogo(String packageName) throws NameNotFoundException { return null; }
    public Drawable getApplicationLogo(ApplicationInfo info) { return null; }
    public Drawable getActivityIcon(ComponentName activityName) throws NameNotFoundException { return Apps.icon(activityName.getPackageName()); }
    public Drawable getActivityIcon(Intent intent) throws NameNotFoundException { ComponentName c = intent.getComponent(); return c == null ? null : Apps.icon(c.getPackageName()); }
    public Drawable getActivityBanner(ComponentName activityName) throws NameNotFoundException { return Apps.banner(activityName.getPackageName()); }
    public Drawable getActivityBanner(Intent intent) throws NameNotFoundException { ComponentName c = intent.getComponent(); return c == null ? null : Apps.banner(c.getPackageName()); }
    public Drawable getDrawable(String packageName, int resid, ApplicationInfo appInfo) { return Apps.drawable(packageName, resid); }
    public Drawable getDefaultActivityIcon() { return Apps.icon(null); }
    public CharSequence getApplicationLabel(ApplicationInfo info) { return Apps.label(info.packageName, info.nonLocalizedLabel); }
    public CharSequence getText(String packageName, int resid, ApplicationInfo appInfo) { return null; }
    public Resources getResourcesForApplication(ApplicationInfo app) throws NameNotFoundException { return Apps.resources(app.packageName); }
    public Resources getResourcesForApplication(String packageName) throws NameNotFoundException { return Apps.resources(packageName); }

    public void setComponentEnabledSetting(ComponentName componentName, int newState, int flags) { Apps.setEnabled(componentName, newState); }
    public int getComponentEnabledSetting(ComponentName componentName) { return Apps.enabled(componentName); }
    public void setApplicationEnabledSetting(String packageName, int newState, int flags) {}
    public int getApplicationEnabledSetting(String packageName) { return COMPONENT_ENABLED_STATE_DEFAULT; }
    public int checkPermission(String permName, String packageName) { return couchside.shim.Device.permission(permName); }
    public int checkSignatures(String packageName1, String packageName2) { return 0; }
    public boolean canRequestPackageInstalls() { return true; }
    public PackageInstaller getPackageInstaller() { return Apps.installer(); }
    public boolean isSafeMode() { return false; }
}
