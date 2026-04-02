package model;

import java.time.LocalDate;
import java.util.Set;

import adt.MyHashTable;

public class DailySummary {

    private LocalDate date;
    private MyHashTable<String, Long> projectDurations; // 用自定义 HashTable
    private long totalDuration;

    public DailySummary(LocalDate date) {
        this.date             = date;
        this.projectDurations = new MyHashTable<>();
        this.totalDuration    = 0;
    }

    public void addEntry(String projectName, long seconds) {
        Long current = projectDurations.get(projectName);
        projectDurations.put(projectName, (current == null ? 0L : current) + seconds);
        totalDuration += seconds;
    }

    public void subtractEntry(String projectName, long seconds) {
        Long current = projectDurations.get(projectName);
        long updated = Math.max(0, (current == null ? 0L : current) - seconds);
        projectDurations.put(projectName, updated);
        totalDuration = Math.max(0, totalDuration - seconds);
    }

    // ── Getters ──────────────────────────────────────────────

    public LocalDate getDate()          { return date;          }
    public long getTotalDuration()      { return totalDuration; }

    /** 返回所有项目名，供 AIService 遍历 */
    public Set<String> getProjectNames() {
        return projectDurations.keySet();
    }

    public long getDurationForProject(String projectName) {
        Long val = projectDurations.get(projectName);
        return val == null ? 0L : val;
    }

    @Override
    public String toString() {
        return "DailySummary{date=" + date + ", total=" + totalDuration + "s}";
    }
}