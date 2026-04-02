package adt;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MyHashTableTest {

    private MyHashTable<String, Integer> table;

    @BeforeEach
    public void setUp() {
        table = new MyHashTable<>();
    }

    @Test
    public void testInitiallyEmpty() {
        assertTrue(table.isEmpty());
        assertEquals(0, table.size());
    }

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
        table.put("2026-03-22", 200);
        assertEquals(200, table.get("2026-03-22"));
        assertEquals(1, table.size());
    }

    @Test
    public void testGetNonExistentKeyReturnsNull() {
        assertNull(table.get("2026-01-01"));
    }

    @Test
    public void testRemove() {
        table.put("2026-03-22", 120);
        table.remove("2026-03-22");
        assertNull(table.get("2026-03-22"));
        assertEquals(0, table.size());
    }

    @Test
    public void testRemoveNonExistentKeyReturnsFalse() {
        table.put("2026-03-22", 120);
        boolean result = table.remove("9999-99-99");
        assertFalse(result);
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

    @Test
    public void testContainsKey() {
        table.put("2026-03-22", 120);
        assertTrue(table.containsKey("2026-03-22"));
        assertFalse(table.containsKey("2026-01-01"));
    }

    @Test
    public void testKeySet() {
        table.put("2026-03-22", 120);
        table.put("2026-03-23", 90);
        Set<String> keys = table.keySet();  // 改为 Set<String>
        assertEquals(2, keys.size());
        assertTrue(keys.contains("2026-03-22"));
        assertTrue(keys.contains("2026-03-23"));
    }

    @Test
    public void testKeySetEmptyTable() {
        assertTrue(table.keySet().isEmpty());
    }

    @Test
    public void testResizeKeepsAllEntries() {
        for (int i = 0; i < 20; i++) {
            table.put("2026-03-" + String.format("%02d", i + 1), i * 10);
        }
        assertEquals(20, table.size());
        for (int i = 0; i < 20; i++) {
            assertEquals(i * 10, table.get("2026-03-" + String.format("%02d", i + 1)));
        }
    }

    @Test
    public void testIsEmptyAfterRemoveAll() {
        table.put("2026-03-22", 120);
        table.remove("2026-03-22");
        assertTrue(table.isEmpty());
    }
}