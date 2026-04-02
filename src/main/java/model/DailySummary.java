package model;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;


public class DailySummary {

    private LocalDate date;
    private Map<String, Long> projectDurations;
    private long totalDuration; 

    public DailySummary(LocalDate date) {
        this.date = date;
        this.projectDurations = new HashMap<>();
        this.totalDuration = 0;
    }

    public void addEntry(String projectName, long seconds) {
        long current = projectDurations.getOrDefault(projectName, 0L);
        projectDurations.put(projectName, current + seconds);
        totalDuration += seconds;
    }

    public void subtractEntry(String projectName, long seconds) {
        long current = projectDurations.getOrDefault(projectName, 0L);
        long updated = Math.max(0, current - seconds);
        projectDurations.put(projectName, updated);
        totalDuration = Math.max(0, totalDuration - seconds);
    }

    // ── Getters ──────────────────────────────────────────────

    public LocalDate getDate()                           { return date;               }
    public long getTotalDuration()                       { return totalDuration;       }
    public Map<String, Long> getProjectDurations()       { return projectDurations;    }

    public long getDurationForProject(String projectName) {
        return projectDurations.getOrDefault(projectName, 0L);
    }

    @Override
    public String toString() {
        return "DailySummary{date=" + date + ", total=" + totalDuration + "s, projects=" + projectDurations + "}";
    }
}