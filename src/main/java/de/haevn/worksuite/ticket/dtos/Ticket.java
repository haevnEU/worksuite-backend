package de.haevn.worksuite.ticket.dtos;

import de.haevn.redmine.model.Attachment;
import de.haevn.redmine.model.CustomField;
import de.haevn.redmine.model.Journal;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * Data transfer object representing a standardized ticket across issue tracking providers.
 *
 * @param id the unique numerical identifier of the ticket
 * @param subject the title or summary of the ticket
 * @param description the full markdown/text description
 * @param status the current workflow status name (e.g. "In Progress", "Review")
 * @param author the display name of the ticket creator
 * @param assignedTo the display name of the assigned assignee
 * @param project the name of the project this ticket belongs to
 * @param createdOn the initial creation timestamp
 * @param updatedOn the last modification timestamp
 */
@Schema(description = "Represents a unified ticket item from a ticketing provider")
public record Ticket(
    @Schema(description = "Unique ticket ID", example = "4021")
    long id,

    @Schema(description = "Ticket subject or title", example = "Implement VCS integration")
    String subject,

    @Schema(description = "Detailed ticket description", example = "Add support for pluggable VCS providers")
    String description,

    @Schema(description = "Creator of the ticket", example = "Max Mustermann")
    String author,

    @Schema(description = "Assignee responsible for the ticket", example = "Erika Musterfrau")
    String assignedTo,

    @Schema(description = "Associated project name", example = "WorkSuite")
    InfoResponse project,

    @Schema(description = "Creation date of the ticket") Instant createdOn,

    @Schema(description = "Last update date of the ticket")
    Instant updatedOn,

    @Schema(description = "List of available activities for time tracking")
    InfoResponse tracker,

    @Schema(description = "List of available priorities for the ticket")
    InfoResponse priority,

    @Schema(description = "Current ticket status", example = "In Progress")
    InfoResponse status,


    // TODO remove redmine innerhitance
    List<Journal> journals,
    List<CustomField> customFields,
    List<Attachment> attachments
) {}