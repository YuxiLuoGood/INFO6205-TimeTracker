package adt;

import java.util.List;
import java.util.function.Predicate;

public interface ListInterface<T> {

    /** 在末尾追加，O(1) */
    void addLast(T item);

    /** 删除所有满足条件的节点，返回是否至少删除了一个 */
    boolean removeIf(Predicate<T> condition);

    /** 更新第一个满足条件的节点，返回是否找到并更新 */
    boolean editIf(Predicate<T> condition, T newItem);

    /** 查找第一个满足条件的元素，找不到返回 null */
    T find(Predicate<T> condition);

    /** 转为普通 List，供 UI 渲染用 */
    List<T> toList();

    /** 元素数量 */
    int size();

    /** 是否为空 */
    boolean isEmpty();
}