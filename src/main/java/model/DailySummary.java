package model;

import java.time.LocalDate;
import java.util.Set;

import adt.MyHashTable;

public class DailySummary {

    private LocalDate date;
    private MyHashTable<String, Long> projectDurations; // Using a custom HashTable
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

    

    public LocalDate getDate()          { return date;          }
    public long getTotalDuration()      { return totalDuration; }

    /** Returns all project names for AIService to iterate through */
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