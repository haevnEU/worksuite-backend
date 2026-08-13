package de.haevn.worksuite.vcs;


import de.haevn.worksuite.common.RestApiController;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

@RequiredArgsConstructor
@RestApiController("/api/v1/vcs")
public class VcsController {
    private final VcsService vcsService;

    @GetMapping("/merge-requests/my")
    public ResponseEntity<List<MergeRequestDto>> getMyMergeRequests() {
        List<MergeRequestDto> mergeRequests = vcsService.getMyMergeRequests();
        return ResponseEntity.ok(mergeRequests);
    }

    @GetMapping("/merge-requests/reviews")
    public ResponseEntity<List<MergeRequestDto>> getPendingReviews() {
        List<MergeRequestDto> pendingReviews = vcsService.getPendingReviews();
        return ResponseEntity.ok(pendingReviews);
    }

    @GetMapping("/pipelines")
    public ResponseEntity<List<ProtectedBranchPipelineDto>> getProtectedBranchPipelines() {
        List<ProtectedBranchPipelineDto> pipelines = vcsService.getProtectedBranchPipelines();
        return ResponseEntity.ok(pipelines);
    }

    @GetMapping("/repositories")
    public ResponseEntity<List<GitLabRepository>> getRepositories() {
        List<GitLabRepository> repositories = vcsService.getRepositories();
        return ResponseEntity.ok(repositories);
    }
}
