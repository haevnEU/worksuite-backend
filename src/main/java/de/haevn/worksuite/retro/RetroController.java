package de.haevn.worksuite.retro;

import de.haevn.worksuite.common.RestApiController;
import de.haevn.worksuite.common.exceptions.BadRequestException;
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
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Controller exposing REST endpoints for managing sprint retrospectives and feedback categories.
 *
 * <p>Example HTTP requests:
 * <pre>{@code
 * GET /api/v1/retros
 * POST /api/v1/retros?name=Sprint+25
 * GET /api/v1/retros/c0a80101-8b9a-4e2b-9e9b-9c7f6d4e5f6a
 * PUT /api/v1/retros/c0a80101-8b9a-4e2b-9e9b-9c7f6d4e5f6a/positive
 * DELETE /api/v1/retros/c0a80101-8b9a-4e2b-9e9b-9c7f6d4e5f6a
 * }</pre>
 */
@Log4j2
@Tag(name = "Retrospectives", description = "Endpoints for managing agile sprint retrospectives and action items")
@RestApiController("/api/v1/retros")
@RequiredArgsConstructor
public class RetroController {

    private static final String LIST_POSITIVE = "positive";
    private static final String LIST_NEGATIVE = "negative";
    private static final String LIST_ACTION = "action";

    private final RetroService retroService;

    /**
     * Retrieves all saved sprint retrospectives.
     *
     * @return list of {@link RetroDTO} records
     */
    @Operation(summary = "Get all retrospectives", description = "Retrieves an array of all sprint retrospectives.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Retrospectives retrieved successfully",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            array = @ArraySchema(schema = @Schema(implementation = RetroDTO.class))))})
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<RetroDTO> getAllRetros() {
        log.info("Fetching all sprint retrospectives");
        return retroService.getAllRetros();
    }

    /**
     * Creates a new sprint retrospective.
     *
     * @param name sprint name or title
     * @return the created {@link RetroDTO}
     */
    @Operation(summary = "Create retrospective", description = "Creates a new sprint retrospective entry.")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Retrospective created successfully",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = RetroDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid sprint name supplied",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))})
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public RetroDTO createRetro(
        @Parameter(description = "Sprint name or identifier", example = "Sprint 25") @RequestParam final String name) {
        log.info("Creating retrospective for sprint: '{}'", name);
        return retroService.createRetro(name);
    }

    /**
     * Retrieves a single sprint retrospective by its unique ID.
     *
     * @param id retrospective unique identifier
     * @return the matching {@link RetroDTO}
     */
    @Operation(summary = "Get retrospective by ID", description = "Retrieves details of a specific retrospective.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Retrospective found and returned",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = RetroDTO.class))),
        @ApiResponse(responseCode = "404", description = "Retrospective not found",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))})
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public RetroDTO getRetro(
        @Parameter(description = "Retrospective unique identifier", example = "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d")
        @PathVariable final UUID id) {
        log.info("Fetching retrospective with ID: '{}'", id);
        return retroService.getRetroWeek(id);
    }

    /**
     * Deletes a retrospective by ID.
     *
     * @param id retrospective unique identifier
     * @return empty response with HTTP 204 No Content
     */
    @Operation(summary = "Delete retrospective",
        description = "Permanently deletes a retrospective and its associated items.")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Retrospective deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Retrospective not found",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))})
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRetro(
        @Parameter(description = "Retrospective unique identifier", example = "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d")
        @PathVariable final UUID id) {
        log.info("Deleting retrospective with ID: '{}'", id);
        retroService.deleteRetro(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Appends an item to one of the retrospective's feedback lists.
     *
     * @param id retrospective unique identifier
     * @param list category name ({@code "positive"}, {@code "negative"}, or {@code "action"})
     * @param item raw item text
     */
    @Operation(summary = "Add item to retrospective list",
        description = "Appends a feedback entry to the positive, negative, or action items list.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Item added successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid list type specified",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Retrospective not found",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))})
    @PutMapping("/{id}/{list}")
    public void addToList(
        @Parameter(description = "Retrospective unique identifier", example = "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d")
        @PathVariable final UUID id,
        @Parameter(description = "Target list type ('positive', 'negative', 'action')", example = "positive")
        @PathVariable final String list, @RequestBody final String item) {
        log.info("Adding entry to '{}' list for retro ID: '{}'", list, id);
        switch (list.toLowerCase()) {
            case LIST_POSITIVE -> retroService.addToPositiveList(id, item);
            case LIST_NEGATIVE -> retroService.addToNegativeList(id, item);
            case LIST_ACTION -> retroService.addToActionItemList(id, item);
            default -> throw new BadRequestException("Invalid list type: " + list);
        }
    }

    /**
     * Removes an item from one of the retrospective's feedback lists.
     *
     * @param id retrospective unique identifier
     * @param list category name ({@code "positive"}, {@code "negative"}, or {@code "action"})
     * @param item raw item text to remove
     */
    @Operation(summary = "Remove item from retrospective list",
        description = "Removes an item from the positive, negative, or action items list.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Item removed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid list type specified",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Retrospective not found",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))})
    @DeleteMapping("/{id}/{list}")
    public void removeFromList(
        @Parameter(description = "Retrospective unique identifier", example = "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d")
        @PathVariable final UUID id,
        @Parameter(description = "Target list type ('positive', 'negative', 'action')", example = "action")
        @PathVariable final String list, @RequestBody final String item) {
        log.info("Removing entry from '{}' list for retro ID: '{}'", list, id);
        switch (list.toLowerCase()) {
            case LIST_POSITIVE -> retroService.removeFromPositiveList(id, item);
            case LIST_NEGATIVE -> retroService.removeFromNegativeList(id, item);
            case LIST_ACTION -> retroService.removeFromActionItemList(id, item);
            default -> throw new BadRequestException("Invalid list type: " + list);
        }
    }
}