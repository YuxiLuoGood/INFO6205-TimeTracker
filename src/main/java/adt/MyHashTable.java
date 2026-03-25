package adt;

import java.util.ArrayList;
import java.util.List;

/**
 * MyHashTable — 哈希表，以日期字符串为 key，存每天的时长汇总
 * 冲突处理：链地址法（Separate Chaining）
 * 成员 A 负责实现
 */
public class MyHashTable<K, V> {

    // ── 链地址法：每个桶是一条链 ────────────────────────────
    private static class Entry<K, V> {
        K key;
        V value;
        Entry<K, V> next;

        Entry(K key, V value) {
            this.key   = key;
            this.value = value;
        }
    }

    // ── 常量 & 字段 ──────────────────────────────────────────
    private static final int    DEFAULT_CAPACITY   = 16;
    private static final double LOAD_FACTOR_LIMIT  = 0.75;

    private Entry<K, V>[] buckets;
    private int size;

    // ── 构造 ────────────────────────────────────────────────
    @SuppressWarnings("unchecked")
    public MyHashTable() {
        buckets = new Entry[DEFAULT_CAPACITY];
        size    = 0;
    }

    // ── 私有工具 ─────────────────────────────────────────────

    /** 把 hashCode 映射到桶索引，确保非负 */
    private int index(K key) {
        return (key.hashCode() & 0x7fffffff) % buckets.length;
    }

    /** 当负载因子超过阈值时，扩容为原来的两倍并 rehash */
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

    // ── 核心方法 ─────────────────────────────────────────────

    /**
     * 插入或更新键值对，平均 O(1)
     * 对应场景：每次 Stop 后更新当天汇总，key = "2026-03-22"
     */
    public void put(K key, V value) {
        if ((double) size / buckets.length >= LOAD_FACTOR_LIMIT) {
            resize();
        }
        int i = index(key);
        Entry<K, V> cur = buckets[i];
        // 链中已有该 key → 更新
        while (cur != null) {
            if (cur.key.equals(key)) {
                cur.value = value;
                return;
            }
            cur = cur.next;
        }
        // 链中没有 → 头插
        Entry<K, V> node = new Entry<>(key, value);
        node.next  = buckets[i];
        buckets[i] = node;
        size++;
    }

    /**
     * 按 key 查询，平均 O(1)；不存在返回 null
     * 对应场景：查看某天数据、AI 分析取最近 7 天
     */
    public V get(K key) {
        int i = index(key);
        Entry<K, V> cur = buckets[i];
        while (cur != null) {
            if (cur.key.equals(key)) return cur.value;
            cur = cur.next;
        }
        return null;
    }

    /**
     * 删除指定 key，平均 O(1)
     */
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

    /**
     * 返回所有 key 的列表
     * 对应场景：生成报告时获取所有有记录的日期
     */
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

    /** 是否包含某个 key */
    public boolean containsKey(K key) {
        return get(key) != null;
    }

    /** 当前存储的键值对数量 */
    public int size() {
        return size;
    }

    /** 是否为空 */
    public boolean isEmpty() {
        return size == 0;
    }
}
