package adt;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MyBagTest {

    private MyBag<String> bag;

    @BeforeEach
    public void setUp() {
        bag = new MyBag<>();
    }

    @Test
    public void testInitiallyEmpty() {
        assertTrue(bag.isEmpty());
        assertEquals(0, bag.size());
    }

    @Test
    public void testAddOneItem() {
        bag.add("Study");
        assertEquals(1, bag.size());
        assertFalse(bag.isEmpty());
    }

    @Test
    public void testAddMultipleItems() {
        bag.add("Study");
        bag.add("Exercise");
        bag.add("Reading");
        assertEquals(3, bag.size());
    }

    @Test
    public void testContainsExistingItem() {
        bag.add("Study");
        assertTrue(bag.contains("Study"));
    }

    @Test
    public void testContainsNonExistingItem() {
        bag.add("Study");
        assertFalse(bag.contains("Exercise"));
    }

    @Test
    public void testContainsOnEmptyBag() {
        assertFalse(bag.contains("Study"));
    }

    @Test
    public void testContainsProjectByName() {
        bag.add("Study");
        bag.add("Exercise");
        assertTrue(bag.containsProject("Study"));
        assertTrue(bag.containsProject("Exercise"));
        assertFalse(bag.containsProject("Sleep"));
    }

    @Test
    public void testContainsProjectOnEmptyBag() {
        assertFalse(bag.containsProject("Study"));
    }

    @Test
    public void testRemoveExistingItem() {
        bag.add("Study");
        bag.remove("Study");
        assertEquals(0, bag.size());
        assertFalse(bag.contains("Study"));
    }

    @Test
    public void testRemoveOneOfMultipleItems() {
        bag.add("Study");
        bag.add("Exercise");
        bag.add("Reading");
        bag.remove("Exercise");
        assertEquals(2, bag.size());
        assertFalse(bag.contains("Exercise"));
        assertTrue(bag.contains("Study"));
        assertTrue(bag.contains("Reading"));
    }

    @Test
    public void testRemoveNonExistingItemDoesNothing() {
        bag.add("Study");
        bag.remove("Exercise"); 
        assertEquals(1, bag.size());
    }

    @Test
    public void testRemoveFromEmptyBagDoesNotThrow() {
        assertDoesNotThrow(() -> bag.remove("Study"));
    }

    @Test
    public void testToArrayReturnsAllItems() {
        bag.add("Study");
        bag.add("Exercise");
        Object[] arr = bag.toArray();
        assertEquals(2, arr.length);
    }

    @Test
    public void testToArrayEmptyBag() {
        Object[] arr = bag.toArray();
        assertEquals(0, arr.length);
    }


    @Test
    public void testIteratorTraversesAllItems() {
        bag.add("Study");
        bag.add("Exercise");
        bag.add("Reading");
        int count = 0;
        for (String item : bag) {
            count++;
        }
        assertEquals(3, count);
    }

    @Test
    public void testIteratorOnEmptyBag() {
        Iterator<String> it = bag.iterator();
        assertFalse(it.hasNext());
    }

    @Test
    public void testIsEmptyAfterRemoveAll() {
        bag.add("Study");
        bag.remove("Study");
        assertTrue(bag.isEmpty());
        assertEquals(0, bag.size());
    }
}