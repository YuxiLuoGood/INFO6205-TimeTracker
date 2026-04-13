package adt;

import java.util.Comparator;
import java.util.List;

public class MyQuickSort implements SortInterface {

    /**
     * In-place sorting of a list: average time complexity O(n log n), worst-case time complexity O(n²)
     * Usage:
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
     * Partition: Use the last element as the pivot
     * Move all elements “less than or equal to the pivot” to the left, and all elements “greater than the pivot” to the right
     * Return the index where the pivot ends up
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