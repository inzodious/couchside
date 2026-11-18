package android.content.pm;

public class ApplicationInfo extends PackageItemInfo {
    public static final int FLAG_SYSTEM = 1, FLAG_DEBUGGABLE = 2, FLAG_HAS_CODE = 4, FLAG_PERSISTENT = 8, FLAG_UPDATED_SYSTEM_APP = 0x80, FLAG_INSTALLED = 0x800000,
        FLAG_STOPPED = 0x200000, FLAG_IS_GAME = 0x2000000, FLAG_LARGE_HEAP = 0x100000, FLAG_EXTERNAL_STORAGE = 0x40000, FLAG_SUSPENDED = 0x40000000;
    public static final int CATEGORY_UNDEFINED = -1, CATEGORY_GAME = 0, CATEGORY_AUDIO = 1, CATEGORY_VIDEO = 2, CATEGORY_IMAGE = 3, CATEGORY_SOCIAL = 4, CATEGORY_NEWS = 5,
        CATEGORY_MAPS = 6, CATEGORY_PRODUCTIVITY = 7, CATEGORY_ACCESSIBILITY = 8;

    public String className, processName, sourceDir, publicSourceDir, dataDir, nativeLibraryDir, permission, taskAffinity;
    public String[] splitSourceDirs, splitPublicSourceDirs, sharedLibraryFiles;
    public int flags = FLAG_INSTALLED | FLAG_HAS_CODE, uid = 10001, targetSdkVersion = 34, minSdkVersion = 25, category = CATEGORY_UNDEFINED, theme, descriptionRes;
    public boolean enabled = true;

    public ApplicationInfo() {}
    public ApplicationInfo(ApplicationInfo o) { super(o); className = o.className; processName = o.processName; sourceDir = o.sourceDir; publicSourceDir = o.publicSourceDir; dataDir = o.dataDir; flags = o.flags; uid = o.uid; targetSdkVersion = o.targetSdkVersion; category = o.category; enabled = o.enabled; }
    public static CharSequence getCategoryTitle(android.content.Context context, int category) {
        switch (category) {
            case CATEGORY_GAME: return "Games"; case CATEGORY_AUDIO: return "Music & Audio"; case CATEGORY_VIDEO: return "Movies & Video"; case CATEGORY_IMAGE: return "Photos & Images";
            case CATEGORY_SOCIAL: return "Social & Communication"; case CATEGORY_NEWS: return "News & Magazines"; case CATEGORY_MAPS: return "Maps & Navigation";
            case CATEGORY_PRODUCTIVITY: return "Productivity"; case CATEGORY_ACCESSIBILITY: return "Accessibility"; default: return null;
        }
    }
    public CharSequence loadDescription(PackageManager pm) { return null; }
}
