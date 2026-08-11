package de.haevn.worksuite.vcs;

import de.haevn.worksuite.common.RestApiController;
import de.haevn.worksuite.common.exceptions.ErrorResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller exposing REST endpoints for inspecting GitLab repositories, Merge Requests, reviews, and CI/CD pipelines.
 *
 * <p>Example HTTP requests:
 * <pre>{@code
 * GET /api/v1/vcs/merge-requests/my
 * GET /api/v1/vcs/merge-requests/reviews
 * GET /api/v1/vcs/pipelines
 * GET /api/v1/vcs/repositories
 * }</pre>
 */
@Log4j2
@Tag(name = "VCS Integration",
    description = "Endpoints for interacting with GitLab repositories, Merge Requests, and pipelines")
@RestApiController("/api/v1/vcs")
@RequiredArgsConstructor
public class VcsController {

    private final VcsService vcsService;

    /**
     * Retrieves all active Merge Requests authored by the authenticated user.
     *
     * @return {@link ResponseEntity} containing a list of {@link MergeRequestDto} objects
     */
    @Operation(summary = "Get my Merge Requests",
        description = "Retrieves all open Merge Requests authored by the currently authenticated user.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Merge Requests retrieved successfully",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            array = @ArraySchema(schema = @Schema(implementation = MergeRequestDto.class)))),
        @ApiResponse(responseCode = "502", description = "Failed to communicate with GitLab",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))})
    @GetMapping(value = "/merge-requests/my", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<MergeRequestDto>> getMyMergeRequests() {
        log.info("Request received to fetch authored Merge Requests");
        final List<MergeRequestDto> mergeRequests = vcsService.getMyMergeRequests();
        return ResponseEntity.ok(mergeRequests);
    }

    /**
     * Retrieves all Merge Requests where the authenticated user is assigned as a reviewer.
     *
     * @return {@link ResponseEntity} containing a list of pending {@link MergeRequestDto} objects
     */
    @Operation(summary = "Get pending reviews",
        description = "Retrieves all open Merge Requests where the active user is assigned to perform a code review.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Pending reviews retrieved successfully",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            array = @ArraySchema(schema = @Schema(implementation = MergeRequestDto.class)))),
        @ApiResponse(responseCode = "502", description = "Failed to communicate with GitLab",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))})
    @GetMapping(value = "/merge-requests/reviews", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<MergeRequestDto>> getPendingReviews() {
        log.info("Request received to fetch pending code reviews");
        final List<MergeRequestDto> pendingReviews = vcsService.getPendingReviews();
        return ResponseEntity.ok(pendingReviews);
    }

    /**
     * Retrieves the latest CI/CD pipeline execution statuses across protected branches.
     *
     * @return {@link ResponseEntity} containing a list of {@link ProtectedBranchPipelineDto} objects
     */
    @Operation(summary = "Get protected branch pipelines",
        description = "Retrieves the latest CI/CD pipeline statuses across protected branches (such as main and release branches).")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Protected branch pipelines retrieved successfully",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            array = @ArraySchema(schema = @Schema(implementation = ProtectedBranchPipelineDto.class)))),
        @ApiResponse(responseCode = "502", description = "Failed to communicate with GitLab",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))})
    @GetMapping(value = "/pipelines", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ProtectedBranchPipelineDto>> getProtectedBranchPipelines() {
        log.info("Request received to fetch protected branch pipelines");
        final List<ProtectedBranchPipelineDto> pipelines = vcsService.getProtectedBranchPipelines();
        return ResponseEntity.ok(pipelines);
    }

    /**
     * Retrieves all repositories accessible by the user with their current open Merge Requests.
     *
     * @return {@link ResponseEntity} containing a list of {@link GitLabRepository} objects
     */
    @Operation(summary = "Get repositories",
        description = "Retrieves accessible GitLab repositories along with health statuses and open Merge Requests.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Repositories retrieved successfully",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            array = @ArraySchema(schema = @Schema(implementation = GitLabRepository.class)))),
        @ApiResponse(responseCode = "502", description = "Failed to communicate with GitLab",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))})
    @GetMapping(value = "/repositories", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<GitLabRepository>> getRepositories() {
        log.info("Request received to fetch repositories");
        final List<GitLabRepository> repositories = vcsService.getRepositories();
        return ResponseEntity.ok(repositories);
    }
}