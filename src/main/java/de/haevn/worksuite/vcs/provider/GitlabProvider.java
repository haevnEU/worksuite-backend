package de.haevn.worksuite.vcs.provider;

import de.haevn.worksuite.common.exceptions.NotFoundException;
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
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Branch;
import org.gitlab4j.api.models.MergeRequest;
import org.gitlab4j.api.models.MergeRequestFilter;
import org.gitlab4j.api.models.MergeRequestParams;
import org.gitlab4j.api.models.Pipeline;
import org.gitlab4j.api.models.PipelineFilter;
import org.gitlab4j.api.models.Project;
import org.gitlab4j.api.models.ProtectedBranch;
import org.gitlab4j.api.models.User;
import org.gitlab4j.models.Constants.MergeRequestScope;
import org.gitlab4j.models.Constants.MergeRequestState;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.GitProperties;
import org.springframework.stereotype.Component;

/**
 * GitLab-specific implementation of the {@link de.haevn.worksuite.vcs.VcsProvider} interface.
 *
 * <p>Provides full integration with GitLab REST APIs using the {@link GitLabApi} client,
 * dynamically retrieving user access tokens via {@link UserContextHolder}.
 *
 * <p>Example usage:
 * <pre>{@code
 * @Autowired
 * private GitlabProvider gitlabProvider;
 *
 * List<MergeRequestDto> pendingReviews = gitlabProvider.getPendingReviews();
 * String mrUrl = gitlabProvider.createMergeRequest(4021L, mrProtocolRequest);
 * }</pre>
 */
@Slf4j
@Component
public class GitlabProvider implements VcsProvider {

    private static final String DEFAULT_TARGET_BRANCH = "master";
    private static final String DRAFT_PREFIX = "Draft:";

    private final GitProperties gitProperties;

    @Value("${app.vcs.url}")
    private String vcsUrl;

    @Value("${app.redmine.url:http://localhost/redmine}")
    private String ticketUrl;

