package de.haevn.worksuite.vcs;

import de.haevn.worksuite.vcs.provider.VcsProvider;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service orchestrator dispatching version control operations directly to the selected {@link VcsProvider}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VcsService {

    private final VcsProviderRegistry vcsProviderRegistry;

    /**
     * Creates a merge request via the specified VCS provider.
     */
    public String createMergeRequest(final de.haevn.worksuite.vcs.VcsProvider providerType, final long ticketId,
        final MrProtocolRequest protocol) {
        final VcsProvider provider = vcsProviderRegistry.getVcsService(providerType);
        return provider.createMergeRequest(ticketId, protocol);
    }

    /**
     * Fetches authored merge requests via the specified VCS provider.
     */
    public List<MergeRequestDto> getMyMergeRequests(final de.haevn.worksuite.vcs.VcsProvider providerType) {
        final VcsProvider provider = vcsProviderRegistry.getVcsService(providerType);
        return provider.getMyMergeRequests();
    }

    /**
     * Fetches pending reviews via the specified VCS provider.
     */
    public List<MergeRequestDto> getPendingReviews(final de.haevn.worksuite.vcs.VcsProvider providerType) {
        final VcsProvider provider = vcsProviderRegistry.getVcsService(providerType);
        return provider.getPendingReviews();
    }

    /**
     * Fetches protected branch pipeline states via the specified VCS provider.
     */
    public List<PipelineDTO> getProtectedBranchPipelines(final de.haevn.worksuite.vcs.VcsProvider providerType) {
        final VcsProvider provider = vcsProviderRegistry.getVcsService(providerType);
        return provider.getProtectedBranchPipelines();
    }

    /**
     * Fetches accessible repositories via the specified VCS provider.
     */
    public List<RepositoryDTO> getRepositories(final de.haevn.worksuite.vcs.VcsProvider providerType) {
        final VcsProvider provider = vcsProviderRegistry.getVcsService(providerType);
        return provider.getRepositories();
    }
}