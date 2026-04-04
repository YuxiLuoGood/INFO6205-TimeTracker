package adt;

import java.util.Comparator;
import java.util.List;

public interface SortInterface {
    <T> void sort(List<T> list, Comparator<T> comparator);
}