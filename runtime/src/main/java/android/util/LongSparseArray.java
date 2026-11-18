package android.util;

import java.util.TreeMap;

public class LongSparseArray<E> implements Cloneable {
    private TreeMap<Long, E> map = new TreeMap<>();
    public LongSparseArray() {}
    public LongSparseArray(int initialCapacity) {}
    @Override public LongSparseArray<E> clone() { LongSparseArray<E> c = new LongSparseArray<>(); c.map = new TreeMap<>(map); return c; }
    public E get(long key) { return map.get(key); }
    public E get(long key, E valueIfKeyNotFound) { return map.getOrDefault(key, valueIfKeyNotFound); }
    public void delete(long key) { map.remove(key); }
    public void remove(long key) { map.remove(key); }
    public void removeAt(int index) { map.remove(keyAt(index)); }
    public void put(long key, E value) { map.put(key, value); }
    public void append(long key, E value) { map.put(key, value); }
    public int size() { return map.size(); }
    public long keyAt(int index) { return map.keySet().stream().skip(index).findFirst().orElseThrow(); }
    public E valueAt(int index) { return map.get(keyAt(index)); }
    public int indexOfKey(long key) { return map.containsKey(key) ? map.headMap(key).size() : -1; }
    public void clear() { map.clear(); }
}
