package android.os;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A parcel on the simulated TV, where everything runs in one process: values are kept as the objects themselves, in
 * order, and [marshall] gives a ticket to them rather than their bytes (Media3 parcels subtitle cues this way, writing and
 * reading them back in the same app). Reading follows the order of writing, as with Android's parcels.
 */
public final class Parcel {
    private static final Map<Long, List<Object>> MARSHALLED = new LinkedHashMap<Long, List<Object>>(256, 0.75f, true) {
        @Override protected boolean removeEldestEntry(Map.Entry<Long, List<Object>> eldest) { return size() > 4096; }
    };
    private static final AtomicLong TICKETS = new AtomicLong(1);

    private List<Object> values = new ArrayList<>();
    private int position;

    private Parcel() {}

    public static Parcel obtain() { return new Parcel(); }
    public static Parcel obtain(IBinder binder) { return new Parcel(); }
    public void recycle() { values = new ArrayList<>(); position = 0; }

    public byte[] marshall() {
        long ticket = TICKETS.getAndIncrement();
        synchronized (MARSHALLED) { MARSHALLED.put(ticket, new ArrayList<>(values)); }
        return ByteBuffer.allocate(8).putLong(ticket).array();
    }

    public void unmarshall(byte[] data, int offset, int length) {
        List<Object> found = null;
        if (length == 8) synchronized (MARSHALLED) { found = MARSHALLED.get(ByteBuffer.wrap(data, offset, length).getLong()); }
        values = found != null ? new ArrayList<>(found) : new ArrayList<>();
        position = values.size();
    }

    public int dataSize() { return values.size(); }
    public int dataAvail() { return values.size() - position; }
    public int dataPosition() { return position; }
    public int dataCapacity() { return values.size(); }
    public void setDataPosition(int pos) { position = Math.max(0, Math.min(pos, values.size())); }
    public void setDataSize(int size) { while (values.size() > size) values.remove(values.size() - 1); position = Math.min(position, size); }
    public void setDataCapacity(int size) {}
    public boolean hasFileDescriptors() { return false; }
    public void enforceInterface(String descriptor) {}
    public void writeInterfaceToken(String descriptor) { put(descriptor); }
    public void appendFrom(Parcel parcel, int offset, int length) { for (int i = offset; i < offset + length && i < parcel.values.size(); i++) put(parcel.values.get(i)); }

    private void put(Object value) {
        if (position < values.size()) values.set(position, value); else values.add(value);
        position++;
    }

    @SuppressWarnings("unchecked")
    private <T> T take() { return position < values.size() ? (T) values.get(position++) : null; }

    private <T> T take(T fallback) { T v = take(); return v != null ? v : fallback; }

    public void writeInt(int v) { put(v); }
    public void writeLong(long v) { put(v); }
    public void writeFloat(float v) { put(v); }
    public void writeDouble(double v) { put(v); }
    public void writeByte(byte v) { put(v); }
    public void writeBoolean(boolean v) { put(v); }
    public void writeString(String v) { put(v); }
    public void writeString8(String v) { put(v); }
    public void writeString16(String v) { put(v); }
    public void writeCharSequence(CharSequence v) { put(v); }
    public void writeByteArray(byte[] v) { put(v == null ? null : v.clone()); }
    public void writeByteArray(byte[] v, int offset, int len) { put(v == null ? null : java.util.Arrays.copyOfRange(v, offset, offset + len)); }
    public void writeBlob(byte[] v) { writeByteArray(v); }
    public void writeIntArray(int[] v) { put(v == null ? null : v.clone()); }
    public void writeLongArray(long[] v) { put(v == null ? null : v.clone()); }
    public void writeFloatArray(float[] v) { put(v == null ? null : v.clone()); }
    public void writeDoubleArray(double[] v) { put(v == null ? null : v.clone()); }
    public void writeBooleanArray(boolean[] v) { put(v == null ? null : v.clone()); }
    public void writeStringArray(String[] v) { put(v == null ? null : v.clone()); }
    public void writeCharArray(char[] v) { put(v == null ? null : v.clone()); }
    public void writeStringList(List<String> v) { put(v == null ? null : new ArrayList<>(v)); }
    public void writeList(List<?> v) { put(v == null ? null : new ArrayList<>(v)); }
    public void writeMap(Map<?, ?> v) { put(v == null ? null : new HashMap<>(v)); }
    public void writeBundle(Bundle v) { put(v); }
    public void writePersistableBundle(PersistableBundle v) { put(v); }
    public void writeValue(Object v) { put(v); }
    public void writeParcelable(Parcelable p, int flags) { put(p); }
    public void writeTypedObject(Parcelable p, int flags) { put(p); }
    public void writeParcelableArray(Parcelable[] v, int flags) { put(v); }
    public <T extends Parcelable> void writeTypedList(List<T> v) { writeList(v); }
    public <T extends Parcelable> void writeTypedList(List<T> v, int flags) { writeList(v); }
    public <T extends Parcelable> void writeTypedArray(T[] v, int flags) { put(v); }
    public void writeParcelableList(List<? extends Parcelable> v, int flags) { writeList(v); }
    public void writeSerializable(java.io.Serializable v) { put(v); }
    public void writeStrongBinder(IBinder v) { put(v); }
    public void writeNoException() {}
    public void writeException(Exception e) { put(e); }
    public void writeSparseArray(android.util.SparseArray<?> v) { put(v); }

