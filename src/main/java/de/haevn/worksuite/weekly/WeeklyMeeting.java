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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JPA entity representing a weekly sprint meeting protocol with nested daily breakdown summaries.
 *
 * <p>Example instantiation:
 * <pre>{@code
 * WeeklyMeeting meeting = WeeklyMeeting.builder()
 *     .title("Weekly Sprint (11.08.2026 - 18.08.2026)")
 *     .summary("Overall successful sprint with all deliverables met.")
 *     .daySummaries(new ArrayList<>())
 *     .build();
 * }</pre>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "weekly_meetings")
public class WeeklyMeeting {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "title")
    private String title;

    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    @Builder.Default
    @OneToMany(mappedBy = "weeklyMeeting", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DaySummary> daySummaries = new ArrayList<>();

    /**
     * Convenience helper method to add a {@link DaySummary} and synchronize the bidirectional association.
     *
     * <p>Example:
     * <pre>{@code
     * meeting.addDaySummary(daySummary);
     * }</pre>
     *
     * @param daySummary the child day summary to attach
     */
    public void addDaySummary(final DaySummary daySummary) {
        if (daySummary != null) {
            daySummaries.add(daySummary);
            daySummary.setWeeklyMeeting(this);
        }
    }

    /**
     * Convenience helper method to remove a {@link DaySummary} and clear the relationship.
     *
     * <p>Example:
     * <pre>{@code
     * meeting.removeDaySummary(daySummary);
     * }</pre>
     *
     * @param daySummary the child day summary to detach
     */
    public void removeDaySummary(final DaySummary daySummary) {
        if (daySummary != null) {
            daySummaries.remove(daySummary);
            daySummary.setWeeklyMeeting(null);
        }
    }

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