package android.util;

public class SparseBooleanArray implements Cloneable {
    private final SparseArray<Boolean> map;
    public SparseBooleanArray() { map = new SparseArray<>(); }
    public SparseBooleanArray(int initialCapacity) { map = new SparseArray<>(initialCapacity); }
    private SparseBooleanArray(SparseArray<Boolean> m) { map = m; }
    @Override public SparseBooleanArray clone() { return new SparseBooleanArray(map.clone()); }
    public boolean get(int key) { return get(key, false); }
    public boolean get(int key, boolean valueIfKeyNotFound) { return map.get(key, valueIfKeyNotFound); }
    public void delete(int key) { map.delete(key); }
    public void removeAt(int index) { map.removeAt(index); }
    public void put(int key, boolean value) { map.put(key, value); }
    public void append(int key, boolean value) { map.append(key, value); }
    public int size() { return map.size(); }
    public int keyAt(int index) { return map.keyAt(index); }
    public boolean valueAt(int index) { return map.valueAt(index); }
    public void setValueAt(int index, boolean value) { map.setValueAt(index, value); }
    public int indexOfKey(int key) { return map.indexOfKey(key); }
    public void clear() { map.clear(); }
    @Override public String toString() { return map.toString(); }
}
