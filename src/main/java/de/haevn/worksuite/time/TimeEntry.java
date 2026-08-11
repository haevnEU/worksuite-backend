package de.haevn.worksuite.time;

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
 * JPA entity representing a locally tracked work time booking record.
 *
 * <p>Example instantiation:
 * <pre>{@code
 * TimeEntry entry = TimeEntry.builder()
 *     .ticketId(4021L)
 *     .activityId(9L)
 *     .hours(2)
 *     .minutes(45)
 *     .date(Instant.now())
 *     .description("Implemented JWT verification")
 *     .build();
 * }</pre>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "time_entries")
@Schema(description = "Locally persisted time booking record")
public class TimeEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    @Schema(description = "Unique time entry identifier", example = "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d")
    private UUID id;

    @Column(name = "hours", nullable = false)
    @Schema(description = "Logged hours", example = "3")
    private int hours;

    @Column(name = "minutes", nullable = false)
    @Schema(description = "Logged minutes", example = "15")
    private int minutes;

    @Column(name = "date", nullable = false)
    @Schema(description = "Date timestamp of the work performed", example = "2026-08-17T00:00:00.000Z")
    private Instant date;

    @Column(name = "description", columnDefinition = "TEXT")
    @Schema(description = "Description or comment summarizing the logged work", example = "Bug fixing in PDF exporter")
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Schema(description = "Record creation timestamp", example = "2026-08-17T18:00:00.000Z")
    private Instant createdAt;

    @Column(name = "activity_id", nullable = false)
    @Schema(description = "Activity identifier", example = "9")
    private long activityId;

    @Column(name = "ticket_id", nullable = false)
    @Schema(description = "Associated ticket identifier", example = "4021")
    private long ticketId;

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