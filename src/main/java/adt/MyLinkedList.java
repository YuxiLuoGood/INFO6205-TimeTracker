package adt;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

// MyLinkedList — Doubly linked list for storing time entries (TimeEntry)
public class MyLinkedList<T> {

    // Inner Node Class
    private static class Node<T> {
        T data;
        Node<T> prev;
        Node<T> next;

        Node(T data) {
            this.data = data;
        }
    }

    // Fields
    private Node<T> head;   // 哨兵头节点（dummy）
    private Node<T> tail;   // 哨兵尾节点（dummy）
    private int size;

    // Constructor
    public MyLinkedList() {
        head = new Node<>(null);
        tail = new Node<>(null);
        head.next = tail;
        tail.prev = head;
        size = 0;
    }

    // Core Methods

    // Appends a new entry to the end of the list
    // called when the user clicks Stop to add a new TimeEntry
    public void addLast(T entry) {
        Node<T> node = new Node<>(entry);
        node.prev = tail.prev;
        node.next = tail;
        tail.prev.next = node;
        tail.prev = node;
        size++;
    }

    // Removes all nodes that satisfy the given condition
    public void removeIf(Predicate<T> condition) {
        Node<T> cur = head.next;
        while (cur != tail) {
            Node<T> next = cur.next;
            if (condition.test(cur.data)) {
                cur.prev.next = cur.next;
                cur.next.prev = cur.prev;
                size--;
            }
            cur = next;
        }
    }

    // Updates the first node that satisfies the given condition
    // Called when the user edits the duration of a record
    public void editIf(Predicate<T> condition, T newItem) {
        Node<T> cur = head.next;
        while (cur != tail) {
            if (condition.test(cur.data)) {
                cur.data = newItem;
                return;
            }
            cur = cur.next;
        }
    }

    // Returns all entries as a list, O(n)
    // Called when rendering the history panel
    public List<T> toList() {
        List<T> result = new ArrayList<>();
        Node<T> cur = head.next;
        while (cur != tail) {
            result.add(cur.data);
            cur = cur.next;
        }
        return result;
    }

    // Returns the number of entries in the list
    public int size() {
        return size;
    }

    // Returns true if the list is empty
    public boolean isEmpty() {
        return size == 0;
    }
}
