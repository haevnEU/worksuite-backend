package de.haevn.worksuite.weekly;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * JPA entity representing a single day's summary and associated task entries within a weekly sprint meeting.
 *
 * <p>Example instantiation:
 * <pre>{@code
 * DaySummary day = DaySummary.builder()
 *     .date(Instant.now())
 *     .summary("Completed PR reviews and tested SSO login flow.")
 *     .tasks(new ArrayList<>(List.of("Review PR #42", "Deploy to staging")))
 *     .build();
 * }</pre>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "day_summaries")
public class DaySummary {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "weekly_meeting_id", nullable = false)
    @JsonIgnore
    @ToString.Exclude
    private WeeklyMeeting weeklyMeeting;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "date")
    private Instant date;

    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    @Builder.Default
    @ElementCollection
    @CollectionTable(name = "day_summary_tasks", joinColumns = @JoinColumn(name = "day_summary_id"))
    @Column(name = "task")
    private List<String> tasks = new ArrayList<>();

    /**
     * Pre-persist lifecycle callback initializing the creation timestamp.
     */
    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
    }
}