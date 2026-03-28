package adt;

import java.util.ArrayList;
import java.util.List;

public class MyHashTable<K, V> {

    // Separate Chaining: each bucket is a linked chain
    private static class Entry<K, V> {
        K key;
        V value;
        Entry<K, V> next;

        Entry(K key, V value) {
            this.key   = key;
            this.value = value;
        }
    }

    // Constants & Fields
    private static final int    DEFAULT_CAPACITY   = 16;
    private static final double LOAD_FACTOR_LIMIT  = 0.75;

    private Entry<K, V>[] buckets;
    private int size;

    // Constructor
    @SuppressWarnings("unchecked")
    public MyHashTable() {
        buckets = new Entry[DEFAULT_CAPACITY];
        size    = 0;
    }

    // Private Helpers

    // Maps hashCode to a non-negative bucket index
    private int index(K key) {
        return (key.hashCode() & 0x7fffffff) % buckets.length;
    }

    // Doubles the capacity and rehashes all entries when load factor exceeds threshold
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

    // Inserts or updates a key-value pair
    public void put(K key, V value) {
        if ((double) size / buckets.length >= LOAD_FACTOR_LIMIT) {
            resize();
        }
        int i = index(key);
        Entry<K, V> cur = buckets[i];
        // Key already exists in chain → update value
        while (cur != null) {
            if (cur.key.equals(key)) {
                cur.value = value;
                return;
            }
            cur = cur.next;
        }
        // Key not found → insert at head
        Entry<K, V> node = new Entry<>(key, value);
        node.next  = buckets[i];
        buckets[i] = node;
        size++;
    }

    // Retrieves value by key, returns null if not found
    public V get(K key) {
        int i = index(key);
        Entry<K, V> cur = buckets[i];
        while (cur != null) {
            if (cur.key.equals(key)) return cur.value;
            cur = cur.next;
        }
        return null;
    }

    // Removes the entry with the given key
    public void remove(K key) {
        int i = index(key);
        Entry<K, V> cur  = buckets[i];
        Entry<K, V> prev = null;
        while (cur != null) {
            if (cur.key.equals(key)) {
                if (prev == null) buckets[i] = cur.next;
                else              prev.next  = cur.next;
                size--;
                return;
            }
            prev = cur;
            cur  = cur.next;
        }
    }

    // Returns a list of all keys
    // retrieve all recorded dates when generating a report
    public List<K> keySet() {
        List<K> keys = new ArrayList<>();
        for (Entry<K, V> head : buckets) {
            Entry<K, V> cur = head;
            while (cur != null) {
                keys.add(cur.key);
                cur = cur.next;
            }
        }
        return keys;
    }

    // Returns true if the given key exists in the table
    public boolean containsKey(K key) {
        return get(key) != null;
    }

    // Returns the number of key-value pairs stored
    public int size() {
        return size;
    }

    // Returns true if the table is empty
    public boolean isEmpty() {
        return size == 0;
    }
}
