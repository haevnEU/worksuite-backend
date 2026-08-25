package de.haevn.worksuite.ticket.provider;

import de.haevn.redmine.api.InfoType;
import de.haevn.redmine.api.QueryParams;
import de.haevn.redmine.api.RedmineClient;
import de.haevn.redmine.api.RedmineException;
import de.haevn.redmine.model.Issue;
import de.haevn.redmine.model.RedmineInfoResponses;
import de.haevn.worksuite.config.UserContextHolder;
import de.haevn.worksuite.config.UserIntegrationContext;
import de.haevn.worksuite.ticket.TicketProviderType;
import de.haevn.worksuite.ticket.TicketStatus;
import de.haevn.worksuite.ticket.dtos.InfoResponse;
import de.haevn.worksuite.ticket.dtos.LogTimeRequest;
import de.haevn.worksuite.ticket.dtos.QaProtocolRequest;
import de.haevn.worksuite.ticket.dtos.Ticket;
import de.haevn.worksuite.time.TimeService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Redmine-specific implementation of the {@link TicketProvider} interface.
 *
 * <p>Handles issue queries, status transitions, checklist interactions, time bookings,
 * and QA protocol comments using the {@link RedmineClient}.
 */
@Slf4j
@Component
public class RedmineProvider implements TicketProvider {

    private static final int QA_STATUS_ID = 8;
    private static final int MERGE_REQUEST_CUSTOM_FIELD_ID = 1;
    private static final int DEFAULT_CHECKBOX_COUNT = 6;
    private static final String ISO_DATE_REGEX = "^\\d{4}-\\d{2}-\\d{2}$";

    private final String baseUrl;
    private final TimeService timeService;

    /**
     * Constructs the {@link RedmineProvider} with the configured base URL and {@link TimeService}.
     *
     * @param baseUrl the Redmine instance URL injected via {@code ${app.redmine.url}}
     * @param timeService local time tracking service
     */
    public RedmineProvider(@Value("${app.redmine.url}") final String baseUrl, final TimeService timeService) {
        if (!StringUtils.hasText(baseUrl)) {
            throw new IllegalArgumentException("Property 'app.redmine.url' must not be null or empty!");
        }
        this.baseUrl = baseUrl.trim();
        this.timeService = Objects.requireNonNull(timeService, "TimeService must not be null");
    }

    @Override
    public TicketProviderType getProviderType() {
        return TicketProviderType.REDMINE;
    }

    //@Cacheable(value = "assignedTickets")
    @Override
    public List<Ticket> fetch() {
        try {
            final RedmineClient client = getClient();
            final List<Issue> assignedIssues = client.getMyAssignedIssues(QueryParams.values());
            final List<Ticket> detailedTickets = new ArrayList<>(assignedIssues.size());

            for (final Issue issue : assignedIssues) {
                final Optional<Issue> detailed = client.getIssueById(issue.id(), QueryParams.values());
                detailedTickets.add(mapToTicket(detailed.orElse(issue)));
            }

            return detailedTickets;
        } catch (final RedmineException e) {
            log.error("Failed to fetch assigned tickets from Redmine", e);
            return Collections.emptyList();
        }
    }

    @Override
    public Optional<Ticket> getByIssuedId(final long id) {
        try {
            return getClient().getIssueById(id, QueryParams.values()).map(this::mapToTicket);
        } catch (final RedmineException e) {
            log.error("Failed to fetch ticket #{} from Redmine", id, e);
            return Optional.empty();
        }
    }

    @Override
    public void moveToStatus(final long ticketId, final TicketStatus status) {
        Objects.requireNonNull(status, "TicketStatus must not be null");
        try {
            getClient().moveToStatus(ticketId, status.getId());
        } catch (final RedmineException e) {
            log.error("Failed to move ticket #{} to status {}", ticketId, status, e);
            throw new RuntimeException("Failed to update ticket status in Redmine: " + e.getMessage(), e);
        }
    }

    @Override
    public void addComment(final long ticketId, final String comment) {
        if (!StringUtils.hasText(comment)) {
            return;
        }
        try {
            getClient().addComment(ticketId, comment.trim());
        } catch (final RedmineException e) {
            log.error("Failed to append comment to ticket #{}", ticketId, e);
            throw new RuntimeException("Failed to add comment in Redmine: " + e.getMessage(), e);
        }
    }

    @Override
    public void bookTime(final long ticketId, final int hours, final int minutes, final String description,
        final int activityId, final Optional<String> dateOpt) {
        final String date = dateOpt.filter(d -> d.matches(ISO_DATE_REGEX)).orElseGet(() -> LocalDate.now().toString());
        try {
            getClient().logTime(ticketId, hours, minutes, description, activityId, date);
        } catch (final RedmineException e) {
            log.error("Failed to log time for ticket #{}", ticketId, e);
            throw new RuntimeException("Failed to log time in Redmine: " + e.getMessage(), e);
        }
    }

