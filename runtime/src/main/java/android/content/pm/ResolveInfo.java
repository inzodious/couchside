package android.content.pm;

import android.graphics.drawable.Drawable;

public class ResolveInfo {
    public ActivityInfo activityInfo;
    public ServiceInfo serviceInfo;
    public ProviderInfo providerInfo;
    public android.content.IntentFilter filter;
    public int priority, preferredOrder, match, labelRes, icon;
    public boolean isDefault;
    public CharSequence nonLocalizedLabel;
    public String resolvePackageName;

    public ResolveInfo() {}
    private PackageItemInfo info() { return activityInfo != null ? activityInfo : serviceInfo; }
    public CharSequence loadLabel(PackageManager pm) { return info().loadLabel(pm); }
    public Drawable loadIcon(PackageManager pm) { return info().loadIcon(pm); }
    public final int getIconResource() { return activityInfo == null ? 0 : activityInfo.getIconResource(); }
    @Override public String toString() { return "ResolveInfo{" + (info() == null ? "" : info().packageName + "/" + info().name) + "}"; }
}
