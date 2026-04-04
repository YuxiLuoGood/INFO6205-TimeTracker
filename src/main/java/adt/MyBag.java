package adt;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class MyBag<T> implements BagInterface<T>, Iterable<T> {

    private static class Node<T> {
        T data;
        Node<T> next;

        Node(T data) {
            this.data = data;
        }
    }

    private Node<T> head;
    private int size;

    public MyBag() {
        head = null;
        size = 0;
    }

    @Override
    public void add(T item) {
        Node<T> node = new Node<>(item);
        node.next = head;
        head = node;
        size++;
    }

    @Override
    public boolean contains(T item) {
        Node<T> cur = head;
        while (cur != null) {
            if (cur.data.equals(item)) return true;
            cur = cur.next;
        }
        return false;
    }

    public boolean containsProject(String name) {
        Node<T> cur = head;
        while (cur != null) {
            if (cur.data != null && cur.data.toString().equals(name)) return true;
            cur = cur.next;
        }
        return false;
    }

    @Override
    public boolean remove(T item) {
        Node<T> cur = head, prev = null;
        while (cur != null) {
            if (cur.data.equals(item)) {
                if (prev == null) head = cur.next;
                else prev.next = cur.next;
                size--;
                return true;
            }
            prev = cur;
            cur = cur.next;
        }
        return false;
    }

    @Override
    public int size() { return size; }

    @Override
    public boolean isEmpty() { return size == 0; }

    @Override
    public Object[] toArray() {
        Object[] arr = new Object[size];
        Node<T> cur = head;
        for (int i = 0; i < size; i++) {
            arr[i] = cur.data;
            cur = cur.next;
        }
        return arr;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private Node<T> cur = head;

            @Override
            public boolean hasNext() { return cur != null; }

            @Override
            public T next() {
                if (!hasNext()) throw new NoSuchElementException();
                T data = cur.data;
                cur = cur.next;
                return data;
            }
        };
    }
}
