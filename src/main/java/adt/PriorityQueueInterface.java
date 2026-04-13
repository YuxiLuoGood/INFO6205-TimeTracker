package adt;

import java.util.NoSuchElementException;

public interface PriorityQueueInterface<T> {

	/** Insert an element, O(log n) */
    void insert(T item);

    /** Find the largest element without removing it; O(1); throws NoSuchElementException if the collection is empty */
    T peekMax() throws NoSuchElementException;

    /** Retrieves and removes the largest element; O(log n); throws NoSuchElementException if the list is empty */
    T extractMax() throws NoSuchElementException;

    /** Number of elements */
    int size();

    /** Is it empty? */
    boolean isEmpty();
}