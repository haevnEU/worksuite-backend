package de.haevn.worksuite.vcs.provider;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.haevn.worksuite.common.exceptions.NotFoundException;
import de.haevn.worksuite.config.UserContextHolder;
import de.haevn.worksuite.config.UserIntegrationContext;
import de.haevn.worksuite.vcs.MergeRequestDto;
import de.haevn.worksuite.vcs.MrProtocolRequest;
import de.haevn.worksuite.vcs.PipelineDTO;
import de.haevn.worksuite.vcs.PipelineStatus;
import de.haevn.worksuite.vcs.RepositoryDTO;
import de.haevn.worksuite.vcs.provider.gitlab.AssignedMergeRequest;
import de.haevn.worksuite.vcs.provider.gitlab.GitLabPipeline;
import de.haevn.worksuite.vcs.provider.gitlab.GitLabProject;
import de.haevn.worksuite.vcs.provider.gitlab.GitLabUser;
import io.micrometer.common.util.StringUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.GitProperties;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * GitLab-specific implementation of the {@link de.haevn.worksuite.vcs.VcsProvider} interface.
 *
 * <p>Communicates directly with the official GitLab REST API v4 using Spring's {@link RestClient}.
 */
@Slf4j
@Component
public class GitlabProvider implements VcsProvider {

    private static final String DRAFT_PREFIX = "Draft:";

    private final GitProperties gitProperties;
    private final RestClient.Builder restClientBuilder;

    @Value("${app.vcs.repos.watched:}")
    private String targetRepoList;

    @Value("${app.vcs.url}")
    private String vcsUrl;

    @Value("${app.redmine.url:http://localhost/redmine}")
    private String ticketUrl;

