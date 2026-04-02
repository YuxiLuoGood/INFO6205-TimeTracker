package adt;

import java.util.NoSuchElementException;

public interface PriorityQueueInterface<T> {

    /** 插入元素，O(log n) */
    void insert(T item);

    /** 查看最大元素但不移除，O(1)；空时抛 NoSuchElementException */
    T peekMax() throws NoSuchElementException;

    /** 取出最大元素并移除，O(log n)；空时抛 NoSuchElementException */
    T extractMax() throws NoSuchElementException;

    /** 元素数量 */
    int size();

    /** 是否为空 */
    boolean isEmpty();
}