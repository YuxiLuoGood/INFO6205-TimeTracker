package adt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.*;

public class MyPriorityQueueTest {

    private MyPriorityQueue<Integer> pq;

    @BeforeEach
    public void setUp() {
    	pq = new MyPriorityQueue<Integer>(Comparator.naturalOrder());
    }

    @Test
    public void testInitiallyEmpty() {
        assertTrue(pq.isEmpty());
        assertEquals(0, pq.size());
    }

    // ── insert ────────────────────────────────────────────────

    @Test
    public void testInsertOneItem() {
        pq.insert(10);
        assertEquals(1, pq.size());
        assertFalse(pq.isEmpty());
    }

    @Test
    public void testInsertMultipleItems() {
        pq.insert(10);
        pq.insert(30);
        pq.insert(20);
        assertEquals(3, pq.size());
    }

    // ── peekMax ───────────────────────────────────────────────

    @Test
    public void testPeekMaxReturnsLargest() {
        pq.insert(10);
        pq.insert(50);
        pq.insert(30);
        assertEquals(50, pq.peekMax()); 
    }

    @Test
    public void testPeekMaxDoesNotRemoveItem() {
        pq.insert(10);
        pq.insert(50);
        pq.peekMax();
        assertEquals(2, pq.size()); 
    }

    @Test
    public void testPeekMaxOnEmptyQueueThrows() {
        assertThrows(java.util.NoSuchElementException.class, () -> pq.peekMax());
    }

    // ── extractMax ────────────────────────────────────────────

    @Test
    public void testExtractMaxReturnsLargest() {
        pq.insert(10);
        pq.insert(50);
        pq.insert(30);
        assertEquals(50, pq.extractMax()); 
    }

    @Test
    public void testExtractMaxRemovesItem() {
        pq.insert(10);
        pq.insert(50);
        pq.extractMax();
        assertEquals(1, pq.size()); 
    }

    @Test
    public void testExtractMaxReturnsInDescendingOrder() {

        pq.insert(10);
        pq.insert(50);
        pq.insert(30);
        pq.insert(40);
        pq.insert(20);

        assertEquals(50, pq.extractMax()); // Top 1
        assertEquals(40, pq.extractMax()); // Top 2
        assertEquals(30, pq.extractMax()); // Top 3
        assertEquals(20, pq.extractMax()); // Top 4
        assertEquals(10, pq.extractMax()); // Top 5
    }

    @Test
    public void testExtractMaxOnEmptyQueueThrows() {
        assertThrows(java.util.NoSuchElementException.class, () -> pq.extractMax());
    }

    @Test
    public void testGrowBeyondInitialCapacity() {

        for (int i = 1; i <= 20; i++) {
            pq.insert(i);
        }
        assertEquals(20, pq.size());
        assertEquals(20, pq.extractMax()); 
    }

    // ── isEmpty & size ────────────────────────────────────────

    @Test
    public void testIsEmptyAfterExtractAll() {
        pq.insert(10);
        pq.insert(20);
        pq.extractMax();
        pq.extractMax();
        assertTrue(pq.isEmpty());
        assertEquals(0, pq.size());
    }
}
