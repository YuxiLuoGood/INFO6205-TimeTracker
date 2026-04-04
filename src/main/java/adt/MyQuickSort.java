package adt;

import java.util.Comparator;
import java.util.List;

public class MyQuickSort implements SortInterface {

    /**
     * 对 List 原地排序，平均 O(n log n)，最坏 O(n²)
     * 使用方式：
     *   MyQuickSort sorter = new MyQuickSort();
     *   sorter.sort(projects, (a, b) -> Long.compare(b.getTotalDuration(), a.getTotalDuration()));
     */
    @Override
    public <T> void sort(List<T> list, Comparator<T> comparator) {
        if (list == null || list.size() <= 1) return;
        quickSort(list, 0, list.size() - 1, comparator);
    }

    private <T> void quickSort(List<T> list, int low, int high, Comparator<T> comparator) {
        if (low < high) {
            int pivotIndex = partition(list, low, high, comparator);
            quickSort(list, low, pivotIndex - 1, comparator);
            quickSort(list, pivotIndex + 1, high, comparator);
        }
    }

    /**
     * 分区：以最后一个元素为 pivot
     * 把所有"小于等于 pivot"的元素移到左边，"大于 pivot"的移到右边
     * 返回 pivot 最终所在的索引
     */
    private <T> int partition(List<T> list, int low, int high, Comparator<T> comparator) {
        T pivot = list.get(high);
        int i = low - 1;

        for (int j = low; j < high; j++) {
            if (comparator.compare(list.get(j), pivot) <= 0) {
                i++;
                swap(list, i, j);
            }
        }
        swap(list, i + 1, high);
        return i + 1;
    }

    private <T> void swap(List<T> list, int i, int j) {
        T temp = list.get(i);
        list.set(i, list.get(j));
        list.set(j, temp);
    }
}