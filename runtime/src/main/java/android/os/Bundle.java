package android.os;

import java.io.Serializable;
import java.util.ArrayList;

public final class Bundle extends BaseBundle implements Cloneable {
    public static final Bundle EMPTY = new Bundle();

    public Bundle() {}
    public Bundle(int capacity) {}
    public Bundle(Bundle b) { if (b != null) map.putAll(b.map); }
    public Bundle(ClassLoader loader) {}

    @Override public Object clone() { return new Bundle(this); }
    public Bundle deepCopy() { return new Bundle(this); }
    public void putAll(Bundle bundle) { map.putAll(bundle.map); }
    public void setClassLoader(ClassLoader loader) {}
    public ClassLoader getClassLoader() { return getClass().getClassLoader(); }

    @SuppressWarnings("unchecked") private <T> T typed(String key, T fallback, Class<?> type) {
        Object v = map.get(key);
        return type.isInstance(v) ? (T) v : fallback;
    }
    public void putByte(String key, byte value) { map.put(key, value); }
    public void putChar(String key, char value) { map.put(key, value); }
    public void putShort(String key, short value) { map.put(key, value); }
    public void putFloat(String key, float value) { map.put(key, value); }
    public void putCharSequence(String key, CharSequence value) { map.put(key, value); }
    public void putParcelable(String key, Parcelable value) { map.put(key, value); }
    public void putParcelableArray(String key, Parcelable[] value) { map.put(key, value); }
    public void putParcelableArrayList(String key, ArrayList<? extends Parcelable> value) { map.put(key, value); }
    public void putIntegerArrayList(String key, ArrayList<Integer> value) { map.put(key, value); }
    public void putStringArrayList(String key, ArrayList<String> value) { map.put(key, value); }
    public void putSerializable(String key, Serializable value) { map.put(key, value); }
    public void putByteArray(String key, byte[] value) { map.put(key, value); }
    public void putFloatArray(String key, float[] value) { map.put(key, value); }
    public void putBundle(String key, Bundle value) { map.put(key, value); }
    public void putBinder(String key, IBinder value) { map.put(key, value); }
    public byte getByte(String key) { return typed(key, (byte) 0, Byte.class); }
    public char getChar(String key) { return typed(key, (char) 0, Character.class); }
    public short getShort(String key) { return typed(key, (short) 0, Short.class); }
    public float getFloat(String key) { return getFloat(key, 0f); }
    public float getFloat(String key, float defaultValue) { return typed(key, defaultValue, Float.class); }
    public CharSequence getCharSequence(String key) { return typed(key, null, CharSequence.class); }
    public CharSequence getCharSequence(String key, CharSequence defaultValue) { return typed(key, defaultValue, CharSequence.class); }
    public Bundle getBundle(String key) { return typed(key, null, Bundle.class); }
    @SuppressWarnings("unchecked") public <T extends Parcelable> T getParcelable(String key) { return (T) typed(key, null, Parcelable.class); }
    public <T> T getParcelable(String key, Class<T> clazz) { Object v = map.get(key); return clazz.isInstance(v) ? clazz.cast(v) : null; }
    public Parcelable[] getParcelableArray(String key) { return typed(key, null, Parcelable[].class); }
    @SuppressWarnings("unchecked") public <T extends Parcelable> ArrayList<T> getParcelableArrayList(String key) { return (ArrayList<T>) typed(key, null, ArrayList.class); }
    public Serializable getSerializable(String key) { return typed(key, null, Serializable.class); }
    public <T extends Serializable> T getSerializable(String key, Class<T> clazz) { Object v = map.get(key); return clazz.isInstance(v) ? clazz.cast(v) : null; }
    @SuppressWarnings("unchecked") public ArrayList<Integer> getIntegerArrayList(String key) { return (ArrayList<Integer>) typed(key, null, ArrayList.class); }
    @SuppressWarnings("unchecked") public ArrayList<String> getStringArrayList(String key) { return (ArrayList<String>) typed(key, null, ArrayList.class); }
    public byte[] getByteArray(String key) { return typed(key, null, byte[].class); }
    public float[] getFloatArray(String key) { return typed(key, null, float[].class); }
    public IBinder getBinder(String key) { return typed(key, null, IBinder.class); }
    @Override public String toString() { return "Bundle" + map; }
}
