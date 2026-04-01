package adt;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class MyBag<T> implements Iterable<T> {

    private static class Node<T> {
        T data;
        Node<T> next;

        Node(T data) {
            this.data = data;
            this.next = null;
        }
    }

    private Node<T> head; 
    private int size;  

    public MyBag() {
        head = null;
        size = 0;
    }

 
    public void add(T item) {
        Node<T> newNode = new Node<>(item);
        newNode.next = head; 
        head = newNode; 
        size++;
    }

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
            // toString() 在 Project 类中被重写为返回 name 字段
            if (cur.data != null && cur.data.toString().equals(name)) return true;
            cur = cur.next;
        }
        return false;
    }

    public void remove(T item) {
        Node<T> cur  = head;
        Node<T> prev = null; 

        while (cur != null) {
            if (cur.data.equals(item)) {
                if (prev == null) {
                    head = cur.next;
                } else {
                    prev.next = cur.next;
                }
                size--;
                return; 
            }
            prev = cur;
            cur  = cur.next;
        }
       
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

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
            public boolean hasNext() {
                return cur != null;
            }

            @Override
            public T next() {
                if (!hasNext()) throw new NoSuchElementException("MyBag has no more elements");
                T data = cur.data;
                cur = cur.next;
                return data;
            }
        };
    }
}
