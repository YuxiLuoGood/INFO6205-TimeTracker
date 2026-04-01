package adt;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class MyQuickSortTest {

    private final MyQuickSort sorter = new MyQuickSort();

    @Test
    void testSortAscending() {
        List<Integer> list = new ArrayList<>(Arrays.asList(5, 3, 8, 1, 9, 2));
        sorter.sort(list, Integer::compareTo);
        assertEquals(Arrays.asList(1, 2, 3, 5, 8, 9), list);
    }

    @Test
    void testSortDescending() {
        List<Integer> list = new ArrayList<>(Arrays.asList(5, 3, 8, 1, 9, 2));
        sorter.sort(list, (a, b) -> Integer.compare(b, a));
        assertEquals(Arrays.asList(9, 8, 5, 3, 2, 1), list);
    }

    @Test
    void testSortByDuration() {
        // 模拟按项目总时长降序排序（实际会用 Project 对象）
        List<long[]> projects = new ArrayList<>();
        projects.add(new long[]{120});  // 2 min
        projects.add(new long[]{3600}); // 1 hour
        projects.add(new long[]{900});  // 15 min

        sorter.sort(projects, (a, b) -> Long.compare(b[0], a[0]));

        assertEquals(3600, projects.get(0)[0]);
        assertEquals(900,  projects.get(1)[0]);
        assertEquals(120,  projects.get(2)[0]);
    }

    @Test
    void testEmptyList() {
        List<Integer> list = new ArrayList<>();
        assertDoesNotThrow(() -> sorter.sort(list, Integer::compareTo));
        assertTrue(list.isEmpty());
    }

    @Test
    void testSingleElement() {
        List<Integer> list = new ArrayList<>(Arrays.asList(42));
        sorter.sort(list, Integer::compareTo);
        assertEquals(1, list.size());
        assertEquals(42, list.get(0));
    }

    @Test
    void testAlreadySorted() {
        List<Integer> list = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5));
        sorter.sort(list, Integer::compareTo);
        assertEquals(Arrays.asList(1, 2, 3, 4, 5), list);
    }

    @Test
    void testDuplicates() {
        List<Integer> list = new ArrayList<>(Arrays.asList(3, 1, 3, 2, 1));
        sorter.sort(list, Integer::compareTo);
        assertEquals(Arrays.asList(1, 1, 2, 3, 3), list);
    }
}