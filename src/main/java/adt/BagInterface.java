package adt;

public interface BagInterface<T> {
    void add(T item);
    boolean contains(T item);
    boolean remove(T item);
    int size();
    boolean isEmpty();
    Object[] toArray();
}