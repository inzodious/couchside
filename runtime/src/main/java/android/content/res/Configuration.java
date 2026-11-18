package android.content.res;

import java.util.Locale;

public final class Configuration implements Comparable<Configuration> {
    public static final int ORIENTATION_UNDEFINED = 0, ORIENTATION_PORTRAIT = 1, ORIENTATION_LANDSCAPE = 2;
    public static final int UI_MODE_TYPE_MASK = 0x0f, UI_MODE_TYPE_UNDEFINED = 0, UI_MODE_TYPE_NORMAL = 1, UI_MODE_TYPE_DESK = 2, UI_MODE_TYPE_CAR = 3, UI_MODE_TYPE_TELEVISION = 4,
        UI_MODE_TYPE_APPLIANCE = 5, UI_MODE_TYPE_WATCH = 6, UI_MODE_TYPE_VR_HEADSET = 7, UI_MODE_NIGHT_MASK = 0x30, UI_MODE_NIGHT_UNDEFINED = 0, UI_MODE_NIGHT_NO = 0x10, UI_MODE_NIGHT_YES = 0x20;
    public static final int SCREENLAYOUT_SIZE_MASK = 0x0f, SCREENLAYOUT_SIZE_SMALL = 1, SCREENLAYOUT_SIZE_NORMAL = 2, SCREENLAYOUT_SIZE_LARGE = 3, SCREENLAYOUT_SIZE_XLARGE = 4,
        SCREENLAYOUT_LAYOUTDIR_MASK = 0xC0, SCREENLAYOUT_LAYOUTDIR_LTR = 0x40;
    public static final int KEYBOARD_NOKEYS = 1, KEYBOARD_QWERTY = 2, NAVIGATION_DPAD = 2, TOUCHSCREEN_NOTOUCH = 1, DENSITY_DPI_UNDEFINED = 0, SCREEN_WIDTH_DP_UNDEFINED = 0;

    public float fontScale = 1f;
    public int mcc, mnc;
    public Locale locale = Locale.getDefault();
    public int touchscreen = TOUCHSCREEN_NOTOUCH, keyboard = KEYBOARD_NOKEYS, keyboardHidden, hardKeyboardHidden, navigation = NAVIGATION_DPAD, navigationHidden;
    public int orientation = ORIENTATION_LANDSCAPE, screenLayout = SCREENLAYOUT_SIZE_XLARGE | SCREENLAYOUT_LAYOUTDIR_LTR, uiMode = UI_MODE_TYPE_TELEVISION | UI_MODE_NIGHT_YES;
    public int screenWidthDp = couchside.shim.ScreenSize.widthDp(), screenHeightDp = couchside.shim.ScreenSize.heightDp(), smallestScreenWidthDp = 540, densityDpi = 320, colorMode, fontWeightAdjustment;

    public Configuration() {}
    public Configuration(Configuration o) { setTo(o); }
    public void setTo(Configuration o) {
        fontScale = o.fontScale; locale = o.locale; orientation = o.orientation; screenLayout = o.screenLayout; uiMode = o.uiMode;
        screenWidthDp = o.screenWidthDp; screenHeightDp = o.screenHeightDp; smallestScreenWidthDp = o.smallestScreenWidthDp; densityDpi = o.densityDpi;
    }
    public void setToDefaults() { setTo(new Configuration()); }
    public android.os.LocaleList getLocales() { return new android.os.LocaleList(locale); }
    public void setLocale(Locale loc) { locale = loc; }
    public void setLocales(android.os.LocaleList locales) { if (locales != null && !locales.isEmpty()) locale = locales.get(0); }
    public void setLayoutDirection(Locale loc) {}
    public int getLayoutDirection() { return 0; }
    public boolean isScreenRound() { return false; }
    public boolean isScreenHdr() { return false; }
    public boolean isScreenWideColorGamut() { return false; }
    public boolean isNightModeActive() { return true; }
    public int diff(Configuration delta) { return equals(delta) ? 0 : -1; }
    @Override public int compareTo(Configuration o) { return Integer.compare(hashCode(), o.hashCode()); }
    @Override public boolean equals(Object o) {
        return o instanceof Configuration c && c.fontScale == fontScale && c.screenWidthDp == screenWidthDp && c.screenHeightDp == screenHeightDp && c.densityDpi == densityDpi
            && c.uiMode == uiMode && c.orientation == orientation && java.util.Objects.equals(c.locale, locale);
    }
    @Override public int hashCode() { return java.util.Objects.hash(fontScale, screenWidthDp, screenHeightDp, densityDpi, uiMode, orientation, locale); }
}
