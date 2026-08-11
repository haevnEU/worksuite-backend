package de.haevn.worksuite.ticket;

import de.haevn.redmine.api.InfoType;
import de.haevn.worksuite.ticket.dtos.InfoResponse;
import de.haevn.worksuite.ticket.dtos.LogTimeRequest;
import de.haevn.worksuite.ticket.dtos.QaProtocolRequest;
import de.haevn.worksuite.ticket.dtos.Ticket;
import de.haevn.worksuite.ticket.provider.TicketProvider;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service orchestrating ticket management operations across pluggable {@link TicketProvider} implementations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketProviderRegistry ticketProviderRegistry;

    /**
     * Fetches all issues/tickets assigned to the authenticated user for the specified provider.
     */
    public List<Ticket> fetch(final TicketProviderType providerType) {
        final TicketProvider provider = ticketProviderRegistry.getTicketProvider(providerType);
        return provider.fetch();
    }

    /**
     * Retrieves a single ticket by its unique issue identifier for the specified provider.
     */
    public Optional<Ticket> getByIssuedId(final TicketProviderType providerType, final long id) {
        final TicketProvider provider = ticketProviderRegistry.getTicketProvider(providerType);
        return provider.getByIssuedId(id);
    }

    /**
     * Moves a ticket to a target workflow status.
     */
    public void moveToStatus(final TicketProviderType providerType, final long ticketId, final TicketStatus status) {
        final TicketProvider provider = ticketProviderRegistry.getTicketProvider(providerType);
        provider.moveToStatus(ticketId, status);
    }

    /**
     * Appends a comment to a ticket.
     */
    public void addComment(final TicketProviderType providerType, final long ticketId, final String comment) {
        final TicketProvider provider = ticketProviderRegistry.getTicketProvider(providerType);
        provider.addComment(ticketId, comment);
    }

    /**
     * Logs work hours and minutes directly onto a ticket.
     */
    public void bookTime(
        final TicketProviderType providerType,
        final long ticketId,
        final int hours,
        final int minutes,
        final String description,
        final int activityId,
        final Optional<String> dateOpt
    ) {
        final TicketProvider provider = ticketProviderRegistry.getTicketProvider(providerType);
        provider.bookTime(ticketId, hours, minutes, description, activityId, dateOpt);
    }

    /**
     * Initializes default checklist checkboxes on a ticket.
     */
    public void createCheckboxes(final TicketProviderType providerType, final long ticketId) {
        final TicketProvider provider = ticketProviderRegistry.getTicketProvider(providerType);
        provider.createCheckboxes(ticketId);
    }

    /**
     * Updates the checked state of a specific checklist item index.
     */
    public void tickCheckbox(
        final TicketProviderType providerType,
        final long ticketId,
        final int checkboxIndex,
        final boolean state
    ) {
        final TicketProvider provider = ticketProviderRegistry.getTicketProvider(providerType);
        provider.tickCheckbox(ticketId, checkboxIndex, state);
    }

    /**
     * Moves a ticket to Quality Assurance (QS) and formats a structured QA protocol comment.
     */
    public void moveToQs(final TicketProviderType providerType, final long id, final @Valid QaProtocolRequest data) {
        final TicketProvider provider = ticketProviderRegistry.getTicketProvider(providerType);
        provider.moveToQs(id, data);
    }

    /**
     * Attaches a Merge Request link to the ticket's custom field or reference.
     */
    public void addMergeRequestLink(final TicketProviderType providerType, final long id, final String mrLink) {
        final TicketProvider provider = ticketProviderRegistry.getTicketProvider(providerType);
        provider.addMergeRequestLink(id, mrLink);
    }

    /**
     * Logs work time against the ticket provider and triggers local sync.
     */
    public void bookTicket(final TicketProviderType providerType, final long id, final @Valid LogTimeRequest request) {
        final TicketProvider provider = ticketProviderRegistry.getTicketProvider(providerType);
        provider.bookTicket(id, request);
    }

    /**
     * Queries provider metadata catalogues for enumeration types and activities.
     */
    public List<InfoResponse> getInfo(final TicketProviderType providerType, final InfoType infoType) {
        final TicketProvider provider = ticketProviderRegistry.getTicketProvider(providerType);
        return provider.getInfo(infoType);
    }
}