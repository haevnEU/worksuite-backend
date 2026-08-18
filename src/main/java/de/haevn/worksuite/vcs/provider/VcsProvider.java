package de.haevn.worksuite.vcs.provider;

import de.haevn.worksuite.vcs.MergeRequestDto;
import de.haevn.worksuite.vcs.MrProtocolRequest;
import de.haevn.worksuite.vcs.PipelineDTO;
import de.haevn.worksuite.vcs.RepositoryDTO;
import java.util.List;

/**
 * Common abstraction service for interacting with Version Control System (VCS) providers
 * (e.g., GitLab, GitHub).
 *
 * <p>Example usage:
 * <pre>{@code
 * VcsService vcsService = vcsServiceRegistry.getProvider(VcsProvider.GITLAB);
 * List<MergeRequestDto> pendingReviews = vcsService.getPendingReviews();
 * }</pre>
 */

public interface VcsProvider {

    /**
     * Creates a new merge request with standardized description formatting and issue linking.
     *
     * @param ticketId the unique identifier of the issue/ticket associated with this merge request
     * @param protocol the protocol request payload containing descriptions and checklist details
     * @return the web URL pointing to the newly created merge request, or {@code "n/a"}
     */
    String createMergeRequest(long ticketId, MrProtocolRequest protocol);

    /**
     * Fetches all open merge requests authored by the currently authenticated user.
     *
     * @return a list of {@link MergeRequestDto} objects, or an empty list if none found
     */
    List<MergeRequestDto> getMyMergeRequests();

    /**
     * Fetches all open merge requests assigned to the currently authenticated user for review.
     *
     * @return a list of {@link MergeRequestDto} objects pending user review
     */
    List<MergeRequestDto> getPendingReviews();

    /**
     * Retrieves the latest pipeline execution status for all protected branches across member projects.
     *
     * @return a list of {@link PipelineDTO} representing protected branch build states
     */
    List<PipelineDTO> getProtectedBranchPipelines();

    /**
     * Retrieves all repositories accessible to the user, including open merge request counts
     * and default branch pipeline statuses.
     *
     * @return a list of {@link RepositoryDTO} objects
     */
    List<RepositoryDTO> getRepositories();

    /**
     * Identifies the specific VCS provider type handled by this service implementation.
     *
     * @return the associated {@link de.haevn.worksuite.vcs.VcsProvider} identifier
     */
    de.haevn.worksuite.vcs.VcsProvider getProvider();
}