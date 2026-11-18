package android.util;

import java.util.Arrays;

/** Android's int-keyed map: sorted keys, binary search, the same iteration order as on a TV. */
public class SparseArray<E> implements Cloneable {
    int[] keys;
    Object[] values;
    int size;

    public SparseArray() { this(10); }
    public SparseArray(int initialCapacity) { keys = new int[Math.max(1, initialCapacity)]; values = new Object[keys.length]; }

    @SuppressWarnings("unchecked") @Override public SparseArray<E> clone() {
        try {
            SparseArray<E> c = (SparseArray<E>) super.clone();
            c.keys = keys.clone(); c.values = values.clone();
            return c;
        } catch (CloneNotSupportedException e) { throw new AssertionError(e); }
    }

    private int find(int key) { return Arrays.binarySearch(keys, 0, size, key); }

    public E get(int key) { return get(key, null); }
    @SuppressWarnings("unchecked") public E get(int key, E valueIfKeyNotFound) { int i = find(key); return i < 0 ? valueIfKeyNotFound : (E) values[i]; }
    public boolean contains(int key) { return find(key) >= 0; }
    public void delete(int key) { int i = find(key); if (i >= 0) removeAt(i); }
    public void remove(int key) { delete(key); }
    public void removeAt(int index) {
        System.arraycopy(keys, index + 1, keys, index, size - index - 1);
        System.arraycopy(values, index + 1, values, index, size - index - 1);
        values[--size] = null;
    }
    public void removeAtRange(int index, int count) { for (int i = 0; i < count && index < size; i++) removeAt(index); }
    public void put(int key, E value) {
        int i = find(key);
        if (i >= 0) { values[i] = value; return; }
        i = ~i;
        if (size == keys.length) { keys = Arrays.copyOf(keys, size * 2); values = Arrays.copyOf(values, size * 2); }
        System.arraycopy(keys, i, keys, i + 1, size - i);
        System.arraycopy(values, i, values, i + 1, size - i);
        keys[i] = key; values[i] = value; size++;
    }
    public void set(int key, E value) { put(key, value); }
    public void putAll(SparseArray<? extends E> other) { for (int i = 0; i < other.size(); i++) put(other.keyAt(i), other.valueAt(i)); }
    public E removeReturnOld(int key) { int i = find(key); if (i < 0) return null; E old = valueAt(i); removeAt(i); return old; }
    public void append(int key, E value) {
        if (size != 0 && key <= keys[size - 1]) { put(key, value); return; }
        if (size == keys.length) { keys = Arrays.copyOf(keys, size * 2); values = Arrays.copyOf(values, size * 2); }
        keys[size] = key; values[size] = value; size++;
    }
    public int size() { return size; }
    public int keyAt(int index) { return keys[index]; }
    @SuppressWarnings("unchecked") public E valueAt(int index) { return (E) values[index]; }
    public void setValueAt(int index, E value) { values[index] = value; }
    public int indexOfKey(int key) { int i = find(key); return i < 0 ? -1 : i; }
    public int indexOfValue(E value) { for (int i = 0; i < size; i++) if (values[i] == value) return i; return -1; }
    public boolean contentEquals(SparseArray<?> other) {
        if (other == null || other.size != size) return false;
        for (int i = 0; i < size; i++) if (keys[i] != other.keys[i] || !java.util.Objects.equals(values[i], other.values[i])) return false;
        return true;
    }
    public void clear() { Arrays.fill(values, 0, size, null); size = 0; }
    @Override public String toString() {
        StringBuilder sb = new StringBuilder("{");
        for (int i = 0; i < size; i++) { if (i > 0) sb.append(", "); sb.append(keys[i]).append('=').append(values[i]); }
        return sb.append('}').toString();
    }
}
