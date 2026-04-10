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

    private final Node<T> head;
    private final Node<T> tail;
    private int size;

    public MyLinkedList() {
        head = new Node<>(null);
        tail = new Node<>(null);
        head.next = tail;
        tail.prev = head;
        size = 0;
    }

    @Override
    public void addLast(T item) {
        Node<T> node = new Node<>(item);
        node.prev = tail.prev;
        node.next = tail;
        tail.prev.next = node;
        tail.prev = node;
        size++;
    }

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

    /** 查找第一个满足条件的元素，找不到返回 null，O(n) */
    @Override
    public T find(Predicate<T> condition) {
        Node<T> cur = head.next;
        while (cur != tail) {
            if (condition.test(cur.data)) return cur.data;
            cur = cur.next;
        }
        return null;
    }

    /** 转为普通 List，供 UI 渲染，O(n) */
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