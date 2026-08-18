package de.haevn.worksuite.vcs.provider;

import de.haevn.worksuite.config.UserContextHolder;
import de.haevn.worksuite.config.UserIntegrationContext;
import de.haevn.worksuite.vcs.MergeRequestDto;
import de.haevn.worksuite.vcs.MrProtocolRequest;
import de.haevn.worksuite.vcs.PipelineDTO;
import de.haevn.worksuite.vcs.PipelineStatus;
import de.haevn.worksuite.vcs.RepositoryDTO;
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
import org.springframework.stereotype.Component;

/**
 * GitLab-specific implementation of the {@link VcsProvider} interface.
 *
 * <p>Provides integration with GitLab REST APIs using the {@link GitLabApi} client,
 * resolving user security tokens dynamically via {@link UserContextHolder}.
 *
 * <p>Example usage:
 * <pre>{@code
 * @Autowired
 * private GitlabProvider gitlabProvider;
 *
 * List<MergeRequestDto> pendingReviews = gitlabProvider.getPendingReviews();
 * }</pre>
 */
@Slf4j
@Component
public class GitlabProvider implements VcsProvider {

    @Value("${app.vcs.url}")
    private String vcsUrl;

    @Value("${app.redmine.url:http://localhost/redmine}")
    private String ticketUrl;

    /**
     * Instantiates an authenticated {@link GitLabApi} client using the active user's integration context.
     *
     * @return a configured {@link GitLabApi} instance
     * @throws IllegalStateException if the GitLab personal access token is missing in the user context
     */
    private GitLabApi getGitLabApi() {
        final UserIntegrationContext ctx = UserContextHolder.getContext();
        final String activeToken = (ctx != null && isNotBlank(ctx.vcsToken())) ? ctx.vcsToken() : "";

        if (!isNotBlank(activeToken)) {
            throw new IllegalStateException("GitLab personal access token is not configured for user.");
        }

        return new GitLabApi(vcsUrl, activeToken);
    }

    /**
     * Assembles a structured Markdown description for merge requests including issue references and checklists.
     *
     * @param data the protocol request model containing details and flags
     * @return the assembled Markdown text
     */
    private String buildMrDescription(final MrProtocolRequest data) {
        final StringBuilder builder = new StringBuilder();

        if (isNotBlank(data.description())) {
            builder.append("## Description\n\n").append(data.description().trim()).append("\n\n");
        }

        if (isNotBlank(data.ticketId())) {
            final String rawTicketId = data.ticketId().trim().replaceAll("^#", "");
            final String formattedTicketLabel = "#" + rawTicketId;
            final String ticketUrl = sanitizeBaseUrl(this.ticketUrl) + "/issues/" + rawTicketId;

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

    /**
     * Checks if a string reference is non-null and not blank.
     *
     * @param str the string to validate
     * @return {@code true} if text is present, {@code false} otherwise
     */
    private boolean isNotBlank(final String str) {
        return str != null && !str.isBlank();
    }

    /**
     * Removes trailing slashes from base URL paths.
     *
     * @param url the raw URL string
     * @return the normalized URL without a trailing slash
     */
    private String sanitizeBaseUrl(final String url) {
        return (url != null && url.endsWith("/")) ? url.substring(0, url.length() - 1) : url;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String createMergeRequest(final long ticketId, final MrProtocolRequest protocol) {
        log.info("Initiating merge request creation for ticket #{}", ticketId);
        // Method placeholder for future automated branch creation and MR dispatching
        return "n/a";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<MergeRequestDto> getMyMergeRequests() {
        try {
            final MergeRequestFilter filter =
                new MergeRequestFilter().withState(MergeRequestState.OPENED).withScope(MergeRequestScope.CREATED_BY_ME);

            final List<MergeRequest> mrs = getGitLabApi().getMergeRequestApi().getMergeRequests(filter);
            return mrs.stream().map(this::mapToMergeRequestDto).toList();
        } catch (final GitLabApiException e) {
            log.error("Failed to fetch authored merge requests from GitLab", e);
            return Collections.emptyList();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<MergeRequestDto> getPendingReviews() {
        try {
            final MergeRequestFilter filter = new MergeRequestFilter().withState(MergeRequestState.OPENED)
                .withScope(MergeRequestScope.ASSIGNED_TO_ME);

            final List<MergeRequest> mrs = getGitLabApi().getMergeRequestApi().getMergeRequests(filter);
            return mrs.stream().map(this::mapToMergeRequestDto).toList();
        } catch (final GitLabApiException e) {
            log.error("Failed to fetch pending review merge requests from GitLab", e);
            return Collections.emptyList();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<PipelineDTO> getProtectedBranchPipelines() {
        final List<PipelineDTO> result = new ArrayList<>();
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
                                final var commit =
                                    getGitLabApi().getCommitsApi().getCommit(project.getId(), fullPipeline.getSha());
                                commitTitle = commit.getTitle();
                            } catch (final GitLabApiException ignored) {
                                log.debug("Commit details could not be resolved for SHA: {}", fullPipeline.getSha());
                            }
                        }

                        result.add(
                            new PipelineDTO(String.valueOf(fullPipeline.getId()), project.getName(), branch.getName(),
                                mapPipelineStatus(fullPipeline.getStatus()), commitTitle, fullPipeline.getWebUrl(),
                                fullPipeline.getUpdatedAt() != null ?
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

    /**
     * {@inheritDoc}
     */
    @Override
    public List<RepositoryDTO> getRepositories() {
        try {
            final List<Project> projects = getGitLabApi().getProjectApi().getProjects();
            return projects.stream().map(this::mapToGitLabRepository).toList();
        } catch (final GitLabApiException e) {
            log.error("Failed to fetch repositories from GitLab", e);
            return Collections.emptyList();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public de.haevn.worksuite.vcs.VcsProvider getProvider() {
        return de.haevn.worksuite.vcs.VcsProvider.GITLAB;
    }

    /**
     * Converts a GitLab4J {@link MergeRequest} model into a standardized {@link MergeRequestDto}.
     *
     * @param mr the raw GitLab merge request entity
     * @return the mapped {@link MergeRequestDto}
     */
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

    /**
     * Maps a GitLab4J pipeline status enumeration value to the internal {@link PipelineStatus}.
     *
     * @param status the raw GitLab pipeline status
     * @return the mapped {@link PipelineStatus}
     */
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

    /**
     * Converts a GitLab4J {@link Project} model into a standardized {@link RepositoryDTO}.
     *
     * @param project the raw GitLab project entity
     * @return the populated {@link RepositoryDTO}
     */
    private RepositoryDTO mapToGitLabRepository(final Project project) {
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

        return new RepositoryDTO(project.getId(), project.getWebUrl(), project.getName(), lastPipelineStatus,
            project.getPathWithNamespace(), openMrDtos.size(), openMrDtos);
    }
}