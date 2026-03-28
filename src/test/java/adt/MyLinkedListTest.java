package adt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MyLinkedListTest {

    private MyLinkedList<String> list;

    @BeforeEach
    public void setUp() {
        list = new MyLinkedList<>();
    }

    // Initial State

    @Test
    public void testInitiallyEmpty() {
        assertTrue(list.isEmpty());
        assertEquals(0, list.size());
    }

    // addLast

    @Test
    public void testAddLastSingleElement() {
        list.addLast("entry1");
        assertEquals(1, list.size());
        assertFalse(list.isEmpty());
    }

    @Test
    public void testAddLastMultipleElements() {
        list.addLast("entry1");
        list.addLast("entry2");
        list.addLast("entry3");
        assertEquals(3, list.size());
    }

    @Test
    public void testAddLastPreservesOrder() {
        list.addLast("entry1");
        list.addLast("entry2");
        list.addLast("entry3");
        List<String> result = list.toList();
        assertEquals("entry1", result.get(0));
        assertEquals("entry2", result.get(1));
        assertEquals("entry3", result.get(2));
    }

    // removeIf

    @Test
    public void testRemoveIfMatchingElement() {
        list.addLast("entry1");
        list.addLast("entry2");
        list.removeIf(e -> e.equals("entry1"));
        assertEquals(1, list.size());
        assertFalse(list.toList().contains("entry1"));
    }

    @Test
    public void testRemoveIfMultipleMatches() {
        list.addLast("entry1");
        list.addLast("entry2");
        list.addLast("entry1");
        list.removeIf(e -> e.equals("entry1"));
        assertEquals(1, list.size());
        assertEquals("entry2", list.toList().get(0));
    }

    @Test
    public void testRemoveIfNoMatch() {
        list.addLast("entry1");
        list.addLast("entry2");
        list.removeIf(e -> e.equals("entry99"));
        assertEquals(2, list.size());
    }

    @Test
    public void testRemoveIfAllElements() {
        list.addLast("entry1");
        list.addLast("entry2");
        list.removeIf(e -> true);
        assertEquals(0, list.size());
        assertTrue(list.isEmpty());
    }

    @Test
    public void testRemoveIfFromEmptyList() {
        // removing from an empty list should not throw
        assertDoesNotThrow(() -> list.removeIf(e -> true));
        assertEquals(0, list.size());
    }

    // editIf

    @Test
    public void testEditIfMatchingElement() {
        list.addLast("entry1");
        list.addLast("entry2");
        list.editIf(e -> e.equals("entry1"), "entryUpdated");
        List<String> result = list.toList();
        assertTrue(result.contains("entryUpdated"));
        assertFalse(result.contains("entry1"));
        assertEquals(2, list.size());
    }

    @Test
    public void testEditIfOnlyFirstMatchIsEdited() {
        list.addLast("entry1");
        list.addLast("entry1");
        list.editIf(e -> e.equals("entry1"), "entryUpdated");
        List<String> result = list.toList();
        assertEquals("entryUpdated", result.get(0));
        assertEquals("entry1", result.get(1));
    }

    @Test
    public void testEditIfNoMatch() {
        list.addLast("entry1");
        list.editIf(e -> e.equals("entry99"), "entryUpdated");
        assertEquals("entry1", list.toList().get(0));
    }

    // toList
    @Test
    public void testToListEmptyList() {
        assertTrue(list.toList().isEmpty());
    }

    @Test
    public void testToListReturnsAllElements() {
        list.addLast("entry1");
        list.addLast("entry2");
        List<String> result = list.toList();
        assertEquals(2, result.size());
        assertTrue(result.contains("entry1"));
        assertTrue(result.contains("entry2"));
    }

    // isEmpty & size

    @Test
    public void testIsEmptyAfterRemoveAll() {
        list.addLast("entry1");
        list.removeIf(e -> true);
        assertTrue(list.isEmpty());
        assertEquals(0, list.size());
    }
}