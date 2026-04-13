package adt;

import java.util.HashSet;
import java.util.Set;

public class MyHashTable<K, V> implements HashTableInterface<K, V> {

    private static class Entry<K, V> {
        K key;
        V value;
        Entry<K, V> next;

        Entry(K key, V value) {
            this.key   = key;
            this.value = value;
        }
    }

    private static final int    DEFAULT_CAPACITY  = 16;
    private static final double LOAD_FACTOR_LIMIT = 0.75;

    private Entry<K, V>[] buckets;
    private int size;

    @SuppressWarnings("unchecked")
    public MyHashTable() {
        buckets = new Entry[DEFAULT_CAPACITY];
        size    = 0;
    }

    private int index(K key) {
        return (key.hashCode() & 0x7fffffff) % buckets.length;
    }

    @SuppressWarnings("unchecked")
    private void resize() {
        Entry<K, V>[] old = buckets;
        buckets = new Entry[old.length * 2];
        size    = 0;
        for (Entry<K, V> head : old) {
            Entry<K, V> cur = head;
            while (cur != null) {
                put(cur.key, cur.value);
                cur = cur.next;
            }
        }
    }

    @Override
    public void put(K key, V value) {
        if ((double) size / buckets.length >= LOAD_FACTOR_LIMIT) resize();
        int i = index(key);
        Entry<K, V> cur = buckets[i];
        while (cur != null) {
            if (cur.key.equals(key)) { cur.value = value; return; }
            cur = cur.next;
        }
        Entry<K, V> node = new Entry<>(key, value);
        node.next  = buckets[i];
        buckets[i] = node;
        size++;
    }

    @Override
    public V get(K key) {
        Entry<K, V> cur = buckets[index(key)];
        while (cur != null) {
            if (cur.key.equals(key)) return cur.value;
            cur = cur.next;
        }
        return null;
    }

    @Override
    public boolean containsKey(K key) {
        return get(key) != null;
    }

    /** Deletes the specified key and returns whether the operation was successful; O(1) on average */
    @Override
    public boolean remove(K key) {
        int i = index(key);
        Entry<K, V> cur = buckets[i], prev = null;
        while (cur != null) {
            if (cur.key.equals(key)) {
                if (prev == null) buckets[i] = cur.next;
                else prev.next = cur.next;
                size--;
                return true;
            }
            prev = cur;
            cur  = cur.next;
        }
        return false;
    }

    /** Returns a collection of all keys for iterating through all dates */
    @Override
    public Set<K> keySet() {
        Set<K> keys = new HashSet<>();
        for (Entry<K, V> head : buckets) {
            Entry<K, V> cur = head;
            while (cur != null) {
                keys.add(cur.key);
                cur = cur.next;
            }
        }
        return keys;
    }

    @Override
    public int size() { return size; }

    @Override
    public boolean isEmpty() { return size == 0; }
}