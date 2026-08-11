package de.haevn.worksuite.time;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Data transfer object representing a logged work time entry.
 *
 * <p>Example usage:
 * <pre>{@code
 * TimeDTO dto = new TimeDTO(
 *     UUID.randomUUID(),
 *     2,
 *     30,
 *     Instant.now(),
 *     "Code review and refactoring",
 *     Instant.now(),
 *     9L,
 *     4021L
 * );
 * }</pre>
 *
 * @param id primary unique identifier of the time entry
 * @param hours logged whole hours
 * @param minutes logged minutes (0-59)
 * @param date timestamp indicating the target date of the logged work
 * @param description comment or summary of the completed tasks
 * @param createdAt record creation timestamp
 * @param activityId numerical Redmine activity category ID
 * @param ticketId associated Redmine ticket ID
 */
@Schema(description = "Data transfer object representing a recorded time entry")
public record TimeDTO(

    @Schema(description = "Unique time entry identifier", example = "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d") UUID id,

    @Schema(description = "Logged whole hours", example = "2") int hours,

    @Schema(description = "Logged minutes", example = "30") int minutes,

    @Schema(description = "Target date timestamp of the work performed",
        example = "2026-08-17T00:00:00.000Z") Instant date,

    @Schema(description = "Comment or summary of the completed work",
        example = "Refactored exception handling and added Swagger annotations.") String description,

    @Schema(description = "Creation instant of the time entry record",
        example = "2026-08-17T18:00:00.000Z") Instant createdAt,

    @Schema(description = "Activity category identifier", example = "9") long activityId,

    @Schema(description = "Associated ticket identifier", example = "4021") long ticketId) {

    /**
     * Constructs a {@link TimeDTO} by extracting attributes from a {@link TimeEntry} entity.
     *
     * <p>Example:
     * <pre>{@code
     * TimeDTO dto = new TimeDTO(timeEntryEntity);
     * }</pre>
     *
     * @param timeEntry the source {@link TimeEntry} entity
     */
    public TimeDTO(final TimeEntry timeEntry) {
        this(Objects.requireNonNull(timeEntry, "TimeEntry entity must not be null").getId(), timeEntry.getHours(),
            timeEntry.getMinutes(), timeEntry.getDate(), timeEntry.getDescription(), timeEntry.getCreatedAt(),
            timeEntry.getActivityId(), timeEntry.getTicketId());
    }
}