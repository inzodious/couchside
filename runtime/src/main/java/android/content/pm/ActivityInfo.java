package android.content.pm;

public class ActivityInfo extends ComponentInfo {
    public static final int SCREEN_ORIENTATION_UNSPECIFIED = -1, SCREEN_ORIENTATION_LANDSCAPE = 0, SCREEN_ORIENTATION_PORTRAIT = 1, SCREEN_ORIENTATION_SENSOR_LANDSCAPE = 6;
    public static final int COLOR_MODE_DEFAULT = 0, COLOR_MODE_WIDE_COLOR_GAMUT = 1, COLOR_MODE_HDR = 2;
    public static final int CONFIG_ORIENTATION = 0x80, CONFIG_SCREEN_SIZE = 0x400, CONFIG_KEYBOARD = 0x10, CONFIG_KEYBOARD_HIDDEN = 0x20, CONFIG_UI_MODE = 0x200, CONFIG_FONT_SCALE = 0x40000000;
    public int theme, launchMode, flags, screenOrientation = SCREEN_ORIENTATION_UNSPECIFIED, configChanges, softInputMode, uiOptions, colorMode;
    public String permission, taskAffinity, targetActivity, parentActivityName;
    public ActivityInfo() {}
    public ActivityInfo(ActivityInfo o) { super(o); theme = o.theme; launchMode = o.launchMode; flags = o.flags; targetActivity = o.targetActivity; }
}
