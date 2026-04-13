package adt;

import java.util.List;
import java.util.function.Predicate;

public interface ListInterface<T> {

	/** Append at the end, O(1) */
    void addLast(T item);

    /** Deletes all nodes that meet the criteria and returns whether at least one node was deleted */
    boolean removeIf(Predicate<T> condition);

    /** Update the first node that meets the criteria, and return whether it was found and updated */
    boolean editIf(Predicate<T> condition, T newItem);

    /** Finds the first element that meets the criteria; returns null if none is found */
    T find(Predicate<T> condition);

    /** Convert to a regular List for UI rendering */
    List<T> toList();

    /** Number of elements */
    int size();

    /** Is it empty? */
    boolean isEmpty();
}