package android.content.pm;

import android.graphics.drawable.Drawable;

public class PackageItemInfo {
    public String name, packageName;
    public int labelRes, icon, banner, logo;
    public CharSequence nonLocalizedLabel;
    public android.os.Bundle metaData;

    public PackageItemInfo() {}
    public PackageItemInfo(PackageItemInfo o) { name = o.name; packageName = o.packageName; labelRes = o.labelRes; icon = o.icon; banner = o.banner; logo = o.logo; nonLocalizedLabel = o.nonLocalizedLabel; metaData = o.metaData; }
    public CharSequence loadLabel(PackageManager pm) { return pm.labelOf(packageName, nonLocalizedLabel); }
    public Drawable loadIcon(PackageManager pm) { return pm.iconOf(packageName); }
    public Drawable loadUnbadgedIcon(PackageManager pm) { return pm.iconOf(packageName); }
    public Drawable loadBanner(PackageManager pm) { return pm.bannerOf(packageName); }
    public Drawable loadLogo(PackageManager pm) { return null; }
}
