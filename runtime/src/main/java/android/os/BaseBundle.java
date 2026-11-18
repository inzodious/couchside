package android.os;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class BaseBundle {
    final Map<String, Object> map = new LinkedHashMap<>();

    public int size() { return map.size(); }
    public boolean isEmpty() { return map.isEmpty(); }
    public void clear() { map.clear(); }
    public boolean containsKey(String key) { return map.containsKey(key); }
    public Object get(String key) { return map.get(key); }
    public void remove(String key) { map.remove(key); }
    public Set<String> keySet() { return map.keySet(); }

    @SuppressWarnings("unchecked") private <T> T typed(String key, T fallback, Class<?> type) {
        Object v = map.get(key);
        return type.isInstance(v) ? (T) v : fallback;
    }
    public void putBoolean(String key, boolean value) { map.put(key, value); }
    public void putInt(String key, int value) { map.put(key, value); }
    public void putLong(String key, long value) { map.put(key, value); }
    public void putDouble(String key, double value) { map.put(key, value); }
    public void putString(String key, String value) { map.put(key, value); }
    public void putBooleanArray(String key, boolean[] value) { map.put(key, value); }
    public void putIntArray(String key, int[] value) { map.put(key, value); }
    public void putLongArray(String key, long[] value) { map.put(key, value); }
    public void putDoubleArray(String key, double[] value) { map.put(key, value); }
    public void putStringArray(String key, String[] value) { map.put(key, value); }
    public boolean getBoolean(String key) { return getBoolean(key, false); }
    public boolean getBoolean(String key, boolean defaultValue) { return typed(key, defaultValue, Boolean.class); }
    public int getInt(String key) { return getInt(key, 0); }
    public int getInt(String key, int defaultValue) { return typed(key, defaultValue, Integer.class); }
    public long getLong(String key) { return getLong(key, 0L); }
    public long getLong(String key, long defaultValue) { return typed(key, defaultValue, Long.class); }
    public double getDouble(String key) { return getDouble(key, 0.0); }
    public double getDouble(String key, double defaultValue) { return typed(key, defaultValue, Double.class); }
    public String getString(String key) { return typed(key, null, String.class); }
    public String getString(String key, String defaultValue) { return typed(key, defaultValue, String.class); }
    public boolean[] getBooleanArray(String key) { return typed(key, null, boolean[].class); }
    public int[] getIntArray(String key) { return typed(key, null, int[].class); }
    public long[] getLongArray(String key) { return typed(key, null, long[].class); }
    public double[] getDoubleArray(String key) { return typed(key, null, double[].class); }
    public String[] getStringArray(String key) { return typed(key, null, String[].class); }
    public void putAll(PersistableBundle bundle) {}
}
