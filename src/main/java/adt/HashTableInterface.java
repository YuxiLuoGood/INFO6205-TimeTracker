package adt;

import java.util.Set;

public interface HashTableInterface<K, V> {
    void put(K key, V value);
    V get(K key);
    boolean containsKey(K key);
    boolean remove(K key);
    Set<K> keySet();
    int size();
    boolean isEmpty();
}