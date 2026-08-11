package de.haevn.worksuite.stats;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JPA entity representing aggregated daily activity metrics and logged hours.
 *
 * <p>Example instantiation:
 * <pre>{@code
 * Stats dailyStats = Stats.builder()
 *     .day(Instant.now())
 *     .movedToQa(3)
 *     .movedToReview(2)
 *     .returnFromQa(0)
 *     .returnFromReview(1)
 *     .hoursSpent(8)
 *     .build();
 * }</pre>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "stats")
@Schema(description = "Aggregated daily developer activity metrics and hours")
public class Stats {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    @Schema(description = "Unique statistics record identifier", example = "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d")
    private UUID id;

    @Column(name = "moved_to_qa", nullable = false)
    @Schema(description = "Count of tickets moved to QA", example = "4")
    private int movedToQa;

    @Column(name = "moved_to_review", nullable = false)
    @Schema(description = "Count of tickets moved to review", example = "2")
    private int movedToReview;

    @Column(name = "return_from_qa", nullable = false)
    @Schema(description = "Count of tickets returned from QA", example = "1")
    private int returnFromQa;

    @Column(name = "return_from_review", nullable = false)
    @Schema(description = "Count of tickets returned from review", example = "0")
    private int returnFromReview;

    @Column(name = "hours_spent", nullable = false)
    @Schema(description = "Total hours logged on tasks for this day", example = "8")
    private int hoursSpent;

    @Column(name = "day", nullable = false)
    @Schema(description = "Target reference day timestamp", example = "2026-08-17T00:00:00.000Z")
    private Instant day;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Schema(description = "Record creation instant", example = "2026-08-17T18:00:00.000Z")
    private Instant createdAt;

    /**
     * Lifecycle callback initializing the creation timestamp prior to persistence.
     */
    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
    }
}