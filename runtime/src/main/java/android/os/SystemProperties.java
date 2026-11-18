package android.os;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** The device's system properties: what the simulated TV reports (Build's values), plus what the app sets. */
public class SystemProperties {
    public static final int PROP_NAME_MAX = Integer.MAX_VALUE, PROP_VALUE_MAX = 91;
    private static final Map<String, String> values = new ConcurrentHashMap<>();

    static {
        values.put("ro.build.version.sdk", String.valueOf(Build.VERSION.SDK_INT));
        values.put("ro.build.version.release", Build.VERSION.RELEASE);
        values.put("ro.product.manufacturer", Build.MANUFACTURER);
        values.put("ro.product.brand", Build.BRAND);
        values.put("ro.product.model", Build.MODEL);
        values.put("ro.product.device", Build.DEVICE);
        values.put("ro.debuggable", "0");
    }

    public static String get(String key) { return get(key, ""); }
    public static String get(String key, String def) { String v = values.get(key); return v == null || v.isEmpty() ? def : v; }
    public static int getInt(String key, int def) { try { return Integer.parseInt(get(key, "")); } catch (NumberFormatException e) { return def; } }
    public static long getLong(String key, long def) { try { return Long.parseLong(get(key, "")); } catch (NumberFormatException e) { return def; } }
    public static boolean getBoolean(String key, boolean def) {
        String v = get(key, "");
        if (v.equals("1") || v.equals("y") || v.equals("yes") || v.equals("on") || v.equals("true")) return true;
        if (v.equals("0") || v.equals("n") || v.equals("no") || v.equals("off") || v.equals("false")) return false;
        return def;
    }
    public static void set(String key, String value) { if (value == null) values.remove(key); else values.put(key, value); }
    public static void addChangeCallback(Runnable callback) {}
    public static void removeChangeCallback(Runnable callback) {}
    public static void reportSyspropChanged() {}
}
