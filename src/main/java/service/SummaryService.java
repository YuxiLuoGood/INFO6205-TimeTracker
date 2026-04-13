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

/**
 * SummaryService coordinates the three analytical ADTs:
 *   - MyHashTable:     indexes daily summaries by date string key
 *   - MyPriorityQueue: maintains a real-time ranking of projects by duration
 *   - MyQuickSort:     produces a fully sorted project list for reports and AI analysis
 *
 * This service sits between the TimerService (data input) and the UI / AI / Calendar layers (data output).
 */
public class SummaryService {

    private final List<Project>                     projects;
    private final MyHashTable<String, DailySummary> dailyTable;    // key: "yyyy-MM-dd"
    private MyPriorityQueue<Project>                rankingQueue;  // max-heap by totalDuration
    private final MyQuickSort                       sorter;

    public SummaryService(List<Project> projects) {
        this.projects     = projects;
        this.dailyTable   = new MyHashTable<>();
        this.rankingQueue = new MyPriorityQueue<>(
                Comparator.comparingLong(Project::getTotalDuration));
        this.sorter       = new MyQuickSort();
    }

    // Record a new time entry 

    /**
     * Called after TimerService.stop() to update both ADTs with the new session.
     * Updates the DailySummary in MyHashTable and re-inserts the project into
     * MyPriorityQueue so the live ranking reflects the latest total duration.
     *
     * @param project the project that was timed
     * @param entry   the TimeEntry just created by the timer
     */
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

    //Rebuild a single date after manual entry or edit 

    /**
     * Recomputes the DailySummary for a given date by scanning all projects.
     * Called after a manual entry, edit, or delete so the HashTable stays accurate.
     *
     * @param date the date whose summary needs to be refreshed
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

    // Queries

    /**
     * Returns the DailySummary for the given date from MyHashTable.
     * O(1) average time. Returns null if no data exists for that date.
     *
     * @param date the date to look up
     */
    public DailySummary getDailySummary(LocalDate date) {
        return dailyTable.get(date.toString());
    }

    /**
     * Returns the Top N projects for today using MyPriorityQueue.
     * Extracts the N highest-priority items and re-inserts them to preserve the queue.
     * Used by TimerPanel to display the live ranking after each session.
     *
     * @param n number of top projects to return
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
        // Re-insert extracted projects to keep the queue intact
        for (Project p : temp) rankingQueue.insert(p);
        return top;
    }

    /**
     * Returns the Top N projects for a historical date using MyHashTable.
     * Retrieves the DailySummary for that date and sorts its entries by duration.
     * Used by TimerPanel when the user switches the ranking to a past date.
     *
     * @param date the historical date to query
     * @param n    number of top projects to return
     */
    public List<Project> getTopNByDate(LocalDate date, int n) {
        DailySummary summary = getDailySummary(date);
        if (summary == null) return new ArrayList<>();

        // Collect project name + duration pairs and sort descending
        List<String[]> list = new ArrayList<>();
        for (String name : summary.getProjectNames()) {
            list.add(new String[]{name,
                    String.valueOf(summary.getDurationForProject(name))});
        }
        list.sort((a, b) -> Long.compare(Long.parseLong(b[1]), Long.parseLong(a[1])));

        // Build temporary Project objects to carry name and duration to the UI
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
     * Returns all projects sorted by total duration in descending order using MyQuickSort.
     * Used when generating a full weekly report or building the AI analysis prompt.
     */
    public List<Project> getSortedProjects() {
        List<Project> list = new ArrayList<>(projects);
        sorter.sort(list, (a, b) ->
                Long.compare(b.getTotalDuration(), a.getTotalDuration()));
        return list;
    }

    /**
     * Returns DailySummary objects for the most recent N days.
     * Only days that have recorded data are included.
     * Used by AIService to build the habit analysis prompt.
     *
     * @param days number of past days to include
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

    // Rebuild ADTs from persisted data

    /**
     * Reconstructs MyHashTable and MyPriorityQueue from existing Project data.
     * Called once at startup after JsonStorage.load() restores the project list.
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
     * Rebuilds both MyPriorityQueue and all DailySummary entries in MyHashTable
     * after a project is deleted. This ensures the deleted project no longer
     * appears in any ranking or daily summary.
     */
    public void rebuildRanking() {
        // Rebuild the priority queue from the current project list
        rankingQueue = new MyPriorityQueue<>(
                Comparator.comparingLong(Project::getTotalDuration));
        for (Project p : projects) {
            if (p.getTotalDuration() > 0) rankingQueue.insert(p);
        }

        // Rebuild all daily summaries so deleted project data is removed
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