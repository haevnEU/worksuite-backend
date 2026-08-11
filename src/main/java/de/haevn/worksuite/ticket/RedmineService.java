package de.haevn.worksuite.ticket;

import de.haevn.redmine.api.InfoType;
import de.haevn.redmine.api.QueryParams;
import de.haevn.redmine.api.RedmineClient;
import de.haevn.redmine.api.RedmineException;
import de.haevn.redmine.model.Issue;
import de.haevn.redmine.model.RedmineInfoResponses;
import de.haevn.worksuite.config.UserContextHolder;
import de.haevn.worksuite.config.UserIntegrationContext;
import de.haevn.worksuite.time.TimeService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Service encapsulating API interactions with Redmine, managing ticket queries, status transitions,
 * QA protocols, and time booking.
 *
 * <p>Example usage:
 * <pre>{@code
 * @Autowired
 * private RedmineService redmineService;
 *
 * List<Issue> myIssues = redmineService.fetch();
 * redmineService.moveToStatus(4021L, TicketStatus.REVIEW);
 * }</pre>
 */
@Slf4j
@Service
public class RedmineService {

    private static final int QA_STATUS_ID = 8;
    private static final int MERGE_REQUEST_CUSTOM_FIELD_ID = 1;
    private static final int DEFAULT_CHECKBOX_COUNT = 6;
    private static final String ISO_DATE_REGEX = "^\\d{4}-\\d{2}-\\d{2}$";

    private final String baseUrl;
    private final TimeService timeService;

    /**
     * Constructs the {@link RedmineService} with the configured base URL and {@link TimeService}.
     *
     * @param baseUrl the Redmine instance URL injected via {@code ${app.redmine.url}}
     * @param timeService local time tracking service
     */
    public RedmineService(@Value("${app.redmine.url}") final String baseUrl, final TimeService timeService) {
        if (!StringUtils.hasText(baseUrl)) {
            throw new IllegalArgumentException("Property 'app.redmine.url' must not be null or empty!");
        }
        this.baseUrl = baseUrl.trim();
        this.timeService = Objects.requireNonNull(timeService, "TimeService must not be null");
    }

    /**
     * Fetches all issues assigned to the currently authenticated user with complete metadata details.
     *
     * @return a thread-safe list of assigned {@link Issue} objects
     * @throws RedmineException if Redmine communication fails
     */
    public List<Issue> fetch() throws RedmineException {
        final RedmineClient client = getClient();
        final List<Issue> assignedIssues = client.getMyAssignedIssues(QueryParams.values());
        final List<Issue> detailedIssues = new ArrayList<>(assignedIssues.size());

        for (final Issue issue : assignedIssues) {
            final Optional<Issue> detailed = client.getIssueById(issue.id(), QueryParams.values());
            detailedIssues.add(detailed.orElse(issue));
        }

        return detailedIssues;
    }

    /**
     * Retrieves a single issue by its identifier.
     *
     * @param id ticket issue ID
     * @return an {@link Optional} holding the {@link Issue}, or empty if not found
     * @throws RedmineException if Redmine communication fails
     */
    public Optional<Issue> getByIssuedId(final long id) throws RedmineException {
        return getClient().getIssueById(id, QueryParams.values());
    }

    /**
     * Moves a ticket to a target workflow status.
     *
     * @param ticketId ticket issue ID
     * @param status target {@link TicketStatus}
     * @throws RedmineException if updating status fails
     */
    public void moveToStatus(final long ticketId, final TicketStatus status) throws RedmineException {
        Objects.requireNonNull(status, "TicketStatus must not be null");
        getClient().moveToStatus(ticketId, status.getId());
    }

    /**
     * Appends a journal comment to a ticket.
     *
     * @param ticketId ticket issue ID
     * @param comment comment markdown text
     * @throws RedmineException if updating the ticket fails
     */
    public void addComment(final long ticketId, final String comment) throws RedmineException {
        if (!StringUtils.hasText(comment)) {
            return;
        }
        getClient().addComment(ticketId, comment.trim());
    }

    /**
     * Logs work hours and minutes directly into Redmine.
     *
     * @param ticketId ticket issue ID
     * @param hours logged hours
     * @param minutes logged minutes
     * @param description work description
     * @param activityId activity category ID
     * @param dateOpt optional ISO formatted date (defaults to today)
     * @throws RedmineException if logging time fails
     */
    public void bookTime(final long ticketId, final int hours, final int minutes, final String description,
        final int activityId, final Optional<String> dateOpt) throws RedmineException {
        final String date = dateOpt.filter(d -> d.matches(ISO_DATE_REGEX)).orElseGet(() -> LocalDate.now().toString());

        getClient().logTime(ticketId, hours, minutes, description, activityId, date);
    }

