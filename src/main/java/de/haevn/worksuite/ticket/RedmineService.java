package de.haevn.worksuite.ticket;

import de.haevn.redmine.api.InfoType;
import de.haevn.redmine.api.QueryParams;
import de.haevn.redmine.api.RedmineClient;
import de.haevn.redmine.api.RedmineException;
import de.haevn.redmine.model.Issue;
import de.haevn.redmine.model.RedmineInfoResponses;
import de.haevn.worksuite.common.FileDownloadService;
import de.haevn.worksuite.time.TimeService;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Slf4j
@Service
public class RedmineService {
    private final String apiKey;
    private final String baseUrl;
    private final FileDownloadService fileDownloadService;
    private final RedmineClient redmineClient;
    private final TimeService timeService;
    private final List<Issue> issues = new ArrayList<>();

    public RedmineService(final FileDownloadService fileDownloadService, @Value("${app.redmine.url}") String baseUrl,
        @Value("${app.redmine.api-key}") String apiKey, final TimeService timeService) {
        this.fileDownloadService = fileDownloadService;
        this.timeService = timeService;
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalArgumentException("Property 'app.redmine.url' must not be null or empty!");
        }
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.redmineClient = RedmineClient.create(baseUrl, apiKey);
    }

    //@Cacheable("tickets")
    public List<Issue> fetch() throws RedmineException {
        issues.clear();
        issues.addAll(redmineClient.getMyAssignedIssues(QueryParams.values()));
        for (int i = 0; i < issues.size(); i++) {
            final Issue issue = issues.get(i);
            final var t = getByIssuedId(issue.id());
            if (t.isPresent()) {
                issues.set(i, t.get());
            }
        }
        return issues;
    }

    public Optional<Issue> getByIssuedId(final long id) throws RedmineException {
        return redmineClient.getIssueById(id, QueryParams.values());
    }

    public void moveToStatus(final long ticketId, final TicketStatus status) throws RedmineException {
        redmineClient.moveToStatus(ticketId, status.id);
    }

    public void addComment(final long ticketId, String comment) throws RedmineException {
        redmineClient.addComment(ticketId, comment);
    }

    public void bookTime(final long ticketId, int hours, int minutes, String description, int activityId,
        Optional<String> dateOpt) throws RedmineException {

        final String date =
            dateOpt.filter(d -> d.matches("^\\d{4}-\\d{2}-\\d{2}$")).orElseGet(() -> LocalDate.now().toString());

        redmineClient.logTime(ticketId, hours, minutes, description, activityId, date);
    }

    public void createCheckboxes(final long ticketId) throws RedmineException {
        redmineClient.addChecklistItem(ticketId, "");
        redmineClient.addChecklistItem(ticketId, "");
        redmineClient.addChecklistItem(ticketId, "");
        redmineClient.addChecklistItem(ticketId, "");
        redmineClient.addChecklistItem(ticketId, "");
        redmineClient.addChecklistItem(ticketId, "");
    }

    public void tickCheckbox(final long ticketId, final int checkboxIndex, final boolean state)
        throws RedmineException {
        redmineClient.tickCheckbox(ticketId, checkboxIndex, state);
    }

    public Flux<DataBuffer> downloadAttachment(final String url) {
        URI downloadUri = URI.create(url);
        return fileDownloadService.download(downloadUri, "X-Redmine-API-Key", apiKey);
    }

    public void moveToQs(final long id, final @Valid QaProtocolRequest data) throws RedmineException {
        log.info("Moving to Qs for {}", id);
        log.info("Got data {}", data.toString());
        redmineClient.moveToStatus(id, 8, buildQAComment(data));
    }

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

    public void addMergeRequestLink(final long id, final String mrLink) throws RedmineException {
        redmineClient.setCustomField(id, mrLink, 1);
    }

    public void bookTicket(final long id, final @Valid LogTimeRequest request) throws RedmineException {
        redmineClient.logTime(id, request.hours(), request.minutes(), request.comment(), request.activityId(),
            request.day());
        timeService.book(id, request);
    }

    public List<RedmineInfoResponses.InfoResponse> getInfo(final InfoType infoType) throws RedmineException {
        return redmineClient.getInfo(infoType);
    }

}