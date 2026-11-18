package android.content.pm;

public class PackageInfo {
    public String packageName, versionName, sharedUserId;
    public int versionCode, baseRevisionCode;
    public long firstInstallTime, lastUpdateTime;
    public ApplicationInfo applicationInfo;
    public ActivityInfo[] activities, receivers;
    public ServiceInfo[] services;
    public ProviderInfo[] providers;
    public String[] requestedPermissions, splitNames;
    public int[] requestedPermissionsFlags, gids;
    public Signature[] signatures;
    public SigningInfo signingInfo;
    public FeatureInfo[] reqFeatures;
    public static final int REQUESTED_PERMISSION_GRANTED = 2;
    public PackageInfo() {}
    public long getLongVersionCode() { return versionCode; }
    public void setLongVersionCode(long longVersionCode) { versionCode = (int) longVersionCode; }
}
