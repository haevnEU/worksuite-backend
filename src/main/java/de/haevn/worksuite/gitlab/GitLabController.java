package de.haevn.worksuite.gitlab;


import de.haevn.worksuite.common.RestApiController;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@RequiredArgsConstructor
@RestApiController("/api/v1/gitlab")
public class GitLabController {
    private final GitLabService gitLabService;

    @GetMapping("/merge-requests/my")
    public ResponseEntity<List<MergeRequestDto>> getMyMergeRequests() {
        List<MergeRequestDto> mergeRequests = gitLabService.getMyMergeRequests();
        return ResponseEntity.ok(mergeRequests);
    }

    @GetMapping("/merge-requests/reviews")
    public ResponseEntity<List<MergeRequestDto>> getPendingReviews() {
        List<MergeRequestDto> pendingReviews = gitLabService.getPendingReviews();
        return ResponseEntity.ok(pendingReviews);
    }

    @GetMapping("/pipelines")
    public ResponseEntity<List<ProtectedBranchPipelineDto>> getProtectedBranchPipelines() {
        List<ProtectedBranchPipelineDto> pipelines = gitLabService.getProtectedBranchPipelines();
        return ResponseEntity.ok(pipelines);
    }
}
