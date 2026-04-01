package model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class TimeEntry {

    private LocalDateTime startTime; 
    private LocalDateTime endTime;   
    private long duration;       
    private LocalDate date;      
    private boolean isManual;      

   
    public TimeEntry(LocalDateTime startTime, LocalDateTime endTime, long duration) {
        this.startTime = startTime;
        this.endTime   = endTime;
        this.duration  = duration;
        this.date      = startTime.toLocalDate(); 
        this.isManual  = false;
    }

    public TimeEntry(LocalDate date, long duration) {
        this.startTime = null;
        this.endTime   = null;
        this.duration  = duration;
        this.date      = date;
        this.isManual  = true;
    }

    //  Getters

    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime()   { return endTime;   }
    public long getDuration()           { return duration;  }
    public LocalDate getDate()          { return date;      }
    public boolean isManual()           { return isManual;  }

    //  Setter 

    public void setDuration(long duration) {
        this.duration = duration;
    }

    @Override
    public String toString() {
        if (isManual) {
            return "[Manual] " + date + " | " + formatDuration(duration);
        } else {
            return startTime + " → " + endTime + " | " + formatDuration(duration);
        }
    }

    private String formatDuration(long totalSeconds) {
        long h = totalSeconds / 3600;
        long m = (totalSeconds % 3600) / 60;
        long s = totalSeconds % 60;
        if (h > 0) return h + "h " + m + "m " + s + "s";
        if (m > 0) return m + "m " + s + "s";
        return s + "s";
    }
}
