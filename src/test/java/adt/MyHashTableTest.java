package adt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MyHashTableTest {

    private MyHashTable<String, Integer> table;

    @BeforeEach
    public void setUp() {
        table = new MyHashTable<>();
    }

    // ── 初始状态 ─────────────────────────────────────────────

    @Test
    public void testInitiallyEmpty() {
        assertTrue(table.isEmpty());
        assertEquals(0, table.size());
    }

    // ── put & get ────────────────────────────────────────────

    @Test
    public void testPutAndGet() {
        table.put("2026-03-22", 120);
        assertEquals(120, table.get("2026-03-22"));
    }

    @Test
    public void testPutMultipleKeys() {
        table.put("2026-03-22", 120);
        table.put("2026-03-23", 90);
        table.put("2026-03-24", 60);
        assertEquals(120, table.get("2026-03-22"));
        assertEquals(90,  table.get("2026-03-23"));
        assertEquals(60,  table.get("2026-03-24"));
        assertEquals(3, table.size());
    }

    @Test
    public void testPutUpdatesExistingKey() {
        table.put("2026-03-22", 120);
        table.put("2026-03-22", 200); // 更新同一个 key
        assertEquals(200, table.get("2026-03-22"));
        assertEquals(1, table.size()); // size 不变
    }

    @Test
    public void testGetNonExistentKeyReturnsNull() {
        assertNull(table.get("2026-01-01"));
    }

    // ── remove ───────────────────────────────────────────────

    @Test
    public void testRemove() {
        table.put("2026-03-22", 120);
        table.remove("2026-03-22");
        assertNull(table.get("2026-03-22"));
        assertEquals(0, table.size());
    }

    @Test
    public void testRemoveNonExistentKeyDoesNothing() {
        table.put("2026-03-22", 120);
        table.remove("9999-99-99"); // 不存在的 key
        assertEquals(1, table.size());
    }

    @Test
    public void testRemoveOneOfMultipleKeys() {
        table.put("2026-03-22", 120);
        table.put("2026-03-23", 90);
        table.remove("2026-03-22");
        assertNull(table.get("2026-03-22"));
        assertEquals(90, table.get("2026-03-23"));
        assertEquals(1, table.size());
    }

    // ── containsKey ──────────────────────────────────────────

    @Test
    public void testContainsKey() {
        table.put("2026-03-22", 120);
        assertTrue(table.containsKey("2026-03-22"));
        assertFalse(table.containsKey("2026-01-01"));
    }

    // ── keySet ───────────────────────────────────────────────

    @Test
    public void testKeySet() {
        table.put("2026-03-22", 120);
        table.put("2026-03-23", 90);
        List<String> keys = table.keySet();
        assertEquals(2, keys.size());
        assertTrue(keys.contains("2026-03-22"));
        assertTrue(keys.contains("2026-03-23"));
    }

    @Test
    public void testKeySetEmptyTable() {
        assertTrue(table.keySet().isEmpty());
    }

    // ── resize（大量插入触发扩容）────────────────────────────

    @Test
    public void testResizeKeepsAllEntries() {
        // 插入 20 条，超过默认容量 16 * 0.75 = 12，触发 resize
        for (int i = 0; i < 20; i++) {
            table.put("2026-03-" + String.format("%02d", i + 1), i * 10);
        }
        assertEquals(20, table.size());
        for (int i = 0; i < 20; i++) {
            assertEquals(i * 10, table.get("2026-03-" + String.format("%02d", i + 1)));
        }
    }

    // ── isEmpty ──────────────────────────────────────────────

    @Test
    public void testIsEmptyAfterRemoveAll() {
        table.put("2026-03-22", 120);
        table.remove("2026-03-22");
        assertTrue(table.isEmpty());
    }
}