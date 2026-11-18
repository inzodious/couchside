package android.provider;

import android.content.ContentResolver;
import couchside.shim.Device;

public final class Settings {
    public static final String ACTION_SETTINGS = "android.settings.SETTINGS", ACTION_APPLICATION_DETAILS_SETTINGS = "android.settings.APPLICATION_DETAILS_SETTINGS",
        ACTION_WIFI_SETTINGS = "android.settings.WIFI_SETTINGS", ACTION_ACCESSIBILITY_SETTINGS = "android.settings.ACCESSIBILITY_SETTINGS",
        ACTION_DISPLAY_SETTINGS = "android.settings.DISPLAY_SETTINGS", ACTION_SOUND_SETTINGS = "android.settings.SOUND_SETTINGS", ACTION_DATE_SETTINGS = "android.settings.DATE_SETTINGS",
        ACTION_DEVICE_INFO_SETTINGS = "android.settings.DEVICE_INFO_SETTINGS", ACTION_MANAGE_UNKNOWN_APP_SOURCES = "android.settings.MANAGE_UNKNOWN_APP_SOURCES",
        ACTION_USAGE_ACCESS_SETTINGS = "android.settings.USAGE_ACCESS_SETTINGS", ACTION_MANAGE_OVERLAY_PERMISSION = "android.settings.action.MANAGE_OVERLAY_PERMISSION",
        ACTION_APPLICATION_SETTINGS = "android.settings.APPLICATION_SETTINGS", ACTION_MANAGE_APPLICATIONS_SETTINGS = "android.settings.MANAGE_APPLICATIONS_SETTINGS",
        ACTION_MANAGE_ALL_APPLICATIONS_SETTINGS = "android.settings.MANAGE_ALL_APPLICATIONS_SETTINGS", ACTION_BLUETOOTH_SETTINGS = "android.settings.BLUETOOTH_SETTINGS",
        ACTION_INPUT_METHOD_SETTINGS = "android.settings.INPUT_METHOD_SETTINGS", ACTION_WIRELESS_SETTINGS = "android.settings.WIRELESS_SETTINGS",
        ACTION_INTERNAL_STORAGE_SETTINGS = "android.settings.INTERNAL_STORAGE_SETTINGS", ACTION_HOME_SETTINGS = "android.settings.HOME_SETTINGS",
        ACTION_CAPTIONING_SETTINGS = "android.settings.CAPTIONING_SETTINGS", ACTION_DREAM_SETTINGS = "android.settings.DREAM_SETTINGS",
        ACTION_APPLICATION_DEVELOPMENT_SETTINGS = "android.settings.APPLICATION_DEVELOPMENT_SETTINGS", ACTION_NETWORK_OPERATOR_SETTINGS = "android.settings.NETWORK_OPERATOR_SETTINGS";

    public static boolean canDrawOverlays(android.content.Context context) { return true; }

    public static class SettingNotFoundException extends android.util.AndroidException {
        public SettingNotFoundException(String msg) { super(msg); }
    }

    private static int intOf(String table, String name) throws SettingNotFoundException {
        String v = Device.setting(table, name);
        try { return Integer.parseInt(v); } catch (NumberFormatException | NullPointerException e) { throw new SettingNotFoundException(name); }
    }
    private static int intOf(String table, String name, int def) { try { return intOf(table, name); } catch (SettingNotFoundException e) { return def; } }
    private static long longOf(String table, String name, long def) { try { return Long.parseLong(Device.setting(table, name)); } catch (RuntimeException e) { return def; } }
    private static float floatOf(String table, String name, float def) { try { return Float.parseFloat(Device.setting(table, name)); } catch (RuntimeException e) { return def; } }

    public static class NameValueTable { public static final String NAME = "name", VALUE = "value"; }

