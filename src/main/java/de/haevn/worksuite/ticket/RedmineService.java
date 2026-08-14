package de.haevn.worksuite.ticket;

import de.haevn.redmine.api.InfoType;
import de.haevn.redmine.api.QueryParams;
import de.haevn.redmine.api.RedmineClient;
import de.haevn.redmine.api.RedmineException;
import de.haevn.redmine.model.Issue;
import de.haevn.redmine.model.RedmineInfoResponses;
import de.haevn.worksuite.common.UserContextHolder;
import de.haevn.worksuite.common.UserIntegrationContext;
import de.haevn.worksuite.time.TimeService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RedmineService {

    private final String baseUrl;
    private final TimeService timeService;

    public RedmineService(@Value("${app.redmine.url}") final String baseUrl, final TimeService timeService) {
        this.timeService = timeService;
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalArgumentException("Property 'app.redmine.url' must not be null or empty!");
        }
        this.baseUrl = baseUrl;
    }


    private RedmineClient getClient() {
        final UserIntegrationContext userCtx = UserContextHolder.getContext();
        final String activeApiKey =
            (userCtx != null && isNotBlank(userCtx.redmineApiKey())) ? userCtx.redmineApiKey() : "";

        if (!isNotBlank(activeApiKey)) {
            log.error("No Redmine API key available (neither in UserContext nor in fallback properties)");
            throw new IllegalStateException("Redmine API key is not configured for the active user.");
        }

        return RedmineClient.create(this.baseUrl, activeApiKey);
    }

    public List<Issue> fetch() throws RedmineException {
        final RedmineClient client = getClient();
        final List<Issue> assignedIssues = client.getMyAssignedIssues(QueryParams.values());

        // Lokale Liste statt instanzweitem Feld -> 100% thread-safe
        final List<Issue> detailedIssues = new ArrayList<>(assignedIssues.size());

        for (final Issue issue : assignedIssues) {
            final Optional<Issue> detailed = client.getIssueById(issue.id(), QueryParams.values());
            detailedIssues.add(detailed.orElse(issue));
        }

        return detailedIssues;
    }

    public Optional<Issue> getByIssuedId(final long id) throws RedmineException {
        return getClient().getIssueById(id, QueryParams.values());
    }

    public void moveToStatus(final long ticketId, final TicketStatus status) throws RedmineException {
        getClient().moveToStatus(ticketId, status.id);
    }

    public void addComment(final long ticketId, final String comment) throws RedmineException {
        getClient().addComment(ticketId, comment);
    }

    public void bookTime(final long ticketId, final int hours, final int minutes, final String description,
        final int activityId, final Optional<String> dateOpt) throws RedmineException {
        final String date =
            dateOpt.filter(d -> d.matches("^\\d{4}-\\d{2}-\\d{2}$")).orElseGet(() -> LocalDate.now().toString());

        getClient().logTime(ticketId, hours, minutes, description, activityId, date);
    }

    public void createCheckboxes(final long ticketId) throws RedmineException {
        final RedmineClient client = getClient();
        for (int i = 0; i < 6; i++) {
            client.addChecklistItem(ticketId, "");
        }
    }

    public void tickCheckbox(final long ticketId, final int checkboxIndex, final boolean state)
        throws RedmineException {
        getClient().tickCheckbox(ticketId, checkboxIndex, state);
    }

    public void moveToQs(final long id, final @Valid QaProtocolRequest data) throws RedmineException {
        log.info("Moving ticket #{} to QS", id);
        getClient().moveToStatus(id, 8, buildQAComment(data));
    }

    public void addMergeRequestLink(final long id, final String mrLink) throws RedmineException {
        getClient().setCustomField(id, mrLink, 1);
    }

    public void bookTicket(final long id, final @Valid LogTimeRequest request) throws RedmineException {
        getClient().logTime(id, request.hours(), request.minutes(), request.comment(), request.activityId(),
            request.day());
        this.timeService.book(id, request);
    }

    public List<RedmineInfoResponses.InfoResponse> getInfo(final InfoType infoType) throws RedmineException {
        return getClient().getInfo(infoType);
    }

    // --- Private Helper Methods ---

    private String buildQAComment(final QaProtocolRequest data) {
        final StringBuilder builder = new StringBuilder();

        if (isNotBlank(data.intro())) {
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

        if (Boolean.FALSE.equals(data.pipelineSuccess()) && isNotBlank(data.pipelineFailReason())) {
            builder.append("h2. Ursache der fehlgeschlagenen Pipeline\n\n").append(data.pipelineFailReason().trim())
                .append("\n\n");
        }

        if (isSectionActive(data.hasAcceptanceCriteria(), data.acceptanceCriteria())) {
            builder.append("h2. Akzeptanzkriterien\n\n");
            for (String line : data.acceptanceCriteria().split("\r?\n")) {
                if (isNotBlank(line)) {
                    String trimmed = line.trim();
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
                String[] datasets = data.testDatasets().split("\r?\n");
                for (String dataset : datasets) {
                    if (!isNotBlank(dataset)) {
                        continue;
                    }
                    String[] parts = dataset.split("\\|", 2);
                    String file = parts[0].trim();
                    String desc = parts.length > 1 ? parts[1].trim() : "";
                    builder.append("|").append(file).append("|").append(desc).append("|\n");
                }
                builder.append("\n");
            }

            if (hasUnitTests) {
                builder.append("*Unit Tests*\n\n");
                for (String test : data.unitTests().split("\r?\n")) {
                    if (isNotBlank(test)) {
                        String trimmed = test.trim();
                        builder.append(trimmed.startsWith("*") ? trimmed : "* " + trimmed).append("\n");
                    }
                }
                builder.append("\n");
            }
        }

        return builder.toString().trim();
    }

    private boolean isSectionActive(final Boolean enabled, final String content) {
        return Boolean.TRUE.equals(enabled) && isNotBlank(content);
    }

    private boolean isNotBlank(final String str) {
        return str != null && !str.isBlank();
    }
}