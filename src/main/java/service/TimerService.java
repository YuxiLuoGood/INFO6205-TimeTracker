package service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import model.Project;
import model.TimeEntry;

/**
 * TimerService handles all timer operations and project management.
 * It is the primary data-entry point of the application — every TimeEntry
 * is created here and stored in the corresponding Project's MyLinkedList.
 *
 * This service is called directly by the UI layer (TimerPanel, HistoryPanel)
 * and works alongside SummaryService, which maintains the analytical ADTs.
 */
public class TimerService {

    private List<Project> projects;       // backed by MyBag in the UI layer
    private Project activeProject;        // the project currently being timed
    private LocalDateTime startTime;      // timestamp when the timer was started
    private boolean running;              // true while a session is in progress

    public TimerService(List<Project> projects) {
        this.projects = projects;
        this.running  = false;
    }

    //Timer control 

    /**
     * Starts a timing session for the given project.
     * Records the current time as the session start.
     * Does nothing if a session is already running.
     *
     * @param projectName name of the project to time
     */
    public void start(String projectName) {
        if (running) return;
        activeProject = findProject(projectName);
        if (activeProject == null) return;
        startTime = LocalDateTime.now();
        running   = true;
    }

    /**
     * Stops the current timing session and saves the result.
     * Calculates the elapsed duration, creates a TimeEntry, and appends it
     * to the active project's MyLinkedList via addEntry().
     *
     * @return the session duration in seconds, or 0 if no session was running
     */
    public long stop() {
        if (!running || activeProject == null) return 0;
        LocalDateTime endTime = LocalDateTime.now();
        long duration = java.time.Duration.between(startTime, endTime).getSeconds();
        TimeEntry entry = new TimeEntry(startTime, endTime, duration);
        activeProject.addEntry(entry);   // appends to MyLinkedList, O(1)
        running       = false;
        activeProject = null;
        startTime     = null;
        return duration;
    }

    /**
     * Returns the number of seconds elapsed since the timer was started.
     * Called every second by javax.swing.Timer in TimerPanel to update
     * the stopwatch display without blocking the UI thread.
     *
     * @return elapsed seconds, or 0 if the timer is not running
     */
    public long getElapsedSeconds() {
        if (!running || startTime == null) return 0;
        return java.time.Duration.between(startTime, LocalDateTime.now()).getSeconds();
    }

    //Manual entry

    /**
     * Adds a past session that the user forgot to track in real time.
     * Creates a TimeEntry with isManual=true and no start/end timestamps.
     * Called when the user submits the ManualEntryDialog.
     *
     * @param projectName     the project to add the entry to
     * @param date            the date the session took place
     * @param durationSeconds the duration of the session in seconds
     */
    public void addManualEntry(String projectName, LocalDate date, long durationSeconds) {
        Project project = findProject(projectName);
        if (project == null) return;
        project.addEntry(new TimeEntry(date, durationSeconds));
    }

    /**
     * Removes a specific TimeEntry from a project's MyLinkedList.
     * Uses MyLinkedList.removeIf() to locate and unlink the target node.
     * Called when the user clicks Delete in the HistoryPanel.
     *
     * @param projectName the project that owns the entry
     * @param entry       the TimeEntry object to remove
     */
    public void removeEntry(String projectName, TimeEntry entry) {
        Project project = findProject(projectName);
        if (project == null) return;
        project.removeEntry(entry);
    }

    /**
     * Replaces an existing TimeEntry with an updated version.
     * Uses MyLinkedList.editIf() to locate the old entry by reference and swap it.
     * Also adjusts the project's totalDuration by the difference in seconds.
     * Called when the user edits a record's duration in the HistoryPanel.
     *
     * @param projectName        the project that owns the entry
     * @param oldEntry           the existing TimeEntry to replace
     * @param newDurationSeconds the corrected duration in seconds
     */
    public void editEntry(String projectName, TimeEntry oldEntry, long newDurationSeconds) {
        Project project = findProject(projectName);
        if (project == null) return;
        long diff = newDurationSeconds - oldEntry.getDuration();
        TimeEntry updated = new TimeEntry(oldEntry.getDate(), newDurationSeconds);
        project.getEntries().editIf(e -> e == oldEntry, updated);
        project.setTotalDuration(Math.max(0, project.getTotalDuration() + diff));
    }

    //Project management

    /**
     * Creates a new Project and adds it to the project list.
     * Does nothing if a project with the same name already exists.
     *
     * @param name the name of the new project
     */
    public void addProject(String name) {
        if (findProject(name) != null) return;
        projects.add(new Project(name));
    }

    /**
     * Removes a project and all its time entries from the project list.
     * The caller (TimerPanel) is responsible for calling
     * SummaryService.rebuildRanking() afterwards to keep the ADTs consistent.
     *
     * @param name the name of the project to remove
     */
    public void removeProject(String name) {
        projects.removeIf(p -> p.getName().equals(name));
    }

    /**
     * Returns the full list of projects.
     * Used by the UI to populate the project dropdown and history table.
     */
    public List<Project> getProjects() {
        return projects;
    }

    /**
     * Returns true if a timing session is currently in progress.
     * Used by the UI to disable certain controls while timing.
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Returns the name of the project currently being timed, or null if idle.
     */
    public String getActiveProjectName() {
        return activeProject == null ? null : activeProject.getName();
    }

    // Private helper

    /**
     * Finds a Project by name using linear search.
     * Returns null if no match is found.
     *
     * @param name the project name to search for
     */
    private Project findProject(String name) {
        for (Project p : projects) {
            if (p.getName().equals(name)) return p;
        }
        return null;
    }
}