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

    private final List<Project> projects;
    private final MyHashTable<String, DailySummary> dailyTable;
    private final MyPriorityQueue<Project> rankingQueue;
    private final MyQuickSort sorter;

    public SummaryService(List<Project> projects) {
        this.projects     = projects;
        this.dailyTable   = new MyHashTable<>();
        this.rankingQueue = new MyPriorityQueue<>(
                Comparator.comparingLong(Project::getTotalDuration));
        this.sorter       = new MyQuickSort();
    }

    // ── 每次 stop() 后调用，更新 HashTable 和 PriorityQueue ─────

    /**
     * 记录一条新的 TimeEntry 到 dailyTable
     * TimerService.stop() 之后由 Main/GUI 调用
     */
    public void recordEntry(Project project, TimeEntry entry) {
        String key = entry.getDate().toString();
        DailySummary summary = dailyTable.get(key);
        if (summary == null) {
            summary = new DailySummary(entry.getDate());
            dailyTable.put(key, summary);
        }
        summary.addEntry(project.getName(), entry.getDuration());

        // 更新实时排行：重新插入最新 totalDuration
        rankingQueue.insert(project);
    }

    /**
     * 补录或编辑记录后同步更新 dailyTable
     */
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

    /**
     * 获取某天的汇总，O(1)
     * 日历同步、AI prompt 构建时调用
     */
    public DailySummary getDailySummary(LocalDate date) {
        return dailyTable.get(date.toString());
    }

    /**
     * 获取实时排行 Top N（来自 PriorityQueue）
     * GUI 每次 stop() 后刷新排行榜时调用
     */
    public List<Project> getTopN(int n) {
        List<Project> top = new ArrayList<>();
        List<Project> temp = new ArrayList<>();

        // 依次 extractMax 取出 top N
        int count = Math.min(n, rankingQueue.size());
        for (int i = 0; i < count; i++) {
            Project p = rankingQueue.extractMax();
            top.add(p);
            temp.add(p);
        }
        // 取出后重新插回，保持队列完整
        for (Project p : temp) {
            rankingQueue.insert(p);
        }
        return top;
    }

    /**
     * 获取完整排序报告（来自 QuickSort）
     * 导出报告或喂给 AI 时调用
     */
    public List<Project> getSortedProjects() {
        List<Project> list = new ArrayList<>(projects);
        sorter.sort(list, (a, b) ->
                Long.compare(b.getTotalDuration(), a.getTotalDuration()));
        return list;
    }

    /**
     * 获取最近 N 天的每日汇总，供 AI 分析
     */
    public List<DailySummary> getRecentDays(int days) {
        List<DailySummary> result = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = 0; i < days; i++) {
            LocalDate date = today.minusDays(i);
            DailySummary summary = dailyTable.get(date.toString());
            if (summary != null) result.add(summary);
        }
        return result;
    }

    /**
     * 程序启动时从已有 Project 数据重建 dailyTable
     * JsonStorage.load() 之后调用
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
            // 重建排行队列
            if (p.getTotalDuration() > 0) {
                rankingQueue.insert(p);
            }
        }
    }
}