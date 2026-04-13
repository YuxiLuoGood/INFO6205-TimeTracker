package adt;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class MyLinkedList<T> implements ListInterface<T> {

    private static class Node<T> {
        T data;
        Node<T> prev;
        Node<T> next;

        Node(T data) {
            this.data = data;
        }
    }


    private final Node<T> head; // Sentinel Head Node
    private final Node<T> tail; // Sentinel tail node
    private int size;

    public MyLinkedList() {
        head = new Node<>(null);
        tail = new Node<>(null);
        head.next = tail;
        tail.prev = head;
        size = 0;
    }

    /** Append at the end, O(1) */

    @Override
    public void addLast(T item) {
        Node<T> node = new Node<>(item);
        node.prev = tail.prev;
        node.next = tail;
        tail.prev.next = node;
        tail.prev = node;
        size++;
    }

    /** Delete all nodes that meet the criteria and return whether at least one was deleted; O(n) */

    @Override
    public boolean removeIf(Predicate<T> condition) {
        boolean removed = false;
        Node<T> cur = head.next;
        while (cur != tail) {
            Node<T> next = cur.next;
            if (condition.test(cur.data)) {
                cur.prev.next = cur.next;
                cur.next.prev = cur.prev;
                size--;
                removed = true;
            }
            cur = next;
        }
        return removed;
    }

    /** Update the first node that meets the condition and return whether the operation was successful; O(n) */

    @Override
    public boolean editIf(Predicate<T> condition, T newItem) {
        Node<T> cur = head.next;
        while (cur != tail) {
            if (condition.test(cur.data)) {
                cur.data = newItem;
                return true;
            }
            cur = cur.next;
        }
        return false;
    }

    /** Finds the first element that meets the condition; returns null if none is found; O(n) */
    @Override
    public T find(Predicate<T> condition) {
        Node<T> cur = head.next;
        while (cur != tail) {
            if (condition.test(cur.data)) return cur.data;
            cur = cur.next;
        }
        return null;
    }

    /** Convert to a regular list for UI rendering, O(n) */
    @Override
    public List<T> toList() {
        List<T> result = new ArrayList<>();
        Node<T> cur = head.next;
        while (cur != tail) {
            result.add(cur.data);
            cur = cur.next;
        }
        return result;
    }

    @Override
    public int size() { return size; }

    @Override
    public boolean isEmpty() { return size == 0; }
}