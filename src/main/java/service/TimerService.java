package service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import model.Project;
import model.TimeEntry;

public class TimerService {

    private List<Project> projects;       // 来自 MyBag（B 写完后替换）
    private Project activeProject;        // 当前正在计时的项目
    private LocalDateTime startTime;      // 当前计时开始时间
    private boolean running;              // 是否正在计时

    public TimerService(List<Project> projects) {
        this.projects = projects;
        this.running = false;
    }

    // ── 计时控制 ──────────────────────────────────────────────

    /**
     * 开始计时
     * GUI 点 Start 时调用
     */
    public void start(String projectName) {
        if (running) return; // 防止重复点 Start
        activeProject = findProject(projectName);
        if (activeProject == null) return;
        startTime = LocalDateTime.now();
        running = true;
    }

    /**
     * 停止计时，生成 TimeEntry 存入 Project
     * GUI 点 Stop 时调用
     * 返回本次计时时长（秒），供 GUI 显示
     */
    public long stop() {
        if (!running || activeProject == null) return 0;

        LocalDateTime endTime = LocalDateTime.now();
        long duration = java.time.Duration.between(startTime, endTime).getSeconds();

        TimeEntry entry = new TimeEntry(startTime, endTime, duration);
        activeProject.addEntry(entry);

        running = false;
        long result = duration;
        activeProject = null;
        startTime = null;
        return result;
    }

    /**
     * 获取当前已计时的秒数（用于 GUI 实时更新秒表显示）
     * javax.swing.Timer 每秒调用一次
     */
    public long getElapsedSeconds() {
        if (!running || startTime == null) return 0;
        return java.time.Duration.between(startTime, LocalDateTime.now()).getSeconds();
    }

    // ── 手动补录 ──────────────────────────────────────────────

    /**
     * 手动补录一条过去的记录
     * GUI 的 ManualEntryDialog 提交时调用
     */
    public void addManualEntry(String projectName, LocalDate date, long durationSeconds) {
        Project project = findProject(projectName);
        if (project == null) return;
        TimeEntry entry = new TimeEntry(date, durationSeconds);
        project.addEntry(entry);
    }

    /**
     * 删除某个项目里的某条记录
     * 历史记录面板点删除时调用
     */
    public void removeEntry(String projectName, TimeEntry entry) {
        Project project = findProject(projectName);
        if (project == null) return;
        project.removeEntry(entry);
    }

    /**
     * 修改某条记录的时长
     * 历史记录面板点编辑时调用
     */
    public void editEntry(String projectName, TimeEntry oldEntry, long newDurationSeconds) {
        Project project = findProject(projectName);
        if (project == null) return;

        // 先算出时长差值，更新 project 的 totalDuration
        long diff = newDurationSeconds - oldEntry.getDuration();

        // 用 editIf 找到对应记录并替换
        TimeEntry updated = new TimeEntry(oldEntry.getDate(), newDurationSeconds);
        project.getEntries().editIf(e -> e == oldEntry, updated);

        // 手动更新 totalDuration（因为 editIf 不知道时长变了）
        project.setTotalDuration(Math.max(0, project.getTotalDuration() + diff));
    }

    // ── 项目管理 ──────────────────────────────────────────────

    public void addProject(String name) {
        if (findProject(name) != null) return; // 已存在
        projects.add(new Project(name));
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

    // ── 私有工具方法 ──────────────────────────────────────────

    private Project findProject(String name) {
        for (Project p : projects) {
            if (p.getName().equals(name)) return p;
        }
        return null;
    }
}