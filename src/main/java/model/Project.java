package model;

import adt.ListInterface;
import adt.MyLinkedList;

import java.util.List;

public class Project {

    private String name;
    private ListInterface<TimeEntry> entries;  
    private long totalDuration;                // 单位：秒

    public Project(String name) {
        this.name = name;
        this.entries = new MyLinkedList<>();   
        this.totalDuration = 0;
    }

    // Add a time entry
    public void addEntry(TimeEntry entry) {
        entries.addLast(entry);
        totalDuration += entry.getDuration();
    }

    // Delete a time entry (for manually deleting history)
    public void removeEntry(TimeEntry entry) {
        boolean removed = entries.removeIf(e -> e == entry);
        if (removed) {
            totalDuration = Math.max(0, totalDuration - entry.getDuration());
        }
    }

    
    public String getName()                        { return name;          }
    public long getTotalDuration()                 { return totalDuration; }
    public ListInterface<TimeEntry> getEntries()   { return entries;       }
    public List<TimeEntry> getEntriesAsList()      { return entries.toList(); }

    // Persistence: Set the total duration directly (used when loading from JsonStorage)
    public void setTotalDuration(long seconds) {
        this.totalDuration = seconds;
    }

    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Project)) return false;
        return this.name.equals(((Project) o).name);
    }

    @Override
    public int hashCode() { return name.hashCode(); }

    @Override
    public String toString() { return name; }
}