    public int readInt() { return take(0); }
    public long readLong() { return take(0L); }
    public float readFloat() { return take(0f); }
    public double readDouble() { return take(0d); }
    public byte readByte() { return take((byte) 0); }
    public boolean readBoolean() { return take(false); }
    public String readString() { return take(); }
    public String readString8() { return take(); }
    public String readString16() { return take(); }
    public CharSequence readCharSequence() { return take(); }
    public byte[] createByteArray() { return take(); }
    public void readByteArray(byte[] into) { byte[] v = take(); if (v != null) System.arraycopy(v, 0, into, 0, Math.min(v.length, into.length)); }
    public byte[] readBlob() { return take(); }
    public int[] createIntArray() { return take(); }
    public long[] createLongArray() { return take(); }
    public float[] createFloatArray() { return take(); }
    public double[] createDoubleArray() { return take(); }
    public boolean[] createBooleanArray() { return take(); }
    public String[] createStringArray() { return take(); }
    public String[] readStringArray() { return take(); }
    public char[] createCharArray() { return take(); }
    public ArrayList<String> createStringArrayList() { List<String> v = take(); return v == null ? null : new ArrayList<>(v); }
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void readList(List out, ClassLoader loader) { List<Object> v = take(); if (v != null) out.addAll(v); }
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T> void readList(List<? super T> out, ClassLoader loader, Class<T> clazz) { List v = take(); if (v != null) out.addAll(v); }
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ArrayList readArrayList(ClassLoader loader) { List v = take(); return v == null ? null : new ArrayList(v); }
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void readMap(Map out, ClassLoader loader) { Map v = take(); if (v != null) out.putAll(v); }
    @SuppressWarnings("rawtypes")
    public HashMap readHashMap(ClassLoader loader) { Map v = take(); return v == null ? null : new HashMap<>(v); }
    public Bundle readBundle() { return take(); }
    public Bundle readBundle(ClassLoader loader) { return take(); }
    public PersistableBundle readPersistableBundle() { return take(); }
    public PersistableBundle readPersistableBundle(ClassLoader loader) { return take(); }
    public Object readValue(ClassLoader loader) { return take(); }
    public <T> T readValue(ClassLoader loader, Class<T> clazz, Class<?>... itemTypes) { return take(); }
    public <T extends Parcelable> T readParcelable(ClassLoader loader) { return take(); }
    public <T> T readParcelable(ClassLoader loader, Class<T> clazz) { return take(); }
    public <T> T readTypedObject(Parcelable.Creator<T> c) { return take(); }
    public Parcelable[] readParcelableArray(ClassLoader loader) { return take(); }
    public <T> ArrayList<T> createTypedArrayList(Parcelable.Creator<T> c) { List<T> v = take(); return v == null ? null : new ArrayList<>(v); }
    public <T> T[] createTypedArray(Parcelable.Creator<T> c) { return take(); }
    public <T> void readTypedList(List<T> out, Parcelable.Creator<T> c) { List<T> v = take(); if (v != null) out.addAll(v); }
    public <T extends Parcelable> List<T> readParcelableList(List<T> out, ClassLoader loader) { List<T> v = take(); if (v != null) out.addAll(v); return out; }
    public java.io.Serializable readSerializable() { return take(); }
    public IBinder readStrongBinder() { return take(); }
    public void readException() {}
    public <T> android.util.SparseArray<T> readSparseArray(ClassLoader loader) { return take(); }
}
