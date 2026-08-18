package de.haevn.worksuite.ticket.provider;

import de.haevn.redmine.api.InfoType;
import de.haevn.worksuite.ticket.dtos.LogTimeRequest;
import de.haevn.worksuite.ticket.dtos.QaProtocolRequest;
import de.haevn.worksuite.ticket.TicketProviderType;
import de.haevn.worksuite.ticket.TicketStatus;
import de.haevn.worksuite.ticket.dtos.InfoResponse;
import de.haevn.worksuite.ticket.dtos.Ticket;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

/**
 * Common abstraction contract for pluggable ticket and issue tracking providers (e.g. Redmine, Jira).
 *
 * <p>Example usage:
 * <pre>{@code
 * TicketProvider provider = ticketProviderRegistry.getProvider(TicketProviderType.REDMINE);
 * List<Ticket> myTickets = provider.fetch();
 * provider.moveToStatus(4021L, TicketStatus.REVIEW);
 * }</pre>
 */
public interface TicketProvider {

    /**
     * Identifies the specific ticket system provider handled by this implementation.
     *
     * @return the associated {@link TicketProviderType} identifier
     */
    TicketProviderType getProviderType();

    /**
     * Fetches all issues/tickets assigned to the authenticated user.
     *
     * @return a list of assigned {@link Ticket} objects
     */
    List<Ticket> fetch();

    /**
     * Retrieves a single ticket by its unique issue identifier.
     *
     * @param id ticket issue ID
     * @return an {@link Optional} holding the {@link Ticket}, or empty if not found
     */
    Optional<Ticket> getByIssuedId(long id);

    /**
     * Moves a ticket to a target workflow status.
     *
     * @param ticketId ticket issue ID
     * @param status target {@link TicketStatus}
     */
    void moveToStatus(long ticketId, TicketStatus status);

    /**
     * Appends a journal or discussion comment to a ticket.
     *
     * @param ticketId ticket issue ID
     * @param comment comment markdown/text content
     */
    void addComment(long ticketId, String comment);

    /**
     * Logs work hours and minutes directly onto a ticket.
     *
     * @param ticketId ticket issue ID
     * @param hours logged hours
     * @param minutes logged minutes
     * @param description work description
     * @param activityId activity category ID
     * @param dateOpt optional ISO formatted date (YYYY-MM-DD)
     */
    void bookTime(long ticketId, int hours, int minutes, String description,
        int activityId, Optional<String> dateOpt);

    /**
     * Initializes default checklist checkboxes on a ticket.
     *
     * @param ticketId ticket issue ID
     */
    void createCheckboxes(long ticketId);

    /**
     * Updates the checked state of a specific checklist item index.
     *
     * @param ticketId ticket issue ID
     * @param checkboxIndex 0-based index of the checklist item
     * @param state {@code true} for checked, {@code false} for unchecked
     */
    void tickCheckbox(long ticketId, int checkboxIndex, boolean state);

    /**
     * Moves a ticket to Quality Assurance (QS) and formats a structured QA protocol comment.
     *
     * @param id ticket issue ID
     * @param data validated QA protocol payload
     */
    void moveToQs(long id, @Valid QaProtocolRequest data);

    /**
     * Attaches a Merge Request link to the ticket's designated custom field or reference field.
     *
     * @param id ticket issue ID
     * @param mrLink target Merge Request URL
     */
    void addMergeRequestLink(long id, String mrLink);

    /**
     * Logs work time against the ticket provider.
     *
     * @param id ticket issue ID
     * @param request validated {@link LogTimeRequest} payload
     */
    void bookTicket(long id, @Valid LogTimeRequest request);

    /**
     * Queries provider metadata catalogues for enumeration types and activities.
     *
     * @return list of {@link InfoResponse} entries
     */
    List<InfoResponse> getInfo(InfoType infoType);
}