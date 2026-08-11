package de.haevn.worksuite.review;

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
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Controller exposing REST endpoints for managing sprint review presentation topics and demo notes.
 *
 * <p>Example HTTP requests:
 * <pre>{@code
 * GET /api/v1/reviews?archived=false
 * POST /api/v1/reviews
 * PUT /api/v1/reviews/c0a80101-8b9a-4e2b-9e9b-9c7f6d4e5f6a
 * PATCH /api/v1/reviews/c0a80101-8b9a-4e2b-9e9b-9c7f6d4e5f6a/archive
 * DELETE /api/v1/reviews/c0a80101-8b9a-4e2b-9e9b-9c7f6d4e5f6a
 * }</pre>
 */
@Slf4j
@Tag(name = "Reviews", description = "Endpoints for creating and managing sprint review topics and demo notes")
@RestApiController("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * Retrieves sprint review entries filtered by their archive status.
     *
     * @param archived whether to fetch archived entries (defaults to {@code false})
     * @return list of matching {@link ReviewResponseDto} entries
     */
    @Operation(summary = "Get review items",
        description = "Retrieves all sprint review topics filtered by archived status.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Reviews retrieved successfully",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            array = @ArraySchema(schema = @Schema(implementation = ReviewResponseDto.class))))})
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ReviewResponseDto> getReviews(
        @Parameter(description = "Whether to fetch archived review items", example = "false")
        @RequestParam(defaultValue = "false") final boolean archived) {
        log.info("Request received to fetch reviews with archived='{}'", archived);
        return reviewService.getReviews(archived);
    }

    /**
     * Creates a new sprint review entry.
     *
     * @param request the review creation payload
     */
    @Operation(summary = "Create review", description = "Creates and stores a new review entry.")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Review created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid validation parameters supplied",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))})
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public void createReview(@Valid @RequestBody final CreateReviewRequestDto request) {
        log.info("Request received to create review for ticket: '{}'", request.ticketNumber());
        reviewService.createReview(request);
    }

    /**
     * Updates an existing sprint review entry.
     *
     * @param id the unique identifier of the review
     * @param request the updated payload
     */
    @Operation(summary = "Update review",
        description = "Updates an existing review item with new content and attributes.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Review updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input payload",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Review not found",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))})
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void updateReview(
        @Parameter(description = "Review unique identifier", example = "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d")
        @PathVariable final UUID id, @Valid @RequestBody final CreateReviewRequestDto request) {
        log.info("Request received to update review with ID: '{}'", id);
        reviewService.updateReview(id, request);
    }

    /**
     * Toggles the archive status of a review item.
     *
     * @param id the unique identifier of the review
     */
    @Operation(summary = "Toggle archive status",
        description = "Inverts the archived status of the specified review entry.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Archive status toggled successfully"),
        @ApiResponse(responseCode = "404", description = "Review not found",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))})
    @PatchMapping("/{id}/archive")
    public void toggleArchive(
        @Parameter(description = "Review unique identifier", example = "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d")
        @PathVariable final UUID id) {
        log.info("Request received to toggle archive state for review ID: '{}'", id);
        reviewService.toggleArchive(id);
    }

    /**
     * Permanently deletes a review entry.
     *
     * @param id the unique identifier of the review to delete
     */
    @Operation(summary = "Delete review", description = "Permanently removes a review entry from the system.")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Review deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Review not found",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))})
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReview(
        @Parameter(description = "Review unique identifier", example = "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d")
        @PathVariable final UUID id) {
        log.info("Request received to delete review with ID: '{}'", id);
        reviewService.deleteReview(id);
    }
}