package org.tiledreader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * <p>A TieredMap is a type of unmodifiable Map defined by an ordered list of
 * other Maps, called tiers. For any given key, the key's corresponding value in
 * the TieredMap is the key's value in the lowest-indexed tier that contains it
 * as a key. If no tier contains a key, the key is not in the TieredMap. If any
 * of a TieredMap's tiers are modified after the TieredMap is constructed, the
 * TieredMap's behavior is undefined.</p>
 * @param <K> The type of keys maintained by this TieredMap
 * @param <V> The type of mapped values
 * @author Alex Heyman
 */
public class TieredMap<K,V> implements Map<K,V> {
    
    private final List<Map<K,V>> tiers;
    private final KeySet keySet;
    private final Values values;
    private final EntrySet entrySet;
    private final int size;
    
    /**
     * Creates a new TieredMap that uses a copy of the specified list of Maps.
     * @param tiers The list of Maps for this TieredMap to use as tiers
     */
    public TieredMap(List<Map<K,V>> tiers) {
        this.tiers = Collections.unmodifiableList(new ArrayList<>(tiers));
        keySet = new KeySet();
        values = new Values();
        entrySet = new EntrySet();
        int numEntries = 0;
        Iterator<Map.Entry<K,V>> iterator = new EntrySetIterator();
        while (iterator.hasNext()) {
            iterator.next();
            numEntries++;
        }
        size = numEntries;
    }
    
    /**
     * Returns an unmodifiable List view of this TieredMap's tiers.
     * @return This TieredMap's tiers
     */
    public List<Map<K,V>> getTiers() {
        return tiers;
    }
    
    private static class Entry<K,V> implements Map.Entry<K,V> {
        
        private final K key;
        private final V value;
        
        private Entry(K key, V value) {
            this.key = key;
            this.value = value;
        }
        
        @Override
        public final K getKey() {
            return key;
        }
        
        @Override
        public final V getValue() {
            return value;
        }
        
        @Override
        public final V setValue(V value) {
            throw new UnsupportedOperationException("Attempted to modify an unmodifiable Map");
        }
        
    }
    
    private class EntrySetIterator implements Iterator<Map.Entry<K,V>> {
        
        private int tier = -1;
        private Iterator<Map.Entry<K,V>> tierIterator = null;
        private Map.Entry<K,V> next = null;
        
        private EntrySetIterator() {
            advanceToNext();
        }
        
        private void advanceToNext() {
            OUTER: while (true) {
                while (tier == -1 || !tierIterator.hasNext()) {
                    tier++;
                    if (tier == tiers.size()) {
                        break OUTER;
                    }
                    tierIterator = tiers.get(tier).entrySet().iterator();
                }
                next = tierIterator.next();
                for (int i = 0; i < tier; i++) {
                    if (tiers.get(i).containsKey(next.getKey())) {
                        continue OUTER;
                    }
                }
                break;
            }
        }
        
        @Override
        public final boolean hasNext() {
            return tier < tiers.size();
        }
        
        @Override
        public final Map.Entry<K,V> next() {
            if (tier < tiers.size()) {
                Map.Entry<K,V> retVal = new Entry<>(next.getKey(), next.getValue());
                advanceToNext();
                return retVal;
            } else {
                throw new NoSuchElementException();
            }
        }
        
        @Override
        public final void remove() {
            throw new UnsupportedOperationException("Attempted to modify an unmodifiable Map");
        }
        
    }
    
    private class EntrySet implements Set<Map.Entry<K,V>> {
        
        @Override
        public final int size() {
            return TieredMap.this.size();
        }
        
        @Override
        public final boolean isEmpty() {
            return TieredMap.this.isEmpty();
        }
        
        @Override
        public final boolean contains(Object o) {
            for (int i = 0; i < tiers.size(); i++) {
                if (tiers.get(i).entrySet().contains(o)) {
                    return true;
                }
            }
            return false;
        }
        
        @Override
        public final Iterator<Map.Entry<K,V>> iterator() {
            return new EntrySetIterator();
        }
        
        @Override
        public final Object[] toArray() {
            Object[] array = new Object[size];
            Iterator<Map.Entry<K,V>> iterator = new EntrySetIterator();
            for (int i = 0; i < size; i++) {
                array[i] = iterator.next();
            }
            return array;
        }
        
