package de.haevn.worksuite.ticket;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Standard ticket status enumeration with corresponding Redmine workflow status identifiers.
 *
 * <p>Example usage:
 * <pre>{@code
 * TicketStatus status = TicketStatus.REVIEW;
 * long statusId = status.getId();
 * }</pre>
 */
@Schema(description = "Redmine ticket lifecycle status mapping")
public enum TicketStatus {

    @Schema(description = "Ticket is queued in the backlog (Status ID: 18)") BACKLOG(18),

    @Schema(description = "Ticket is currently in active development (Status ID: 2)") PROGRESS(2),

    @Schema(description = "Ticket is in code review / merge request stage (Status ID: 19)") REVIEW(19),

    @Schema(description = "Ticket is ready for or undergoing quality assurance (Status ID: 8)") QA(8);

    private final long id;

    TicketStatus(final long id) {
        this.id = id;
    }

    /**
     * Retrieves the numerical Redmine status identifier.
     *
     * @return status identifier number
     */
    public long getId() {
        return this.id;
    }
}