    /**
     * Constructs the {@link GitlabProvider} with required build metadata and client builder.
     *
     * @param gitProperties build-time git metadata provider
     * @param restClientBuilder auto-configured builder for establishing REST connections
     */
    public GitlabProvider(final GitProperties gitProperties, final RestClient.Builder restClientBuilder) {
        this.gitProperties = gitProperties;
        this.restClientBuilder = restClientBuilder;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public de.haevn.worksuite.vcs.VcsProvider getProvider() {
        return de.haevn.worksuite.vcs.VcsProvider.GITLAB;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String createMergeRequest(final long ticketId, final MrProtocolRequest protocol) {
        throw new UnsupportedOperationException("createMergeRequest is currently disabled.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<MergeRequestDto> getMyMergeRequests() {
        try {
            final List<AssignedMergeRequest> mergeRequests = getRestClient().get()
                .uri(uriBuilder -> uriBuilder
                    .path("/merge_requests")
                    .queryParam("scope", "created_by_me")
                    .queryParam("state", "opened")
                    .queryParam("per_page", 100)
                    .build())
                .retrieve()
                .body(new ParameterizedTypeReference<List<AssignedMergeRequest>>() {});

            if (mergeRequests == null) {
                return Collections.emptyList();
            }

            return mergeRequests.stream().map(this::mapToMergeRequestDto).toList();
        } catch (final Exception e) {
            log.error("Failed to fetch authored merge requests from GitLab REST API", e);
            return Collections.emptyList();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<MergeRequestDto> getPendingReviews() {
        try {
            final GitLabUser currentUser = getCurrentUser();
            final Long currentUserId = currentUser.id();

            final List<AssignedMergeRequest> mergeRequests = getRestClient().get()
                .uri(uriBuilder -> uriBuilder
                    .path("/merge_requests")
                    .queryParam("scope", "reviews_for_me")
                    .queryParam("state", "opened")
                    .queryParam("per_page", 100)
                    .build())
                .retrieve()
                .body(new ParameterizedTypeReference<List<AssignedMergeRequest>>() {});

            if (mergeRequests == null) {
                return Collections.emptyList();
            }

            return mergeRequests.stream()
                .filter(mr -> mr.author() != null && !Objects.equals(mr.author().id(), currentUserId))
                .map(this::mapToMergeRequestDto)
                .toList();
        } catch (final Exception e) {
            log.error("Failed to fetch pending review merge requests from GitLab REST API", e);
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
            final RestClient client = getRestClient();
            final List<GitLabProject> projects = getMemberProjects();

            for (final GitLabProject project : projects) {
                final List<GitLabProtectedBranch> protectedBranches = client.get()
                    .uri("/projects/{id}/protected_branches", project.id())
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<GitLabProtectedBranch>>() {});

                if (protectedBranches == null) {
                    continue;
                }

                for (final GitLabProtectedBranch branch : protectedBranches) {
                    final List<GitLabPipeline> pipelines = client.get()
                        .uri(uriBuilder -> uriBuilder
                            .path("/projects/{id}/pipelines")
                            .queryParam("ref", branch.name())
                            .queryParam("per_page", 1)
                            .queryParam("order_by", "id")
                            .queryParam("sort", "desc")
                            .build(project.id()))
                        .retrieve()
                        .body(new ParameterizedTypeReference<List<GitLabPipeline>>() {});

                    if (pipelines != null && !pipelines.isEmpty()) {
                        final GitLabPipeline pipeline = pipelines.get(0);
                        String commitTitle = "-";

                        if (isNotBlank(pipeline.sha())) {
                            try {
                                final GitLabCommit commit = client.get()
                                    .uri("/projects/{id}/repository/commits/{sha}", project.id(), pipeline.sha())
                                    .retrieve()
                                    .body(GitLabCommit.class);
                                if (commit != null && isNotBlank(commit.title())) {
                                    commitTitle = commit.title();
                                }
                            } catch (final Exception ignored) {
                                log.debug("Commit details could not be resolved for SHA: {}", pipeline.sha());
                            }
                        }

                        result.add(new PipelineDTO(
                            String.valueOf(pipeline.id()),
                            project.name(),
                            branch.name(),
                            mapPipelineStatus(pipeline.status()),
                            commitTitle,
                            pipeline.webUrl(),
                            pipeline.createdAt() != null ? pipeline.createdAt() : ""
                        ));
                    }
                }
            }
        } catch (final Exception e) {
            log.error("Failed to fetch protected branch pipelines from GitLab REST API", e);
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
            final List<GitLabProject> projects = getMemberProjects();
            return projects.stream().map(this::mapToGitLabRepository).toList();
        } catch (final Exception e) {
            log.error("Failed to fetch repositories from GitLab REST API", e);
            return Collections.emptyList();
        }
    }

    /**
     * Scans configured target repositories to locate a source branch conforming to ticket naming patterns.
     *
     * @param ticketId the unique numerical ticket identifier
     * @return an {@link Optional} containing the {@link ResolvedBranch}, or empty if not located
     */
    public Optional<ResolvedBranch> resolveBranchOptimized(final long ticketId) {
        final Pattern pattern = Pattern.compile("^(fix|feature)/" + ticketId + "([-/_].*)?$", Pattern.CASE_INSENSITIVE);

        try {
            final RestClient client = getRestClient();
            final List<GitLabProject> memberProjects = getMemberProjects();

            for (final GitLabProject project : memberProjects) {
                try {
                    final List<GitLabBranch> matchingBranches = client.get()
                        .uri(uriBuilder -> uriBuilder
                            .path("/projects/{id}/repository/branches")
                            .queryParam("search", String.valueOf(ticketId))
                            .build(project.id()))
                        .retrieve()
                        .body(new ParameterizedTypeReference<List<GitLabBranch>>() {});

                    if (matchingBranches != null) {
                        final Optional<String> branchName = matchingBranches.stream()
                            .map(GitLabBranch::name)
                            .filter(name -> pattern.matcher(name).matches())
                            .findFirst();

                        if (branchName.isPresent()) {
                            return Optional.of(new ResolvedBranch(project, branchName.get()));
                        }
                    }
                } catch (final Exception e) {
                    log.debug("Could not search branches for project {}: {}", project.id(), e.getMessage());
                }
            }
        } catch (final Exception e) {
            log.error("Failed to resolve branch across member projects", e);
        }

        return Optional.empty();
    }

    /**
     * Builds and configures an authenticated {@link RestClient} instance.
     *
     * @return an initialized {@link RestClient}
     * @throws IllegalStateException if the user integration context does not contain a personal access token
     */
    private RestClient getRestClient() {
        final UserIntegrationContext ctx = UserContextHolder.getContext();
        final String activeToken = (ctx != null && isNotBlank(ctx.vcsToken())) ? ctx.vcsToken() : "";

        if (!isNotBlank(activeToken)) {
            throw new IllegalStateException("GitLab personal access token is not configured for user.");
        }

        return restClientBuilder
            .baseUrl(sanitizeBaseUrl(this.vcsUrl) + "/api/v4")
            .defaultHeader("PRIVATE-TOKEN", activeToken)
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .build();
    }

    /**
     * Retrieves the profile information of the currently authenticated GitLab user.
     *
     * @return the resolved {@link GitLabUser}
     * @throws IllegalStateException if the user details cannot be resolved
     */
    private GitLabUser getCurrentUser() {
        final GitLabUser user = getRestClient().get()
            .uri("/user")
            .retrieve()
            .body(GitLabUser.class);

        if (user == null) {
            throw new IllegalStateException("Unable to resolve current GitLab user context.");
        }
        return user;
    }

    /**
     * Resolves metadata for repositories explicitly configured in the target repository list property.
     *
     * @return a list of matching {@link GitLabProject} models
     */
    private List<GitLabProject> getMemberProjects() {
        final RestClient client = getRestClient();
        final List<GitLabProject> projects = new ArrayList<>();

        if (StringUtils.isBlank(targetRepoList)) {
            return projects;
        }

        final List<Long> targetList = Arrays.stream(targetRepoList.split(";"))
            .map(String::trim)
            .filter(StringUtils::isNotBlank)
            .map(Long::parseLong)
            .toList();

        for (final Long projectId : targetList) {
            try {
                final GitLabProject project = client.get()
                    .uri("/projects/{id}", projectId)
                    .retrieve()
                    .body(GitLabProject.class);

                if (project != null) {
                    projects.add(project);
                }
            } catch (final Exception ex) {
                log.warn("Failed to fetch project details for configured project ID {}: {}", projectId, ex.getMessage());
            }
        }

        return projects;
    }

    /**
     * Maps a GitLab REST API merge request payload to a unified {@link MergeRequestDto}.
     *
     * @param mr the raw {@link AssignedMergeRequest} payload
     * @return the normalized {@link MergeRequestDto}
     */
    private MergeRequestDto mapToMergeRequestDto(final AssignedMergeRequest mr) {
        final MergeRequestDto.AuthorDto authorDto = new MergeRequestDto.AuthorDto(
            mr.author() != null ? mr.author().name() : "Unknown",
            mr.author() != null ? mr.author().avatarUrl() : null
        );

        final boolean isApproved = false;
        final boolean isDraft = (mr.title() != null && mr.title().startsWith(DRAFT_PREFIX));
        final PipelineStatus pipelineStatus = PipelineStatus.SKIPPED;

        return new MergeRequestDto(
            String.valueOf(mr.id()),
            mr.iid() != null ? mr.iid() : 0L,
            mr.title(),
            authorDto,
            mr.sourceBranch(),
            mr.targetBranch(),
            mr.webUrl(),
            pipelineStatus,
            0,
            false,
            isDraft,
            isApproved,
            mr.updatedAt() != null ? mr.updatedAt() : (mr.createdAt() != null ? mr.createdAt() : ""),
            String.valueOf(mr.projectId())
        );
    }

    /**
     * Aggregates open merge requests and latest default branch pipeline status for a repository.
     *
     * @param project the target {@link GitLabProject}
     * @return the populated {@link RepositoryDTO}
     */
    private RepositoryDTO mapToGitLabRepository(final GitLabProject project) {
        List<MergeRequestDto> openMrDtos = Collections.emptyList();
        String lastPipelineStatus = PipelineStatus.SKIPPED.name().toLowerCase();

        try {
            final List<AssignedMergeRequest> openMrs = getRestClient().get()
                .uri(uriBuilder -> uriBuilder
                    .path("/projects/{id}/merge_requests")
                    .queryParam("state", "opened")
                    .queryParam("per_page", 50)
                    .build(project.id()))
                .retrieve()
                .body(new ParameterizedTypeReference<List<AssignedMergeRequest>>() {});

            if (openMrs != null) {
                openMrDtos = openMrs.stream().map(this::mapToMergeRequestDto).toList();
            }
        } catch (final Exception e) {
            log.warn("Failed to fetch merge requests for project {}: {}", project.id(), e.getMessage());
        }

        if (isNotBlank(project.defaultBranch())) {
            try {
                final List<GitLabPipeline> pipelines = getRestClient().get()
                    .uri(uriBuilder -> uriBuilder
                        .path("/projects/{id}/pipelines")
                        .queryParam("ref", project.defaultBranch())
                        .queryParam("per_page", 1)
                        .build(project.id()))
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<GitLabPipeline>>() {});

                if (pipelines != null && !pipelines.isEmpty()) {
                    lastPipelineStatus = mapPipelineStatus(pipelines.get(0).status()).name().toLowerCase();
                }
            } catch (final Exception e) {
                log.warn("Failed to fetch last pipeline status for project {}: {}", project.id(), e.getMessage());
            }
        }

        return new RepositoryDTO(
            project.id() != null ? project.id() : 0L,
            project.webUrl(),
            project.name(),
            lastPipelineStatus,
            project.pathWithNamespace(),
            openMrDtos.size(),
            openMrDtos
        );
    }

    /**
     * Normalizes a raw string status representation from GitLab into a typed {@link PipelineStatus}.
     *
     * @param status raw status string returned by GitLab
     * @return mapped {@link PipelineStatus} enum constant
     */
    private PipelineStatus mapPipelineStatus(final String status) {
        if (status == null) {
            return PipelineStatus.SKIPPED;
        }
        return switch (status.toLowerCase()) {
            case "success" -> PipelineStatus.SUCCESS;
            case "failed" -> PipelineStatus.FAILED;
            case "running" -> PipelineStatus.RUNNING;
            case "canceled" -> PipelineStatus.CANCELED;
            case "pending", "created", "waiting_for_resource", "preparing" -> PipelineStatus.PENDING;
            default -> PipelineStatus.SKIPPED;
        };
    }

    /**
     * Checks whether a given string is non-null and not blank.
     *
     * @param str the candidate string to evaluate
     * @return {@code true} if text is present; {@code false} otherwise
     */
    private boolean isNotBlank(final String str) {
        return str != null && !str.isBlank();
    }

    /**
     * Removes trailing slashes from endpoint URLs to ensure consistent path construction.
     *
     * @param url the raw URL string
     * @return sanitized URL string without trailing slash
     */
    private String sanitizeBaseUrl(final String url) {
        return (url != null && url.endsWith("/")) ? url.substring(0, url.length() - 1) : url;
    }

    /**
     * Immutable value container pairing a matched {@link GitLabProject} with its verified source branch name.
     *
     * @param project the parent GitLab project
     * @param branchName the resolved branch name (e.g. {@code feature/4021-add-vcs})
     */
    public record ResolvedBranch(GitLabProject project, String branchName) {
        public ResolvedBranch {
            Objects.requireNonNull(project, "Project must not be null");
            Objects.requireNonNull(branchName, "Branch name must not be null");
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GitLabProtectedBranch(@JsonProperty("name") String name) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GitLabCommit(@JsonProperty("id") String id, @JsonProperty("title") String title) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GitLabBranch(@JsonProperty("name") String name) {}
}