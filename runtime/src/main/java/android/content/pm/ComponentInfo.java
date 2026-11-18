package android.content.pm;

public class ComponentInfo extends PackageItemInfo {
    public ApplicationInfo applicationInfo;
    public String processName, splitName;
    public int descriptionRes;
    public boolean enabled = true, exported = true, directBootAware;
    public ComponentInfo() {}
    public ComponentInfo(ComponentInfo o) { super(o); applicationInfo = o.applicationInfo; processName = o.processName; enabled = o.enabled; exported = o.exported; }
    public boolean isEnabled() { return enabled && (applicationInfo == null || applicationInfo.enabled); }
    public final int getIconResource() { return icon != 0 ? icon : applicationInfo == null ? 0 : applicationInfo.icon; }
    public final int getLogoResource() { return logo != 0 ? logo : applicationInfo == null ? 0 : applicationInfo.logo; }
    public final int getBannerResource() { return banner != 0 ? banner : applicationInfo == null ? 0 : applicationInfo.banner; }
}
