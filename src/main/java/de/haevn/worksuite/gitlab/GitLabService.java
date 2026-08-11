package de.haevn.worksuite.gitlab;

import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class GitLabService {

    @Value("${app.gitlab.url}")
    private String gitlabBaseUrl;

    @Value("${app.gitlab.api-key:}")
    private String gitlabToken;

    @Value("${app.redmine.url:http://localhost/redmine}")
    private String redmineBaseUrl;

    private String buildMrDescription(final MrProtocolRequest data) {
        final StringBuilder builder = new StringBuilder();

        if (isNotBlank(data.description())) {
            builder.append("## Description\n\n")
                .append(data.description().trim())
                .append("\n\n");
        }

        if (isNotBlank(data.ticketId())) {
            final String rawTicketId = data.ticketId().trim().replaceAll("^#", "");
            final String formattedTicketLabel = "#" + rawTicketId;
            final String ticketUrl = sanitizeBaseUrl(redmineBaseUrl) + "/issues/" + rawTicketId;

            builder.append("**References:** [")
                .append(formattedTicketLabel)
                .append("](")
                .append(ticketUrl)
                .append(")\n\n");
        }

        if (Boolean.TRUE.equals(data.hasImportantChanges()) && isNotBlank(data.importantChanges())) {
            builder.append("## Important Changes\n\n")
                .append(data.importantChanges().trim())
                .append("\n\n");
        }

        final boolean hasUnits = Boolean.TRUE.equals(data.hasUnitTests()) && isNotBlank(data.unitTests());
        final boolean hasManuals = Boolean.TRUE.equals(data.hasManualTests()) && isNotBlank(data.manualTests());

        if (hasUnits || hasManuals) {
            builder.append("## Testing Procedure\n\n");

            if (hasUnits) {
                builder.append("### Unit Tests\n\n")
                    .append(data.unitTests().trim())
                    .append("\n\n");
            }

            if (hasManuals) {
                builder.append("### Manual Tests\n\n")
                    .append(data.manualTests().trim())
                    .append("\n\n");
            }
        }

        builder.append("## Notice Checklist\n\n")
            .append("- [").append(Boolean.TRUE.equals(data.hasBreakingChanges()) ? "x" : " ").append("] Breaking changes present\n")
            .append("- [").append(Boolean.TRUE.equals(data.hasDatabaseSchemaChanges()) ? "x" : " ").append("] Database schema adjusted\n")
            .append("- [").append(Boolean.TRUE.equals(data.hasDatabaseViewsChanges()) ? "x" : " ").append("] Database Views / Functions / Triggers adjusted\n");

        return builder.toString().trim();
    }

    private boolean isNotBlank(final String str) {
        return str != null && !str.isBlank();
    }

    private String sanitizeBaseUrl(final String url) {
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    public String createMergeRequest(final String s, final MrProtocolRequest protocol) {
        log.info("Creating merge request");
        final String comment = buildMrDescription(protocol);
        log.info("Merge request comment: {}", comment);

        return gitlabBaseUrl + "/...";
    }

    public List<MergeRequestDto> getMyMergeRequests() {
        return Collections.emptyList();
    }

    public List<MergeRequestDto> getPendingReviews() {
        return Collections.emptyList();
    }

    public List<ProtectedBranchPipelineDto> getProtectedBranchPipelines() {
        return Collections.emptyList();
    }
}
