package adt;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * MyLinkedList — 双向链表，存储计时记录 (TimeEntry)
 * 成员 A 负责实现
 */
public class MyLinkedList<T> {

    // ── 内部节点类 ──────────────────────────────────────────
    private static class Node<T> {
        T data;
        Node<T> prev;
        Node<T> next;

        Node(T data) {
            this.data = data;
        }
    }

    // ── 字段 ────────────────────────────────────────────────
    private Node<T> head;   // 哨兵头节点（dummy）
    private Node<T> tail;   // 哨兵尾节点（dummy）
    private int size;

    // ── 构造 ────────────────────────────────────────────────
    public MyLinkedList() {
        head = new Node<>(null);
        tail = new Node<>(null);
        head.next = tail;
        tail.prev = head;
        size = 0;
    }

    // ── 核心方法 ─────────────────────────────────────────────

    /**
     * 在链表末尾追加一条记录，O(1)
     * 对应场景：用户点 Stop 时追加新 TimeEntry
     */
    public void addLast(T entry) {
        Node<T> node = new Node<>(entry);
        node.prev = tail.prev;
        node.next = tail;
        tail.prev.next = node;
        tail.prev = node;
        size++;
    }

    /**
     * 删除所有满足条件的节点，O(n)
     * 对应场景：用户删除某条历史记录
     */
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

    /**
     * 修改第一条满足条件的节点，O(n)
     * 对应场景：用户编辑某条记录的时长
     */
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

    /**
     * 返回所有记录的列表，O(n)
     * 对应场景：历史记录面板渲染
     */
    public List<T> toList() {
        List<T> result = new ArrayList<>();
        Node<T> cur = head.next;
        while (cur != tail) {
            result.add(cur.data);
            cur = cur.next;
        }
        return result;
    }

    /** 返回链表中记录的数量 */
    public int size() {
        return size;
    }

    /** 链表是否为空 */
    public boolean isEmpty() {
        return size == 0;
    }
}