    /**
     * Constructs the {@link GitlabProvider} with application Git build properties.
     *
     * @param gitProperties build-time git metadata provider
     */
    public GitlabProvider(final GitProperties gitProperties) {
        this.gitProperties = gitProperties;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public de.haevn.worksuite.vcs.VcsProvider getProvider() {
        return de.haevn.worksuite.vcs.VcsProvider.GITLAB;
    }

    /**
     * Creates a new GitLab merge request automatically resolving the matching feature branch for the ticket.
     *
     * <p>Example usage:
     * <pre>{@code
     * MrProtocolRequest request = new MrProtocolRequest(
     *     "[#4021] Refactor VCS integration",
     *     "Refactored GitLab provider methods",
     *     "4021",
     *     true,
     *     "Refactored branch search logic",
     *     true,
     *     true,
     *     "Added unit tests for branch matching regex",
     *     false,
     *     "",
     *     false,
     *     false,
     *     false
     * );
     * String webUrl = gitlabProvider.createMergeRequest(4021L, request);
     * }</pre>
     *
     * @param ticketId the unique numerical ticket identifier
     * @param protocol the structured merge request metadata payload
     * @return the web URL pointing to the created GitLab merge request
     * @throws NotFoundException if no matching branch is found across member projects
     * @throws RuntimeException if GitLab API interaction fails
     */
    @Override
    public String createMergeRequest(final long ticketId, final MrProtocolRequest protocol) {
        log.info("Initiating merge request creation for ticket #{}", ticketId);

        try {
            final GitLabApi client = getGitLabApi();
            final User currentUser = client.getUserApi().getCurrentUser();

            // Step 1: Scan all member repositories for a matching source branch
            final ResolvedBranch resolvedBranch = resolveBranchOptimized(ticketId)
                .orElseThrow(() -> new NotFoundException("No branch found for ticket #" + ticketId));

            final Project targetProject = resolvedBranch.project();
            final String sourceBranch = resolvedBranch.branchName();

            // Step 2: Determine target branch (prefer project default branch, fallback to master)
            final String targetBranch = isNotBlank(targetProject.getDefaultBranch())
                ? targetProject.getDefaultBranch()
                : DEFAULT_TARGET_BRANCH;

            // Step 3: Determine MR title (prefer user provided title, fallback to auto-generated)
            final String mrTitle = isNotBlank(protocol.title())
                ? protocol.title().trim()
                : "[#" + ticketId + "] Merge branch " + sourceBranch + " into " + targetBranch;

            // Step 4: Build MR creation parameters
            final MergeRequestParams params = new MergeRequestParams()
                .withTitle(mrTitle)
                .withSourceBranch(sourceBranch)
                .withTargetBranch(targetBranch)
                .withTargetProjectId(targetProject.getId())
                .withDescription(buildMrDescription(protocol))
                .withSquash(true)
                .withRemoveSourceBranch(true)
                .withAssigneeId(currentUser.getId());

            final MergeRequest createdMr = client.getMergeRequestApi()
                .createMergeRequest(targetProject.getId(), params);

            log.info("Successfully created merge request #{} in project {}: {}",
                createdMr.getIid(), targetProject.getPathWithNamespace(), createdMr.getWebUrl());

            return createdMr.getWebUrl();
        } catch (final GitLabApiException e) {
            log.error("GitLab API error while creating merge request for ticket #{}: {}", ticketId, e.getMessage(), e);
            throw new RuntimeException("Failed to create merge request in GitLab: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<MergeRequestDto> getMyMergeRequests() {
        try {
            final MergeRequestFilter filter = new MergeRequestFilter()
                .withState(MergeRequestState.OPENED)
                .withScope(MergeRequestScope.CREATED_BY_ME);

            final List<MergeRequest> mergeRequests = getGitLabApi().getMergeRequestApi().getMergeRequests(filter);
            return mergeRequests.stream().map(this::mapToMergeRequestDto).toList();
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
            final MergeRequestFilter filter = new MergeRequestFilter()
                .withState(MergeRequestState.OPENED)
                .withScope(MergeRequestScope.ASSIGNED_TO_ME);

            final List<MergeRequest> mergeRequests = getGitLabApi().getMergeRequestApi().getMergeRequests(filter);
            return mergeRequests.stream().map(this::mapToMergeRequestDto).toList();
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
            final GitLabApi client = getGitLabApi();
            final List<Project> projects = client.getProjectApi().getMemberProjects();

            for (final Project project : projects) {
                final List<ProtectedBranch> protectedBranches =
                    client.getProtectedBranchesApi().getProtectedBranches(project.getId());

                for (final ProtectedBranch branch : protectedBranches) {
                    final PipelineFilter pipelineFilter = new PipelineFilter().withRef(branch.getName());
                    final List<Pipeline> pipelines =
                        client.getPipelineApi().getPipelines(project.getId(), pipelineFilter);

                    if (!pipelines.isEmpty()) {
                        final Pipeline latestPipelineSummary = pipelines.get(0);
                        final Pipeline detailedPipeline =
                            client.getPipelineApi().getPipeline(project.getId(), latestPipelineSummary.getId());

                        String commitTitle = "-";
                        if (detailedPipeline.getSha() != null) {
                            try {
                                final var commit =
                                    client.getCommitsApi().getCommit(project.getId(), detailedPipeline.getSha());
                                commitTitle = commit.getTitle();
                            } catch (final GitLabApiException ignored) {
                                log.debug("Commit details could not be resolved for SHA: {}", detailedPipeline.getSha());
                            }
                        }

                        final String updatedAtIso = detailedPipeline.getUpdatedAt() != null
                            ? detailedPipeline.getUpdatedAt().toInstant().atOffset(ZoneOffset.UTC)
                            .format(DateTimeFormatter.ISO_INSTANT)
                            : "";

                        result.add(new PipelineDTO(
                            String.valueOf(detailedPipeline.getId()),
                            project.getName(),
                            branch.getName(),
                            mapPipelineStatus(detailedPipeline.getStatus()),
                            commitTitle,
                            detailedPipeline.getWebUrl(),
                            updatedAtIso
                        ));
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
     * Scans accessible member projects to locate a source branch conforming to ticket naming patterns.
     *
     * <p>Matches branch names matching {@code ^(fix|feature)/<ticketId>([-/_].*)?$}.
     *
     * <p>Example usage:
     * <pre>{@code
     * Optional<ResolvedBranch> match = gitlabProvider.resolveBranchOptimized(4021L);
     * match.ifPresent(b -> System.out.println("Found: " + b.branchName() + " in " + b.project().getName()));
     * }</pre>
     *
     * @param ticketId the ticket ID to locate
     * @return an {@link Optional} containing the {@link ResolvedBranch}, or empty if not located
     */
    public Optional<ResolvedBranch> resolveBranchOptimized(final long ticketId) {
        final Pattern pattern = Pattern.compile("^(fix|feature)/" + ticketId + "([-/_].*)?$", Pattern.CASE_INSENSITIVE);

        try {
            final GitLabApi client = getGitLabApi();
            final List<Project> memberProjects = client.getProjectApi().getMemberProjects();

            for (final Project project : memberProjects) {
                try {
                    // Server-side pre-filtering using the ticket ID search query string
                    final List<Branch> matchingBranches = client.getRepositoryApi()
                        .getBranches(project.getId(), String.valueOf(ticketId));

                    final Optional<String> branchName = matchingBranches.stream()
                        .map(Branch::getName)
                        .filter(name -> pattern.matcher(name).matches())
                        .findFirst();

                    if (branchName.isPresent()) {
                        return Optional.of(new ResolvedBranch(project, branchName.get()));
                    }
                } catch (final GitLabApiException e) {
                    log.debug("Could not search branches for project {}: {}", project.getId(), e.getMessage());
                }
            }
        } catch (final GitLabApiException e) {
            log.error("Failed to fetch member projects from GitLab", e);
        }

        return Optional.empty();
    }

    /**
     * Instantiates an authenticated {@link GitLabApi} client using the active user's integration context.
     *
     * <p>Example usage:
     * <pre>{@code
     * GitLabApi client = getGitLabApi();
     * }</pre>
     *
     * @return a configured {@link GitLabApi} client instance
     * @throws IllegalStateException if no personal access token exists in {@link UserContextHolder}
     */
    private GitLabApi getGitLabApi() {
        final UserIntegrationContext ctx = UserContextHolder.getContext();
        final String activeToken = (ctx != null && isNotBlank(ctx.vcsToken())) ? ctx.vcsToken() : "";

        if (!isNotBlank(activeToken)) {
            throw new IllegalStateException("GitLab personal access token is not configured for user.");
        }

        return new GitLabApi(this.vcsUrl, activeToken);
    }

    /**
     * Assembles a structured Markdown description for merge requests including issue references and checklists.
     *
     * <p>Example usage:
     * <pre>{@code
     * String markdown = buildMrDescription(protocolRequest);
     * }</pre>
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
            final String formattedTicketUrl = sanitizeBaseUrl(this.ticketUrl) + "/issues/" + rawTicketId;

            builder.append("**References:** [")
                .append(formattedTicketLabel)
                .append("](")
                .append(formattedTicketUrl)
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
        builder.append("## Notice Checklist\n\n")
            .append("| Topic | Status |\n|---|---|\n")
            .append("| **Breaking Changes** | `")
            .append(Boolean.TRUE.equals(data.hasBreakingChanges()) ? "YES" : "NO")
            .append("` |\n")
            .append("| **Database Schema Adjusted** | `")
            .append(Boolean.TRUE.equals(data.hasDatabaseSchemaChanges()) ? "YES" : "NO")
            .append("` |\n")
            .append("| **Views / Functions / Triggers** | `")
            .append(Boolean.TRUE.equals(data.hasDatabaseViewsChanges()) ? "YES" : "NO")
            .append("` |\n");
        return builder.toString().trim();
    }

    /**
     * Converts a GitLab4J {@link MergeRequest} model into a standardized {@link MergeRequestDto}.
     *
     * <p>Example usage:
     * <pre>{@code
     * MergeRequestDto dto = mapToMergeRequestDto(rawGitLabMergeRequest);
     * }</pre>
     *
     * @param mr the raw GitLab merge request entity
     * @return the mapped {@link MergeRequestDto}
     */
    private MergeRequestDto mapToMergeRequestDto(final MergeRequest mr) {
        final String projectName = (mr.getReferences() != null && mr.getReferences().getShort() != null)
            ? mr.getReferences().getShort()
            : "unknown";

        final MergeRequestDto.AuthorDto authorDto = new MergeRequestDto.AuthorDto(
            mr.getAuthor() != null ? mr.getAuthor().getName() : "Unknown",
            mr.getAuthor() != null ? mr.getAuthor().getAvatarUrl() : null
        );

        final boolean isApproved = mr.getApprovedBy() != null && !mr.getApprovedBy().isEmpty();
        final boolean isDraft = Boolean.TRUE.equals(mr.getWorkInProgress())
            || (mr.getTitle() != null && mr.getTitle().startsWith(DRAFT_PREFIX));

        final PipelineStatus pipelineStatus = mr.getHeadPipeline() != null
            ? mapPipelineStatus(mr.getHeadPipeline().getStatus())
            : PipelineStatus.SKIPPED;

        final String updatedAtIso = mr.getUpdatedAt() != null
            ? mr.getUpdatedAt().toInstant().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT)
            : "";

        return new MergeRequestDto(
            String.valueOf(mr.getId()),
            mr.getIid(),
            mr.getTitle(),
            authorDto,
            mr.getSourceBranch(),
            mr.getTargetBranch(),
            mr.getWebUrl(),
            pipelineStatus,
            mr.getUserNotesCount() != null ? mr.getUserNotesCount() : 0,
            Boolean.TRUE.equals(mr.getHasConflicts()),
            isDraft,
            isApproved,
            updatedAtIso,
            projectName
        );
    }

    /**
     * Converts a GitLab4J {@link Project} model into a standardized {@link RepositoryDTO}.
     *
     * <p>Example usage:
     * <pre>{@code
     * RepositoryDTO dto = mapToGitLabRepository(project);
     * }</pre>
     *
     * @param project the raw GitLab project entity
     * @return the populated {@link RepositoryDTO}
     */
    private RepositoryDTO mapToGitLabRepository(final Project project) {
        List<MergeRequestDto> openMrDtos = Collections.emptyList();
        String lastPipelineStatus = PipelineStatus.SKIPPED.name().toLowerCase();

        try {
            final MergeRequestFilter mrFilter = new MergeRequestFilter()
                .withProjectId(project.getId())
                .withState(MergeRequestState.OPENED);

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

        return new RepositoryDTO(
            project.getId(),
            project.getWebUrl(),
            project.getName(),
            lastPipelineStatus,
            project.getPathWithNamespace(),
            openMrDtos.size(),
            openMrDtos
        );
    }

    /**
     * Maps a GitLab4J pipeline status enumeration value to the internal {@link PipelineStatus}.
     *
     * <p>Example usage:
     * <pre>{@code
     * PipelineStatus status = mapPipelineStatus(org.gitlab4j.api.models.PipelineStatus.SUCCESS);
     * }</pre>
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
     * Checks whether a string reference is non-null and contains non-whitespace characters.
     *
     * <p>Example usage:
     * <pre>{@code
     * boolean valid = isNotBlank("feature/4021");
     * }</pre>
     *
     * @param str the string to validate
     * @return {@code true} if text is present, {@code false} otherwise
     */
    private boolean isNotBlank(final String str) {
        return str != null && !str.isBlank();
    }

    /**
     * Removes any trailing slash from the given URL string.
     *
     * <p>Example usage:
     * <pre>{@code
     * String cleanUrl = sanitizeBaseUrl("https://gitlab.example.com/");
     * }</pre>
     *
     * @param url the raw URL string
     * @return normalized URL without trailing slash
     */
    private String sanitizeBaseUrl(final String url) {
        return (url != null && url.endsWith("/")) ? url.substring(0, url.length() - 1) : url;
    }

    /**
     * Immutable value container pairing a matched GitLab {@link Project} with its verified source branch name.
     *
     * @param project the owning {@link Project}
     * @param branchName the resolved branch name (e.g. {@code feature/4021-add-vcs})
     */
    public record ResolvedBranch(Project project, String branchName) {
        public ResolvedBranch {
            Objects.requireNonNull(project, "Project must not be null");
            Objects.requireNonNull(branchName, "Branch name must not be null");
        }
    }
}