        @Override
        public final <T> T[] toArray(T[] a) {
            Iterator<Map.Entry<K,V>> iterator = new EntrySetIterator();
            if (size <= a.length) {
                for (int i = 0; i < size; i++) {
                    a[i] = (T)iterator.next();
                }
                if (size < a.length) {
                    a[size] = null;
                }
                return a;
            }
            T[] newArray = (T[])a[size];
            for (int i = 0; i < size; i++) {
                newArray[i] = (T)iterator.next();
            }
            return newArray;
        }
        
        @Override
        public final boolean add(Map.Entry<K,V> e) {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public final boolean remove(Object o) {
            throw new UnsupportedOperationException("Attempted to modify an unmodifiable Map");
        }
        
        @Override
        public final boolean containsAll(Collection<?> c) {
            for (Object element : c) {
                if (!contains(element)) {
                    return false;
                }
            }
            return true;
        }
        
        @Override
        public final boolean addAll(Collection<? extends Map.Entry<K,V>> c) {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public final boolean retainAll(Collection<?> c) {
            throw new UnsupportedOperationException("Attempted to modify an unmodifiable Map");
        }
        
        @Override
        public final boolean removeAll(Collection<?> c) {
            throw new UnsupportedOperationException("Attempted to modify an unmodifiable Map");
        }
        
        @Override
        public final void clear() {
            throw new UnsupportedOperationException("Attempted to modify an unmodifiable Map");
        }
        
    }
    
    private class KeySetIterator implements Iterator<K> {
        
        private final EntrySetIterator iterator;
        
        private KeySetIterator() {
            iterator = new EntrySetIterator();
        }
        
        @Override
        public final boolean hasNext() {
            return iterator.hasNext();
        }
        
        @Override
        public final K next() {
            if (iterator.hasNext()) {
                return iterator.next().getKey();
            } else {
                throw new NoSuchElementException();
            }
        }
        
        @Override
        public final void remove() {
            throw new UnsupportedOperationException("Attempted to modify an unmodifiable Map");
        }
        
    }
    
    private class KeySet implements Set<K> {
        
        @Override
        public final int size() {
            return TieredMap.this.size();
        }
        
        @Override
        public final boolean isEmpty() {
            return TieredMap.this.isEmpty();
        }
        
        @Override
        public final boolean contains(Object o) {
            for (int i = 0; i < tiers.size(); i++) {
                if (tiers.get(i).entrySet().contains(o)) {
                    return true;
                }
            }
            return false;
        }
        
        @Override
        public final Iterator<K> iterator() {
            return new KeySetIterator();
        }
        
        @Override
        public final Object[] toArray() {
            Object[] array = new Object[size];
            Iterator<K> iterator = new KeySetIterator();
            for (int i = 0; i < size; i++) {
                array[i] = iterator.next();
            }
            return array;
        }
        
        @Override
        public final <T> T[] toArray(T[] a) {
            Iterator<K> iterator = new KeySetIterator();
            if (size <= a.length) {
                for (int i = 0; i < size; i++) {
                    a[i] = (T)iterator.next();
                }
                if (size < a.length) {
                    a[size] = null;
                }
                return a;
            }
            T[] newArray = (T[])a[size];
            for (int i = 0; i < size; i++) {
                newArray[i] = (T)iterator.next();
            }
            return newArray;
        }
        
        @Override
        public final boolean add(K e) {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public final boolean remove(Object o) {
            throw new UnsupportedOperationException("Attempted to modify an unmodifiable Map");
        }
        
        @Override
        public final boolean containsAll(Collection<?> c) {
            for (Object element : c) {
                if (!contains(element)) {
                    return false;
                }
            }
            return true;
        }
        
        @Override
        public final boolean addAll(Collection<? extends K> c) {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public final boolean retainAll(Collection<?> c) {
            throw new UnsupportedOperationException("Attempted to modify an unmodifiable Map");
        }
        
        @Override
        public final boolean removeAll(Collection<?> c) {
            throw new UnsupportedOperationException("Attempted to modify an unmodifiable Map");
        }
        
        @Override
        public final void clear() {
            throw new UnsupportedOperationException("Attempted to modify an unmodifiable Map");
        }
        
    }
    
    private class ValuesIterator implements Iterator<V> {
        
        private final EntrySetIterator iterator;
        
        private ValuesIterator() {
            iterator = new EntrySetIterator();
        }
        
        @Override
        public final boolean hasNext() {
            return iterator.hasNext();
        }
        
        @Override
        public final V next() {
            if (iterator.hasNext()) {
                return iterator.next().getValue();
            } else {
                throw new NoSuchElementException();
            }
        }
        
        @Override
        public final void remove() {
            throw new UnsupportedOperationException("Attempted to modify an unmodifiable Map");
        }
        
    }
    
    private class Values implements Collection<V> {
        
        @Override
        public final int size() {
            return TieredMap.this.size();
        }
        
        @Override
        public final boolean isEmpty() {
            return TieredMap.this.isEmpty();
        }
        
        @Override
        public final boolean contains(Object o) {
            for (int i = 0; i < tiers.size(); i++) {
                if (tiers.get(i).entrySet().contains(o)) {
                    return true;
                }
            }
            return false;
        }
        
        @Override
        public final Iterator<V> iterator() {
            return new ValuesIterator();
        }
        
        @Override
        public final Object[] toArray() {
            Object[] array = new Object[size];
            Iterator<V> iterator = new ValuesIterator();
            for (int i = 0; i < size; i++) {
                array[i] = iterator.next();
            }
            return array;
        }
        
        @Override
        public final <T> T[] toArray(T[] a) {
            Iterator<V> iterator = new ValuesIterator();
            if (size <= a.length) {
                for (int i = 0; i < size; i++) {
                    a[i] = (T)iterator.next();
                }
                if (size < a.length) {
                    a[size] = null;
                }
                return a;
            }
            T[] newArray = (T[])a[size];
            for (int i = 0; i < size; i++) {
                newArray[i] = (T)iterator.next();
            }
            return newArray;
        }
        
        @Override
        public final boolean add(V e) {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public final boolean remove(Object o) {
            throw new UnsupportedOperationException("Attempted to modify an unmodifiable Map");
        }
        
        @Override
        public final boolean containsAll(Collection<?> c) {
            for (Object element : c) {
                if (!contains(element)) {
                    return false;
                }
            }
            return true;
        }
        
        @Override
        public final boolean addAll(Collection<? extends V> c) {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public final boolean retainAll(Collection<?> c) {
            throw new UnsupportedOperationException("Attempted to modify an unmodifiable Map");
        }
        
        @Override
        public final boolean removeAll(Collection<?> c) {
            throw new UnsupportedOperationException("Attempted to modify an unmodifiable Map");
        }
        
        @Override
        public final void clear() {
            throw new UnsupportedOperationException("Attempted to modify an unmodifiable Map");
        }
        
    }
    
    @Override
    public int size() {
        return size;
    }
    
    @Override
    public boolean isEmpty() {
        return size == 0;
    }
    
    @Override
    public boolean containsKey(Object key) {
        for (K myKey : keySet) {
            if (key == null ? myKey == null : key.equals(myKey)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public boolean containsValue(Object value) {
        for (V myValue : values) {
            if (value == null ? myValue == null : value.equals(myValue)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public V get(Object key) {
        for (int i = 0; i < tiers.size(); i++) {
            Map<K,V> tier = tiers.get(i);
            if (tier.containsKey(key)) {
                return tier.get(key);
            }
        }
        return null;
    }
    
    @Override
    public V put(K key, V value) {
        throw new UnsupportedOperationException("Attempted to modify an unmodifiable Map");
    }
    
    @Override
    public V remove(Object key) {
        throw new UnsupportedOperationException("Attempted to modify an unmodifiable Map");
    }
    
    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        throw new UnsupportedOperationException("Attempted to modify an unmodifiable Map");
    }
    
    @Override
    public void clear() {
        throw new UnsupportedOperationException("Attempted to modify an unmodifiable Map");
    }
    
    @Override
    public Set<K> keySet() {
        return keySet;
    }
    
    @Override
    public Collection<V> values() {
        return values;
    }
    
    @Override
    public Set<Map.Entry<K,V>> entrySet() {
        return entrySet;
    }
    
}
