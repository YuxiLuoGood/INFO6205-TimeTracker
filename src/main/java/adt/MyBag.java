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

    /** 添加元素，O(1) 头插 */
    @Override
    public void add(T item) {
        Node<T> node = new Node<>(item);
        node.next = head;
        head = node;
        size++;
    }

    /** 检查是否包含某元素，O(n) */
    @Override
    public boolean contains(T item) {
        Node<T> cur = head;
        while (cur != null) {
            if (cur.data.equals(item)) return true;
            cur = cur.next;
        }
        return false;
    }

    /**
     * 按项目名检查是否已存在，O(n)
     * 依赖 Project.toString() 返回 name
     * 新建项目前调用，防止重名
     */
    public boolean containsProject(String name) {
        Node<T> cur = head;
        while (cur != null) {
            if (cur.data != null && cur.data.toString().equals(name)) return true;
            cur = cur.next;
        }
        return false;
    }

    /** 删除第一个匹配的元素，返回是否成功，O(n) */
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

    /** 转为数组，供 GUI 下拉列表渲染，O(n) */
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

    /** 支持 for-each 遍历 */
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