    @Override
    public void createCheckboxes(final long ticketId) {
        try {
            final RedmineClient client = getClient();
            for (int i = 0; i < DEFAULT_CHECKBOX_COUNT; i++) {
                client.addChecklistItem(ticketId, "");
            }
        } catch (final RedmineException e) {
            log.error("Failed to initialize checkboxes for ticket #{}", ticketId, e);
            throw new RuntimeException("Failed to create checkboxes in Redmine: " + e.getMessage(), e);
        }
    }

    @Override
    public void tickCheckbox(final long ticketId, final int checkboxIndex, final boolean state) {
        try {
            getClient().tickCheckbox(ticketId, checkboxIndex, state);
        } catch (final RedmineException e) {
            log.error("Failed to update checkbox #{} on ticket #{}", checkboxIndex, ticketId, e);
            throw new RuntimeException("Failed to tick checkbox in Redmine: " + e.getMessage(), e);
        }
    }

    @Override
    public void moveToQs(final long id, final @Valid QaProtocolRequest data) {
        log.info("Moving ticket #{} to QS", id);
        final String qaComment = buildQAComment(data);
        try {
            getClient().moveToStatus(id, QA_STATUS_ID, qaComment);
        } catch (final RedmineException e) {
            log.error("Failed to transition ticket #{} to QS", id, e);
            throw new RuntimeException("Failed to move ticket to QS in Redmine: " + e.getMessage(), e);
        }
    }

    @Override
    public void addMergeRequestLink(final long id, final String mrLink) {
        try {
            getClient().setCustomField(id, mrLink, MERGE_REQUEST_CUSTOM_FIELD_ID);
        } catch (final RedmineException e) {
            log.error("Failed to set Merge Request custom field on ticket #{}", id, e);
            throw new RuntimeException("Failed to set MR custom field in Redmine: " + e.getMessage(), e);
        }
    }

    @Override
    public void bookTicket(final long id, final @Valid LogTimeRequest request) {
        try {
            getClient().logTime(id, request.hours(), request.minutes(), request.comment(), (int) request.activityId(),
                request.day());
            this.timeService.book(id, request);
        } catch (final RedmineException e) {
            log.error("Failed to log time against Redmine for ticket #{}", id, e);
            throw new RuntimeException("Failed to book ticket in Redmine: " + e.getMessage(), e);
        }
    }

    @Override
    public List<InfoResponse> getInfo(final InfoType infoType) {
        try {
            final List<RedmineInfoResponses.InfoResponse> responses = getClient().getInfo(infoType);
            return responses.stream().map(this::mapToInfoResponse).toList();
        } catch (final RedmineException e) {
            log.error("Failed to query metadata information from Redmine", e);
            return Collections.emptyList();
        }
    }

    /**
     * Maps a Redmine {@link Issue} model to the domain {@link Ticket} DTO.
     *
     * @param issue the raw Redmine issue entity
     * @return the normalized {@link Ticket} DTO
     */
    private Ticket mapToTicket(final Issue issue) {
        if (issue == null) {
            return null;
        }

        final InfoResponse statusResponse = issue.status() != null
            ? new InfoResponse(issue.status().id(), issue.status().name(), false)
            : new InfoResponse(0, "", false);
        final InfoResponse infoResponse = new InfoResponse(issue.tracker().id(), issue.tracker().name(), false);
        final InfoResponse priorityResponse = new InfoResponse(issue.priority().id(), issue.priority().name(), false);
        final InfoResponse project = new InfoResponse(issue.project().id(), issue.project().name(), false);
        return new Ticket(
            issue.id(),
            issue.subject(),
            issue.description(),
            issue.author() != null ? issue.author().name() : "",
            issue.assignedTo() != null ? issue.assignedTo().name() : "",
            project,
            issue.createdOn(),
            issue.updatedOn(),
            statusResponse,
            infoResponse,
            priorityResponse,
            issue.journals(),
            issue.customFields(),
            issue.attachments()
        );
    }

    /**
     * Maps a Redmine {@link RedmineInfoResponses.InfoResponse} to the domain {@link InfoResponse} DTO.
     *
     * @param info the raw Redmine info response
     * @return the normalized {@link InfoResponse} DTO
     */
    private InfoResponse mapToInfoResponse(final RedmineInfoResponses.InfoResponse info) {
        if (info == null) {
            return null;
        }

        boolean isDefault = false;
        if (info.isDefault() != null) {
            isDefault = info.isDefault();
        }

        return new InfoResponse(
            info.id(),
            info.name(),
            isDefault
        );
    }