    public static final class Secure extends NameValueTable {
        public static final String ANDROID_ID = "android_id", ENABLED_ACCESSIBILITY_SERVICES = "enabled_accessibility_services", ACCESSIBILITY_ENABLED = "accessibility_enabled",
            DEFAULT_INPUT_METHOD = "default_input_method", ENABLED_INPUT_METHODS = "enabled_input_methods", INSTALL_NON_MARKET_APPS = "install_non_market_apps",
            SCREENSAVER_ENABLED = "screensaver_enabled", SCREENSAVER_COMPONENTS = "screensaver_components", SCREENSAVER_ACTIVATE_ON_SLEEP = "screensaver_activate_on_sleep",
            ACCESSIBILITY_DISPLAY_INVERSION_ENABLED = "accessibility_display_inversion_enabled", TOUCH_EXPLORATION_ENABLED = "touch_exploration_enabled", USER_SETUP_COMPLETE = "user_setup_complete";
        public static android.net.Uri getUriFor(String name) { return android.net.Uri.parse("content://settings/secure/" + name); }
        public static String getString(ContentResolver resolver, String name) { return Device.setting("secure", name); }
        public static boolean putString(ContentResolver resolver, String name, String value) { return Device.putSetting("secure", name, value); }
        public static int getInt(ContentResolver cr, String name, int def) { return intOf("secure", name, def); }
        public static int getInt(ContentResolver cr, String name) throws SettingNotFoundException { return intOf("secure", name); }
        public static boolean putInt(ContentResolver cr, String name, int value) { return Device.putSetting("secure", name, Integer.toString(value)); }
        public static long getLong(ContentResolver cr, String name, long def) { return longOf("secure", name, def); }
        public static boolean putLong(ContentResolver cr, String name, long value) { return Device.putSetting("secure", name, Long.toString(value)); }
        public static float getFloat(ContentResolver cr, String name, float def) { return floatOf("secure", name, def); }
        public static boolean putFloat(ContentResolver cr, String name, float value) { return Device.putSetting("secure", name, Float.toString(value)); }
    }

    public static final class Global extends NameValueTable {
        public static final String DEVICE_NAME = "device_name", ANIMATOR_DURATION_SCALE = "animator_duration_scale", TRANSITION_ANIMATION_SCALE = "transition_animation_scale",
            WINDOW_ANIMATION_SCALE = "window_animation_scale", ADB_ENABLED = "adb_enabled", DEVELOPMENT_SETTINGS_ENABLED = "development_settings_enabled",
            AUTO_TIME = "auto_time", AUTO_TIME_ZONE = "auto_time_zone", STAY_ON_WHILE_PLUGGED_IN = "stay_on_while_plugged_in", AIRPLANE_MODE_ON = "airplane_mode_on",
            DEVICE_PROVISIONED = "device_provisioned", INSTALL_NON_MARKET_APPS = "install_non_market_apps", WIFI_ON = "wifi_on";
        public static android.net.Uri getUriFor(String name) { return android.net.Uri.parse("content://settings/global/" + name); }
        public static String getString(ContentResolver resolver, String name) { return Device.setting("global", name); }
        public static boolean putString(ContentResolver resolver, String name, String value) { return Device.putSetting("global", name, value); }
        public static int getInt(ContentResolver cr, String name, int def) { return intOf("global", name, def); }
        public static int getInt(ContentResolver cr, String name) throws SettingNotFoundException { return intOf("global", name); }
        public static boolean putInt(ContentResolver cr, String name, int value) { return Device.putSetting("global", name, Integer.toString(value)); }
        public static long getLong(ContentResolver cr, String name, long def) { return longOf("global", name, def); }
        public static boolean putLong(ContentResolver cr, String name, long value) { return Device.putSetting("global", name, Long.toString(value)); }
        public static float getFloat(ContentResolver cr, String name, float def) { return floatOf("global", name, def); }
        public static boolean putFloat(ContentResolver cr, String name, float value) { return Device.putSetting("global", name, Float.toString(value)); }
    }

    public static final class System extends NameValueTable {
        public static final String SCREEN_OFF_TIMEOUT = "screen_off_timeout", FONT_SCALE = "font_scale", SCREEN_BRIGHTNESS = "screen_brightness", TIME_12_24 = "time_12_24",
            SOUND_EFFECTS_ENABLED = "sound_effects_enabled", ACCELEROMETER_ROTATION = "accelerometer_rotation";
        public static android.net.Uri getUriFor(String name) { return android.net.Uri.parse("content://settings/system/" + name); }
        public static boolean canWrite(android.content.Context context) { return true; }
        public static String getString(ContentResolver resolver, String name) { return Device.setting("system", name); }
        public static boolean putString(ContentResolver resolver, String name, String value) { return Device.putSetting("system", name, value); }
        public static int getInt(ContentResolver cr, String name, int def) { return intOf("system", name, def); }
        public static int getInt(ContentResolver cr, String name) throws SettingNotFoundException { return intOf("system", name); }
        public static boolean putInt(ContentResolver cr, String name, int value) { return Device.putSetting("system", name, Integer.toString(value)); }
        public static long getLong(ContentResolver cr, String name, long def) { return longOf("system", name, def); }
        public static boolean putLong(ContentResolver cr, String name, long value) { return Device.putSetting("system", name, Long.toString(value)); }
        public static float getFloat(ContentResolver cr, String name, float def) { return floatOf("system", name, def); }
        public static boolean putFloat(ContentResolver cr, String name, float value) { return Device.putSetting("system", name, Float.toString(value)); }
    }
}
