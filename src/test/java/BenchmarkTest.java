import adt.*;
import model.Project;
import model.TimeEntry;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

public class BenchmarkTest {

    static final int N = 100;

    public static void main(String[] args) {
        // JVM warmup — run once silently to trigger JIT compilation
        warmup();
        System.out.println("=== ADT Performance Benchmark (N=" + N + ") ===\n");
        benchmarkLinkedList();
        benchmarkHashTable();
        benchmarkPriorityQueue();
        benchmarkQuickSort();
    }

    static void warmup() {
        MyLinkedList<String> l = new MyLinkedList<>();
        for (int i = 0; i < 500; i++) l.addLast("x");
        MyHashTable<String, Integer> h = new MyHashTable<>();
        for (int i = 0; i < 500; i++) { h.put("k" + i, i); h.get("k" + i); }
        MyPriorityQueue<Integer> p = new MyPriorityQueue<>(Integer::compareTo);
        for (int i = 0; i < 500; i++) p.insert(i);
    }

    // ── MyLinkedList ──────────────────────────────────────────

    static void benchmarkLinkedList() {
        MyLinkedList<TimeEntry> list = new MyLinkedList<>();

        // addLast N entries
        long start = System.nanoTime();
        for (int i = 0; i < N; i++) {
            LocalDateTime t = LocalDateTime.now().minusMinutes(i);
            list.addLast(new TimeEntry(t, t.plusMinutes(30), 1800));
        }
        long addTime = System.nanoTime() - start;

        // toList (traverse all)
        start = System.nanoTime();
        List<TimeEntry> all = list.toList();
        long traverseTime = System.nanoTime() - start;

        // removeIf (find + remove one entry)
        TimeEntry target = all.get(N / 2);
        start = System.nanoTime();
        list.removeIf(e -> e == target);
        long removeTime = System.nanoTime() - start;

        System.out.println("MyLinkedList (n=" + N + ")");
        System.out.printf("  addLast x%d:     %,d ns%n", N, addTime);
        System.out.printf("  toList traverse: %,d ns%n", traverseTime);
        System.out.printf("  removeIf:        %,d ns%n%n", removeTime);
    }

    // ── MyHashTable ───────────────────────────────────────────

    static void benchmarkHashTable() {
        MyHashTable<String, Long> table = new MyHashTable<>();

        // put N entries
        long start = System.nanoTime();
        for (int i = 0; i < N; i++) {
            table.put("2026-03-" + String.format("%02d", (i % 28) + 1), (long) i * 100);
        }
        long putTime = System.nanoTime() - start;

        // get (O(1) lookup)
        start = System.nanoTime();
        for (int i = 0; i < N; i++) {
            table.get("2026-03-" + String.format("%02d", (i % 28) + 1));
        }
        long getTime = System.nanoTime() - start;

        // linear scan simulation (what it would cost without HashTable)
        MyLinkedList<String[]> linearList = new MyLinkedList<>();
        for (int i = 0; i < N; i++) {
            linearList.addLast(new String[]{"2026-03-" + String.format("%02d", (i % 28) + 1), String.valueOf(i * 100)});
        }
        start = System.nanoTime();
        for (int i = 0; i < N; i++) {
            final int fi = i;
            linearList.find(e -> e[0].equals("2026-03-" + String.format("%02d", (fi % 28) + 1)));
        }
        long linearTime = System.nanoTime() - start;

        System.out.println("MyHashTable (n=" + N + ")");
        System.out.printf("  put x%d:          %,d ns%n", N, putTime);
        System.out.printf("  get x%d (O(1)):   %,d ns%n", N, getTime);
        System.out.printf("  linear scan x%d:  %,d ns  ← no HashTable%n%n", N, linearTime);
    }

    // ── MyPriorityQueue ───────────────────────────────────────

    static void benchmarkPriorityQueue() {
        MyPriorityQueue<Project> pq = new MyPriorityQueue<>(
                Comparator.comparingLong(Project::getTotalDuration));

        // insert N projects
        long start = System.nanoTime();
        for (int i = 0; i < N; i++) {
            Project p = new Project("Project-" + i);
            p.setTotalDuration(i * 60L);
            pq.insert(p);
        }
        long insertTime = System.nanoTime() - start;

        // peekMax
        start = System.nanoTime();
        pq.peekMax();
        long peekTime = System.nanoTime() - start;

        // extractMax x5 (get top 5)
        start = System.nanoTime();
        for (int i = 0; i < 5; i++) pq.extractMax();
        long extractTime = System.nanoTime() - start;

        System.out.println("MyPriorityQueue (n=" + N + ")");
        System.out.printf("  insert x%d:   %,d ns%n", N, insertTime);
        System.out.printf("  peekMax:      %,d ns%n", peekTime);
        System.out.printf("  extractMax x5:%,d ns%n%n", extractTime);
    }

    // ── MyQuickSort ───────────────────────────────────────────

    static void benchmarkQuickSort() {
        MyQuickSort sorter = new MyQuickSort();

        java.util.List<Project> projects = new java.util.ArrayList<>();
        for (int i = 0; i < N; i++) {
            Project p = new Project("Project-" + i);
            p.setTotalDuration((long)(Math.random() * 10000));
            projects.add(p);
        }

        long start = System.nanoTime();
        sorter.sort(projects, (a, b) ->
                Long.compare(b.getTotalDuration(), a.getTotalDuration()));
        long sortTime = System.nanoTime() - start;

        System.out.println("MyQuickSort (n=" + N + ")");
        System.out.printf("  sort x%d projects: %,d ns%n%n", N, sortTime);
    }
}