package adt;

import java.util.Comparator;
import java.util.NoSuchElementException;

public class MyPriorityQueue<T> implements PriorityQueueInterface<T> {

    private Object[] heap;
    private int size;
    private final Comparator<T> comparator;
    private static final int DEFAULT_CAPACITY = 16;

    public MyPriorityQueue(Comparator<T> comparator) {
        this.comparator = comparator;
        this.heap = new Object[DEFAULT_CAPACITY];
        this.size = 0;
    }

    @Override
    public void insert(T item) {
        if (size == heap.length) grow();
        heap[size] = item;
        siftUp(size);
        size++;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T peekMax() {
        if (size == 0) throw new NoSuchElementException("Priority queue is empty");
        return (T) heap[0];
    }

    @SuppressWarnings("unchecked")
    @Override
    public T extractMax() {
        if (size == 0) throw new NoSuchElementException("Priority queue is empty");
        T max = (T) heap[0];
        size--;
        heap[0] = heap[size];
        heap[size] = null;
        if (size > 0) siftDown(0);
        return max;
    }

    @Override
    public int size() { return size; }

    @Override
    public boolean isEmpty() { return size == 0; }

    @SuppressWarnings("unchecked")
    private void siftUp(int i) {
        while (i > 0) {
            int parent = (i - 1) / 2;
            if (comparator.compare((T) heap[i], (T) heap[parent]) > 0) {
                swap(i, parent);
                i = parent;
            } else {
                break;
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void siftDown(int i) {
        while (true) {
            int left    = 2 * i + 1;
            int right   = 2 * i + 2;
            int largest = i;

            if (left < size && comparator.compare((T) heap[left], (T) heap[largest]) > 0) {
                largest = left;
            }
            if (right < size && comparator.compare((T) heap[right], (T) heap[largest]) > 0) {
                largest = right;
            }
            if (largest != i) {
                swap(i, largest);
                i = largest;
            } else {
                break;
            }
        }
    }

    private void swap(int i, int j) {
        Object tmp = heap[i];
        heap[i] = heap[j];
        heap[j] = tmp;
    }

    private void grow() {
        Object[] newHeap = new Object[heap.length * 2];
        System.arraycopy(heap, 0, newHeap, 0, size);
        heap = newHeap;
    }
}
