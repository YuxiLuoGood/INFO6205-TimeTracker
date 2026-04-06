package service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import adt.MyHashTable;
import adt.MyPriorityQueue;
import adt.MyQuickSort;
import model.DailySummary;
import model.Project;
import model.TimeEntry;

public class SummaryService {

    private final List<Project>                    projects;
    private final MyHashTable<String, DailySummary> dailyTable;
    private MyPriorityQueue<Project>               rankingQueue;
    private final MyQuickSort                      sorter;

    public SummaryService(List<Project> projects) {
        this.projects     = projects;
        this.dailyTable   = new MyHashTable<>();
        this.rankingQueue = new MyPriorityQueue<>(
                Comparator.comparingLong(Project::getTotalDuration));
        this.sorter       = new MyQuickSort();
    }

    // ── 记录新条目 ────────────────────────────────────────────

    public void recordEntry(Project project, TimeEntry entry) {
        String key = entry.getDate().toString();
        DailySummary summary = dailyTable.get(key);
        if (summary == null) {
            summary = new DailySummary(entry.getDate());
            dailyTable.put(key, summary);
        }
        summary.addEntry(project.getName(), entry.getDuration());
        rankingQueue.insert(project);
    }

    // ── 补录或编辑后重新计算某天汇总 ─────────────────────────

    public void refreshDate(LocalDate date) {
        String key = date.toString();
        DailySummary summary = new DailySummary(date);
        for (Project p : projects) {
            for (TimeEntry e : p.getEntriesAsList()) {
                if (e.getDate().equals(date)) {
                    summary.addEntry(p.getName(), e.getDuration());
                }
            }
        }
        dailyTable.put(key, summary);
    }

    // ── 查询 ──────────────────────────────────────────────────

    public DailySummary getDailySummary(LocalDate date) {
        return dailyTable.get(date.toString());
    }

    /**
     * 实时 Top N（今天）：来自 PriorityQueue
     */
    public List<Project> getTopN(int n) {
        List<Project> top  = new ArrayList<>();
        List<Project> temp = new ArrayList<>();
        int count = Math.min(n, rankingQueue.size());
        for (int i = 0; i < count; i++) {
            Project p = rankingQueue.extractMax();
            top.add(p);
            temp.add(p);
        }
        for (Project p : temp) rankingQueue.insert(p);
        return top;
    }

    /**
     * 历史某天 Top N：来自 HashTable
     */
    public List<Project> getTopNByDate(LocalDate date, int n) {
        DailySummary summary = getDailySummary(date);
        if (summary == null) return new ArrayList<>();

        List<String[]> list = new ArrayList<>();
        for (String name : summary.getProjectNames()) {
            list.add(new String[]{name,
                    String.valueOf(summary.getDurationForProject(name))});
        }
        list.sort((a, b) -> Long.compare(Long.parseLong(b[1]), Long.parseLong(a[1])));

        List<Project> result = new ArrayList<>();
        for (int i = 0; i < Math.min(n, list.size()); i++) {
            String[] item = list.get(i);
            Project p = new Project(item[0]);
            p.setTotalDuration(Long.parseLong(item[1]));
            result.add(p);
        }
        return result;
    }

    /**
     * 完整排序报告：来自 QuickSort
     */
    public List<Project> getSortedProjects() {
        List<Project> list = new ArrayList<>(projects);
        sorter.sort(list, (a, b) ->
                Long.compare(b.getTotalDuration(), a.getTotalDuration()));
        return list;
    }

    /**
     * 最近 N 天汇总，供 AI 分析
     */
    public List<DailySummary> getRecentDays(int days) {
        List<DailySummary> result = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = 0; i < days; i++) {
            DailySummary summary = dailyTable.get(today.minusDays(i).toString());
            if (summary != null) result.add(summary);
        }
        return result;
    }

    /**
     * 程序启动时从已有 Project 数据重建 HashTable 和 PriorityQueue
     */
    public void rebuildFromProjects() {
        for (Project p : projects) {
            for (TimeEntry e : p.getEntriesAsList()) {
                String key = e.getDate().toString();
                DailySummary summary = dailyTable.get(key);
                if (summary == null) {
                    summary = new DailySummary(e.getDate());
                    dailyTable.put(key, summary);
                }
                summary.addEntry(p.getName(), e.getDuration());
            }
            if (p.getTotalDuration() > 0) rankingQueue.insert(p);
        }
    }

    /**
     * 删除项目后重建 PriorityQueue 和所有相关日期的 DailySummary
     */
    public void rebuildRanking() {
        rankingQueue = new MyPriorityQueue<>(
                Comparator.comparingLong(Project::getTotalDuration));
        for (Project p : projects) {
            if (p.getTotalDuration() > 0) rankingQueue.insert(p);
        }
        // 重建所有日期的 DailySummary（删项目后旧数据需要清除）
        for (String key : dailyTable.keySet()) {
            LocalDate date = LocalDate.parse(key);
            DailySummary summary = new DailySummary(date);
            for (Project p : projects) {
                for (TimeEntry e : p.getEntriesAsList()) {
                    if (e.getDate().equals(date)) {
                        summary.addEntry(p.getName(), e.getDuration());
                    }
                }
            }
            dailyTable.put(key, summary);
        }
    }
}