package android.util;

public class SparseIntArray implements Cloneable {
    private final SparseArray<Integer> map;
    public SparseIntArray() { map = new SparseArray<>(); }
    public SparseIntArray(int initialCapacity) { map = new SparseArray<>(initialCapacity); }
    private SparseIntArray(SparseArray<Integer> m) { map = m; }
    @Override public SparseIntArray clone() { return new SparseIntArray(map.clone()); }
    public int get(int key) { return get(key, 0); }
    public int get(int key, int valueIfKeyNotFound) { return map.get(key, valueIfKeyNotFound); }
    public void delete(int key) { map.delete(key); }
    public void removeAt(int index) { map.removeAt(index); }
    public void put(int key, int value) { map.put(key, value); }
    public void append(int key, int value) { map.append(key, value); }
    public int size() { return map.size(); }
    public int keyAt(int index) { return map.keyAt(index); }
    public int valueAt(int index) { return map.valueAt(index); }
    public void setValueAt(int index, int value) { map.setValueAt(index, value); }
    public int indexOfKey(int key) { return map.indexOfKey(key); }
    public int indexOfValue(int value) { for (int i = 0; i < map.size(); i++) if (map.valueAt(i) == value) return i; return -1; }
    public void clear() { map.clear(); }
    public int[] copyKeys() { int[] k = new int[size()]; for (int i = 0; i < k.length; i++) k[i] = keyAt(i); return k; }
    @Override public String toString() { return map.toString(); }
}