    private RedmineClient getClient() {
        final UserIntegrationContext userCtx = UserContextHolder.getContext();
        final String activeApiKey =
            (userCtx != null && StringUtils.hasText(userCtx.redmineApiKey())) ? userCtx.redmineApiKey().trim() : "";

        if (!StringUtils.hasText(activeApiKey)) {
            log.error("No Redmine API key found in active UserContextHolder.");
            throw new IllegalStateException("Redmine API key is not configured for the active user.");
        }

        return RedmineClient.create(this.baseUrl, activeApiKey);
    }

    private String buildQAComment(final QaProtocolRequest data) {
        final StringBuilder builder = new StringBuilder();

        if (StringUtils.hasText(data.intro())) {
            builder.append("h2. Einleitung\n\n").append(data.intro().trim()).append("\n\n");
        }

        builder.append("h2. Pipeline Ausführung & Review Details\n\n");
        if (Boolean.TRUE.equals(data.pipelineSuccess())) {
            builder.append("*Pipeline Status:* %{color:green}ERFOLGREICH%\n");
        } else {
            builder.append("*Pipeline Status:* %{color:red}FEHLGESCHLAGEN%\n");
        }

        builder.append("*MR Status:* %{color:green}Approved%\n");
        if (Boolean.TRUE.equals(data.rebaseExecuted())) {
            builder.append("*Rebase Status:* %{color:green}Durchgeführt%\n\n");
        } else {
            builder.append("*Rebase Status:* %{color:red}Nicht durchgeführt%\n\n");
        }

        if (Boolean.FALSE.equals(data.pipelineSuccess()) && StringUtils.hasText(data.pipelineFailReason())) {
            builder.append("h2. Ursache der fehlgeschlagenen Pipeline\n\n").append(data.pipelineFailReason().trim())
                .append("\n\n");
        }

        if (isSectionActive(data.hasAcceptanceCriteria(), data.acceptanceCriteria())) {
            builder.append("h2. Akzeptanzkriterien\n\n");
            for (final String line : data.acceptanceCriteria().split("\\R")) {
                if (StringUtils.hasText(line)) {
                    final String trimmed = line.trim();
                    builder.append(trimmed.startsWith("*") ? trimmed : "* " + trimmed).append("\n");
                }
            }
            builder.append("\n");
        }

        if (isSectionActive(data.hasSideEffects(), data.sideEffects())) {
            builder.append("h2. Nebeneffekte\n\n").append(data.sideEffects().trim()).append("\n\n");
        }

        if (isSectionActive(data.hasChangedEndpoints(), data.changedEndpoints())) {
            builder.append("h2. Geänderte REST Endpunkte\n\n").append("<pre>\n").append(data.changedEndpoints().trim())
                .append("\n</pre>\n\n");
        }

        appendTestSetupSections(builder, data);
        return builder.toString().trim();
    }

    private void appendTestSetupSections(final StringBuilder builder, final QaProtocolRequest data) {
        final boolean hasTestSetup = isSectionActive(data.hasTestSetup(), data.testSetup());
        final boolean hasDatasets = isSectionActive(data.hasTestDatasets(), data.testDatasets());
        final boolean hasUnitTests = isSectionActive(data.hasUnitTests(), data.unitTests());

        if (hasTestSetup || hasDatasets || hasUnitTests) {
            builder.append("h2. Test Setup\n\n");

            if (hasTestSetup) {
                builder.append("*Ablauf*\n\n").append(data.testSetup().trim()).append("\n\n");
            }

            if (hasDatasets) {
                builder.append("*Dateien*\n\n").append("|_.Datei|_.Beschreibung|\n");
                for (final String dataset : data.testDatasets().split("\\R")) {
                    if (!StringUtils.hasText(dataset)) {
                        continue;
                    }
                    final String[] parts = dataset.split("\\|", 2);
                    final String file = parts[0].trim();
                    final String desc = parts.length > 1 ? parts[1].trim() : "";
                    builder.append("|").append(file).append("|").append(desc).append("|\n");
                }
                builder.append("\n");
            }

            if (hasUnitTests) {
                builder.append("*Unit Tests*\n\n");
                for (final String test : data.unitTests().split("\\R")) {
                    if (StringUtils.hasText(test)) {
                        final String trimmed = test.trim();
                        builder.append(trimmed.startsWith("*") ? trimmed : "* " + trimmed).append("\n");
                    }
                }
                builder.append("\n");
            }
        }
    }

    private boolean isSectionActive(final Boolean enabled, final String content) {
        return Boolean.TRUE.equals(enabled) && StringUtils.hasText(content);
    }
}