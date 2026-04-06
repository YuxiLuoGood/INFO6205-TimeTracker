package service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import model.Project;
import model.TimeEntry;

public class TimerService {

    private List<Project> projects;
    private Project activeProject;
    private LocalDateTime startTime;
    private boolean running;

    public TimerService(List<Project> projects) {
        this.projects = projects;
        this.running  = false;
    }

    // ── 计时控制 ──────────────────────────────────────────────

    public void start(String projectName) {
        if (running) return;
        activeProject = findProject(projectName);
        if (activeProject == null) return;
        startTime = LocalDateTime.now();
        running   = true;
    }

    public long stop() {
        if (!running || activeProject == null) return 0;
        LocalDateTime endTime = LocalDateTime.now();
        long duration = java.time.Duration.between(startTime, endTime).getSeconds();
        TimeEntry entry = new TimeEntry(startTime, endTime, duration);
        activeProject.addEntry(entry);
        running       = false;
        activeProject = null;
        startTime     = null;
        return duration;
    }

    public long getElapsedSeconds() {
        if (!running || startTime == null) return 0;
        return java.time.Duration.between(startTime, LocalDateTime.now()).getSeconds();
    }

    // ── 手动补录 ──────────────────────────────────────────────

    public void addManualEntry(String projectName, LocalDate date, long durationSeconds) {
        Project project = findProject(projectName);
        if (project == null) return;
        project.addEntry(new TimeEntry(date, durationSeconds));
    }

    public void removeEntry(String projectName, TimeEntry entry) {
        Project project = findProject(projectName);
        if (project == null) return;
        project.removeEntry(entry);
    }

    public void editEntry(String projectName, TimeEntry oldEntry, long newDurationSeconds) {
        Project project = findProject(projectName);
        if (project == null) return;
        long diff    = newDurationSeconds - oldEntry.getDuration();
        TimeEntry updated = new TimeEntry(oldEntry.getDate(), newDurationSeconds);
        project.getEntries().editIf(e -> e == oldEntry, updated);
        project.setTotalDuration(Math.max(0, project.getTotalDuration() + diff));
    }

    // ── 项目管理 ──────────────────────────────────────────────

    public void addProject(String name) {
        if (findProject(name) != null) return;
        projects.add(new Project(name));
    }

    public void removeProject(String name) {
        projects.removeIf(p -> p.getName().equals(name));
    }

    public List<Project> getProjects() {
        return projects;
    }

    public boolean isRunning() {
        return running;
    }

    public String getActiveProjectName() {
        return activeProject == null ? null : activeProject.getName();
    }

    // ── 私有工具 ──────────────────────────────────────────────

    private Project findProject(String name) {
        for (Project p : projects) {
            if (p.getName().equals(name)) return p;
        }
        return null;
    }
}