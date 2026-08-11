package de.haevn.worksuite.weekly;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Data;

@Data
@Entity
@Table(name = "weekly_meetings")
public class WeeklyMeeting {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "title")
    private String title;

    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    @OneToMany(mappedBy = "weeklyMeeting", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DaySummary> daySummaries = new ArrayList<>();

    public void addDaySummary(DaySummary daySummary) {
        daySummaries.add(daySummary);
        daySummary.setWeeklyMeeting(this);
    }

    public void removeDaySummary(DaySummary daySummary) {
        daySummaries.remove(daySummary);
        daySummary.setWeeklyMeeting(null);
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }
}