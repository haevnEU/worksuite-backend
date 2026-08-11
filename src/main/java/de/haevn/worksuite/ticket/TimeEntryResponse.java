package de.haevn.worksuite.ticket;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * Detailed representation of a recorded time entry returned by ticket operations.
 *
 * <p>Example usage:
 * <pre>{@code
 * TimeEntryResponse response = new TimeEntryResponse(
 *     1001L,
 *     4500L,
 *     "nils.milewski",
 *     LocalDate.of(2026, 8, 17),
 *     4,
 *     15,
 *     "Development",
 *     "Refactored exception hierarchy",
 *     OffsetDateTime.now()
 * );
 * }</pre>
 *
 * @param id primary unique identifier of the time entry
 * @param ticketId associated ticket identifier
 * @param userName user who logged the entry
 * @param day target date of the logged work
 * @param hours logged hours
 * @param minutes logged minutes
 * @param activityId activity name or numerical identifier
 * @param comment description of the activity
 * @param createdOn creation timestamp
 */
@Schema(description = "Time entry details recorded against a ticket")
public record TimeEntryResponse(

    @Schema(description = "Unique time entry ID", example = "10542") Long id,

    @Schema(description = "Associated ticket identifier", example = "4021") Long ticketId,

    @Schema(description = "User who logged the time", example = "nils.milewski") String userName,

    @Schema(description = "Date on which the work occurred", example = "2026-08-17") LocalDate day,

    @Schema(description = "Logged hours", example = "2") int hours,

    @Schema(description = "Logged minutes", example = "30") int minutes,

    @Schema(description = "Activity identifier or category name", example = "Development") String activityId,

    @Schema(description = "Comment detailing the work performed",
        example = "Code review and test fixes") String comment,

    @Schema(description = "Record creation timestamp",
        example = "2026-08-17T14:30:00+02:00") OffsetDateTime createdOn) {
}