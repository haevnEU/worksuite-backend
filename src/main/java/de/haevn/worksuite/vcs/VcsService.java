package de.haevn.worksuite.vcs;

import de.haevn.worksuite.config.UserContextHolder;
import de.haevn.worksuite.config.UserIntegrationContext;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.MergeRequest;
import org.gitlab4j.api.models.MergeRequestFilter;
import org.gitlab4j.api.models.Pipeline;
import org.gitlab4j.api.models.PipelineFilter;
import org.gitlab4j.api.models.Project;
import org.gitlab4j.api.models.ProtectedBranch;
import org.gitlab4j.models.Constants.MergeRequestScope;
import org.gitlab4j.models.Constants.MergeRequestState;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class VcsService {

    @Value("${app.vcs.url}")
    private String vcsUrl;

    @Value("${app.redmine.url:http://localhost/redmine}")
    private String redmineBaseUrl;

    private GitLabApi getGitLabApi() {
        UserIntegrationContext ctx = UserContextHolder.getContext();
        String activeToken = (ctx != null && isNotBlank(ctx.vcsToken())) ? ctx.vcsToken() : "";

        if (!isNotBlank(activeToken)) {
            throw new IllegalStateException("GitLab token is not configured for user.");
        }

        return new GitLabApi(vcsUrl, activeToken);
    }

    private String buildMrDescription(final MrProtocolRequest data) {
        final StringBuilder builder = new StringBuilder();

        if (isNotBlank(data.description())) {
            builder.append("## Description\n\n").append(data.description().trim()).append("\n\n");
        }

        if (isNotBlank(data.ticketId())) {
            final String rawTicketId = data.ticketId().trim().replaceAll("^#", "");
            final String formattedTicketLabel = "#" + rawTicketId;
            final String ticketUrl = sanitizeBaseUrl(redmineBaseUrl) + "/issues/" + rawTicketId;

            builder.append("**References:** [").append(formattedTicketLabel).append("](").append(ticketUrl)
                .append(")\n\n");
        }

        if (Boolean.TRUE.equals(data.hasImportantChanges()) && isNotBlank(data.importantChanges())) {
            builder.append("## Important Changes\n\n").append(data.importantChanges().trim()).append("\n\n");
        }

        final boolean hasUnits = Boolean.TRUE.equals(data.hasUnitTests()) && isNotBlank(data.unitTests());
        final boolean hasManuals = Boolean.TRUE.equals(data.hasManualTests()) && isNotBlank(data.manualTests());

        if (hasUnits || hasManuals) {
            builder.append("## Testing Procedure\n\n");

            if (hasUnits) {
                builder.append("### Unit Tests\n\n").append(data.unitTests().trim()).append("\n\n");
            }

            if (hasManuals) {
                builder.append("### Manual Tests\n\n").append(data.manualTests().trim()).append("\n\n");
            }
        }

        builder.append("## Notice Checklist\n\n").append("- [")
            .append(Boolean.TRUE.equals(data.hasBreakingChanges()) ? "x" : " ").append("] Breaking changes present\n")
            .append("- [").append(Boolean.TRUE.equals(data.hasDatabaseSchemaChanges()) ? "x" : " ")
            .append("] Database schema adjusted\n").append("- [")
            .append(Boolean.TRUE.equals(data.hasDatabaseViewsChanges()) ? "x" : " ")
            .append("] Database Views / Functions / Triggers adjusted\n");

        return builder.toString().trim();
    }

    private boolean isNotBlank(final String str) {
        return str != null && !str.isBlank();
    }

    private String sanitizeBaseUrl(final String url) {
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    public String createMergeRequest(final long ticketId, final MrProtocolRequest protocol) {
        log.info("Creating merge request for project");
        //final String description = buildMrDescription(protocol);
        //
        //if (!isNotBlank(vcsToken)) {
        //    log.error("Cannot create MR: GitLab token is missing.");
        //    throw new IllegalStateException("GitLab token is not configured.");
        //}
        //
        //try {
        //    final String title = "TEST";
        //    final String sourceBranch = "test";
        //    final String targetBranch = "master";
        //
        //    final MergeRequestParams params = new MergeRequestParams()
        //        .withTargetProjectId(getGitLabApi().getProjectApi().getProject("services", "wmt").getId())
        //        .withSourceBranch(sourceBranch)
        //        .withTargetBranch(targetBranch)
        //        .withTitle(title)
        //        .withDescription(description)
        //        .withRemoveSourceBranch(true); // Best Practice: Source-Branch nach Merge löschen
        //
        //    // API-Call zu GitLab
        //    final MergeRequest createdMr = getGitLabApi().getMergeRequestApi().create.createMergeRequest(params);
        //
        //    log.info("Successfully created MR! Web URL: {}", createdMr.getWebUrl());
        //
        //    // Gibt die klickbare URL zum neuen MR zurück
        //    return createdMr.getWebUrl();
        //
        //} catch (final GitLabApiException e) {
        //    log.error("Failed to create merge request in GitLab. Reason: {}", e.getMessage(), e);
        //    throw new RuntimeException("Failed to create Merge Request: " + e.getMessage(), e);
        //}
        return "n/a";
    }

    public List<MergeRequestDto> getMyMergeRequests() {
        try {
            final MergeRequestFilter filter =
                new MergeRequestFilter().withState(MergeRequestState.OPENED).withScope(MergeRequestScope.CREATED_BY_ME);

            final List<MergeRequest> mrs = getGitLabApi().getMergeRequestApi().getMergeRequests(filter);
            return mrs.stream().map(this::mapToMergeRequestDto).toList();
        } catch (final GitLabApiException e) {
            log.error("Failed to fetch my merge requests from GitLab", e);
            return Collections.emptyList();
        }
    }

    public List<MergeRequestDto> getPendingReviews() {
        try {
            final MergeRequestFilter filter = new MergeRequestFilter().withState(MergeRequestState.OPENED)
                .withScope(MergeRequestScope.ASSIGNED_TO_ME);

            final List<MergeRequest> mrs = getGitLabApi().getMergeRequestApi().getMergeRequests(filter);
            return mrs.stream().map(this::mapToMergeRequestDto).toList();
        } catch (final GitLabApiException e) {
            log.error("Failed to fetch pending reviews from GitLab", e);
            return Collections.emptyList();
        }
    }

    public List<ProtectedBranchPipelineDto> getProtectedBranchPipelines() {
        final List<ProtectedBranchPipelineDto> result = new ArrayList<>();
        try {
            final List<Project> projects = getGitLabApi().getProjectApi().getMemberProjects();

            for (final Project project : projects) {
                final List<ProtectedBranch> protectedBranches =
                    getGitLabApi().getProtectedBranchesApi().getProtectedBranches(project.getId());

                for (final ProtectedBranch branch : protectedBranches) {
                    final PipelineFilter pipelineFilter = new PipelineFilter().withRef(branch.getName());
                    final List<Pipeline> pipelines =
                        getGitLabApi().getPipelineApi().getPipelines(project.getId(), pipelineFilter);

                    if (!pipelines.isEmpty()) {
                        final Pipeline latest = pipelines.get(0);
                        final Pipeline fullPipeline =
                            getGitLabApi().getPipelineApi().getPipeline(project.getId(), latest.getId());

                        String commitTitle = "-";
                        if (fullPipeline.getSha() != null) {
                            try {
                                var commit =
                                    getGitLabApi().getCommitsApi().getCommit(project.getId(), fullPipeline.getSha());
                                commitTitle = commit.getTitle();
                            } catch (GitLabApiException ignored) {
                            }
                        }

                        result.add(
                            new ProtectedBranchPipelineDto(String.valueOf(fullPipeline.getId()), project.getName(),
                                branch.getName(), mapPipelineStatus(fullPipeline.getStatus()), commitTitle,
                                fullPipeline.getWebUrl(), fullPipeline.getUpdatedAt() != null ?
                                fullPipeline.getUpdatedAt().toInstant().atOffset(ZoneOffset.UTC)
                                    .format(DateTimeFormatter.ISO_INSTANT) :
                                ""));
                    }
                }
            }
        } catch (final GitLabApiException e) {
            log.error("Failed to fetch protected branch pipelines from GitLab", e);
            return Collections.emptyList();
        }
        return result;
    }

    private MergeRequestDto mapToMergeRequestDto(final MergeRequest mr) {
        final String projectName = (mr.getReferences() != null && mr.getReferences().getShort() != null) ?
            mr.getReferences().getShort() :
            "unknown";

        final MergeRequestDto.AuthorDto authorDto =
            new MergeRequestDto.AuthorDto(mr.getAuthor() != null ? mr.getAuthor().getName() : "Unknown",
                mr.getAuthor() != null ? mr.getAuthor().getAvatarUrl() : null);

        final boolean isApproved = mr.getApprovedBy() != null && !mr.getApprovedBy().isEmpty();

        return new MergeRequestDto(String.valueOf(mr.getId()), mr.getIid(), mr.getTitle(), authorDto,
            mr.getSourceBranch(), mr.getTargetBranch(), mr.getWebUrl(),
            mr.getHeadPipeline() != null ? mapPipelineStatus(mr.getHeadPipeline().getStatus()) : PipelineStatus.SKIPPED,
            mr.getUserNotesCount() != null ? mr.getUserNotesCount() : 0, Boolean.TRUE.equals(mr.getHasConflicts()),
            Boolean.TRUE.equals(mr.getWorkInProgress()) || (mr.getTitle() != null && mr.getTitle()
                .startsWith("Draft:")), isApproved, mr.getUpdatedAt() != null ?
            mr.getUpdatedAt().toInstant().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT) :
            "", projectName);
    }

    private PipelineStatus mapPipelineStatus(final org.gitlab4j.api.models.PipelineStatus status) {
        if (status == null) {
            return PipelineStatus.SKIPPED;
        }
        return switch (status) {
            case SUCCESS -> PipelineStatus.SUCCESS;
            case FAILED -> PipelineStatus.FAILED;
            case RUNNING -> PipelineStatus.RUNNING;
            case CANCELED -> PipelineStatus.CANCELED;
            case PENDING, CREATED, WAITING_FOR_RESOURCE -> PipelineStatus.PENDING;
            default -> PipelineStatus.SKIPPED;
        };
    }

    public List<GitLabRepository> getRepositories() {
        try {
            final List<Project> projects = getGitLabApi().getProjectApi().getProjects();

            return projects.stream().map(this::mapToGitLabRepository).toList();
        } catch (final GitLabApiException e) {
            log.error("Failed to fetch repositories from GitLab", e);
            return Collections.emptyList();
        }
    }

    private GitLabRepository mapToGitLabRepository(final Project project) {
        List<MergeRequestDto> openMrDtos = Collections.emptyList();
        String lastPipelineStatus = PipelineStatus.SKIPPED.name().toLowerCase();

        try {
            final MergeRequestFilter mrFilter =
                new MergeRequestFilter().withProjectId(project.getId()).withState(MergeRequestState.OPENED);

            final List<MergeRequest> openMrs = getGitLabApi().getMergeRequestApi().getMergeRequests(mrFilter);
            openMrDtos = openMrs.stream().map(this::mapToMergeRequestDto).toList();
        } catch (final GitLabApiException e) {
            log.warn("Failed to fetch merge requests for project {}: {}", project.getId(), e.getMessage());
        }

        if (isNotBlank(project.getDefaultBranch())) {
            try {
                final PipelineFilter pipelineFilter = new PipelineFilter().withRef(project.getDefaultBranch());
                final List<Pipeline> pipelines =
                    getGitLabApi().getPipelineApi().getPipelines(project.getId(), pipelineFilter);

                if (!pipelines.isEmpty()) {
                    lastPipelineStatus = mapPipelineStatus(pipelines.get(0).getStatus()).name().toLowerCase();
                }
            } catch (final GitLabApiException e) {
                log.warn("Failed to fetch last pipeline status for project {}: {}", project.getId(), e.getMessage());
            }
        }

        return new GitLabRepository(project.getId(), project.getWebUrl(), project.getName(), lastPipelineStatus,
            project.getPathWithNamespace(), openMrDtos.size(), openMrDtos);
    }
}