    /**
     * Initializes default checklist checkboxes on a ticket.
     *
     * @param ticketId ticket issue ID
     * @throws RedmineException if creating checklist items fails
     */
    public void createCheckboxes(final long ticketId) throws RedmineException {
        final RedmineClient client = getClient();
        for (int i = 0; i < DEFAULT_CHECKBOX_COUNT; i++) {
            client.addChecklistItem(ticketId, "");
        }
    }

    /**
     * Updates the checked state of a specific checklist item index.
     *
     * @param ticketId ticket issue ID
     * @param checkboxIndex 0-based index of the checklist item
     * @param state {@code true} for checked, {@code false} for unchecked
     * @throws RedmineException if updating the checkbox fails
     */
    public void tickCheckbox(final long ticketId, final int checkboxIndex, final boolean state)
        throws RedmineException {
        getClient().tickCheckbox(ticketId, checkboxIndex, state);
    }

    /**
     * Moves a ticket to Quality Assurance (QS) and formats a structured QA protocol comment.
     *
     * @param id ticket issue ID
     * @param data validated QA protocol payload
     * @throws RedmineException if updating status or adding the comment fails
     */
    public void moveToQs(final long id, final @Valid QaProtocolRequest data) throws RedmineException {
        log.info("Moving ticket #{} to QS", id);
        final String qaComment = buildQAComment(data);
        getClient().moveToStatus(id, QA_STATUS_ID, qaComment);
    }

    /**
     * Attaches a Merge Request link to the ticket's custom field.
     *
     * @param id ticket issue ID
     * @param mrLink target Merge Request URL
     * @throws RedmineException if setting the custom field fails
     */
    public void addMergeRequestLink(final long id, final String mrLink) throws RedmineException {
        getClient().setCustomField(id, mrLink, MERGE_REQUEST_CUSTOM_FIELD_ID);
    }

    /**
     * Logs work time against Redmine and synchronizes it into the local database via {@link TimeService}.
     *
     * @param id ticket issue ID
     * @param request validated {@link LogTimeRequest} payload
     * @throws RedmineException if Redmine logging fails
     */
    public void bookTicket(final long id, final @Valid LogTimeRequest request) throws RedmineException {
        getClient().logTime(id, request.hours(), request.minutes(), request.comment(), (int) request.activityId(),
            request.day());
        this.timeService.book(id, request);
    }

    /**
     * Queries Redmine metadata catalogues for enumeration types.
     *
     * @param infoType the target {@link InfoType}
     * @return list of {@link RedmineInfoResponses.InfoResponse} entries
     * @throws RedmineException if querying metadata fails
     */
    public List<RedmineInfoResponses.InfoResponse> getInfo(final InfoType infoType) throws RedmineException {
        return getClient().getInfo(infoType);
    }

    /**
     * Resolves the {@link RedmineClient} using credentials from {@link UserContextHolder}.
     *
     * <p>Example usage:
     * <pre>{@code
     * RedmineClient client = getClient();
     * }</pre>
     *
     * @return configured {@link RedmineClient}
     * @throws IllegalStateException if no API key is available in the current user context
     */
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

    /**
     * Compiles a Textile-formatted protocol comment for the Quality Assurance team.
     *
     * <p>Example usage:
     * <pre>{@code
     * String comment = buildQAComment(qaProtocolRequest);
     * }</pre>
     *
     * @param data the QA protocol input data
     * @return formatted Textile string
     */
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

    /**
     * Appends test setup, dataset tables, and unit test sections to the QA protocol comment.
     *
     * <p>Example usage:
     * <pre>{@code
     * appendTestSetupSections(builder, data);
     * }</pre>
     *
     * @param builder destination {@link StringBuilder}
     * @param data the QA protocol data
     */
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

    /**
     * Evaluates if a section flag is set to true and has non-blank textual content.
     *
     * <p>Example usage:
     * <pre>{@code
     * boolean active = isSectionActive(true, "Content");
     * }</pre>
     *
     * @param enabled flag indicating whether section is included
     * @param content textual content
     * @return {@code true} if active and populated
     */
    private boolean isSectionActive(final Boolean enabled, final String content) {
        return Boolean.TRUE.equals(enabled) && StringUtils.hasText(content);
    }
}