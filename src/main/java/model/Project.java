package model;


public class Project {

    private String name;
    private long totalDuration; 

    public Project(String name) {
        this.name = name;
        this.totalDuration = 0;
    }

    // ── Getters ──────────────────────────────────────────────

    public String getName() {
        return name;
    }

    public long getTotalDuration() {
        return totalDuration;
    }

    
    public void addDuration(long seconds) {
        totalDuration += seconds;
    }

    public void setTotalDuration(long seconds) {
        totalDuration = seconds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Project)) return false;
        Project other = (Project) o;
        return this.name.equals(other.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }
}
