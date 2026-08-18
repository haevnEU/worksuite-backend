package de.haevn.worksuite.vcs;

import de.haevn.worksuite.common.RestApiController;
import de.haevn.worksuite.common.exceptions.ErrorResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller exposing REST endpoints for interacting with pluggable VCS providers (GitLab, GitHub).
 *
 * <p>Example HTTP requests:
 * <pre>{@code
 * GET /api/v1/vcs/merge-requests/my?provider=GITLAB
 * GET /api/v1/vcs/merge-requests/reviews
 * GET /api/v1/vcs/pipelines?provider=GITHUB
 * GET /api/v1/vcs/repositories
 * }</pre>
 */
@Log4j2
@Tag(name = "VCS Integration",
    description = "Endpoints for interacting with VCS repositories, Merge Requests, and pipelines")
@RestApiController("/api/v1/vcs")
@RequiredArgsConstructor
public class VcsController {

    private final VcsService vcsService;

    @Operation(summary = "Create Merge Request",
        description = "Creates a new Merge Request on the specified VCS provider.")
    @PostMapping(value = "/merge-requests", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> createMergeRequest(
        @Parameter(description = "VCS provider type (defaults to GITLAB)", example = "GITLAB")
        @RequestParam(name = "provider", defaultValue = "GITLAB") final VcsProvider provider,
        @Parameter(description = "Associated ticket or issue ID", example = "12345", required = true)
        @RequestParam(name = "ticketId") final long ticketId, @RequestBody final MrProtocolRequest protocol) {
        log.info("Creating Merge Request for ticket #{} using provider {}", ticketId, provider);
        final String webUrl = vcsService.createMergeRequest(provider, ticketId, protocol);
        return ResponseEntity.ok(webUrl);
    }

    @Operation(summary = "Get my Merge Requests",
        description = "Retrieves all open Merge Requests authored by the current user.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Merge Requests retrieved successfully",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            array = @ArraySchema(schema = @Schema(implementation = MergeRequestDto.class)))),
        @ApiResponse(responseCode = "502", description = "Failed to communicate with VCS provider",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))})
    @GetMapping(value = "/merge-requests/my", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<MergeRequestDto>> getMyMergeRequests(
        @Parameter(description = "VCS provider type (defaults to GITLAB)", example = "GITLAB")
        @RequestParam(name = "provider", defaultValue = "GITLAB") final VcsProvider provider) {
        log.info("Fetching authored Merge Requests for provider {}", provider);
        final List<MergeRequestDto> mergeRequests = vcsService.getMyMergeRequests(provider);
        return ResponseEntity.ok(mergeRequests);
    }

    @Operation(summary = "Get pending reviews", description = "Retrieves all open Merge Requests assigned for review.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Pending reviews retrieved successfully",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            array = @ArraySchema(schema = @Schema(implementation = MergeRequestDto.class)))),
        @ApiResponse(responseCode = "502", description = "Failed to communicate with VCS provider",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))})
    @GetMapping(value = "/merge-requests/reviews", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<MergeRequestDto>> getPendingReviews(
        @Parameter(description = "VCS provider type (defaults to GITLAB)", example = "GITLAB")
        @RequestParam(name = "provider", defaultValue = "GITLAB") final VcsProvider provider) {
        log.info("Fetching pending reviews for provider {}", provider);
        final List<MergeRequestDto> pendingReviews = vcsService.getPendingReviews(provider);
        return ResponseEntity.ok(pendingReviews);
    }

    @Operation(summary = "Get protected branch pipelines",
        description = "Retrieves the latest CI/CD pipelines across protected branches.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Pipelines retrieved successfully",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            array = @ArraySchema(schema = @Schema(implementation = PipelineDTO.class)))),
        @ApiResponse(responseCode = "502", description = "Failed to communicate with VCS provider",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))})
    @GetMapping(value = "/pipelines", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PipelineDTO>> getProtectedBranchPipelines(
        @Parameter(description = "VCS provider type (defaults to GITLAB)", example = "GITLAB")
        @RequestParam(name = "provider", defaultValue = "GITLAB") final VcsProvider provider) {
        log.info("Fetching protected branch pipelines for provider {}", provider);
        final List<PipelineDTO> pipelines = vcsService.getProtectedBranchPipelines(provider);
        return ResponseEntity.ok(pipelines);
    }

    @Operation(summary = "Get repositories",
        description = "Retrieves accessible repositories from the selected VCS provider.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Repositories retrieved successfully",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            array = @ArraySchema(schema = @Schema(implementation = RepositoryDTO.class)))),
        @ApiResponse(responseCode = "502", description = "Failed to communicate with VCS provider",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))})
    @GetMapping(value = "/repositories", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<RepositoryDTO>> getRepositories(
        @Parameter(description = "VCS provider type (defaults to GITLAB)", example = "GITLAB")
        @RequestParam(name = "provider", defaultValue = "GITLAB") final VcsProvider provider) {
        log.info("Fetching repositories for provider {}", provider);
        final List<RepositoryDTO> repositories = vcsService.getRepositories(provider);
        return ResponseEntity.ok(repositories);
    }
}