package model;

import adt.ListInterface;
import adt.MyLinkedList;

import java.util.List;

public class Project {

    private String name;
    private ListInterface<TimeEntry> entries;  // 用接口类型，方便以后替换
    private long totalDuration;                // 单位：秒

    public Project(String name) {
        this.name = name;
        this.entries = new MyLinkedList<>();   
        this.totalDuration = 0;
    }

    // ── 添加一条计时记录 ──────────────────────────────────────
    public void addEntry(TimeEntry entry) {
        entries.addLast(entry);
        totalDuration += entry.getDuration();
    }

    // ── 删除一条计时记录（手动删除历史时用）─────────────────────
    public void removeEntry(TimeEntry entry) {
        boolean removed = entries.removeIf(e -> e == entry);
        if (removed) {
            totalDuration = Math.max(0, totalDuration - entry.getDuration());
        }
    }

    // ── Getters ──────────────────────────────────────────────
    public String getName()                        { return name;          }
    public long getTotalDuration()                 { return totalDuration; }
    public ListInterface<TimeEntry> getEntries()   { return entries;       }
    public List<TimeEntry> getEntriesAsList()      { return entries.toList(); }

    // ── 持久化用：直接设置总时长（JsonStorage 加载时用）──────────
    public void setTotalDuration(long seconds) {
        this.totalDuration = seconds;
    }

    // ── 标准方法 ──────────────────────────────────────────────
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