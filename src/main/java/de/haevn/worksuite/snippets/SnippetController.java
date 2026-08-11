package de.haevn.worksuite.snippets;

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
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Controller exposing REST endpoints for creating, updating, and retrieving code snippets.
 *
 * <p>Example HTTP requests:
 * <pre>{@code
 * GET /api/v1/snippets
 * POST /api/v1/snippets
 * GET /api/v1/snippets/c0a80101-8b9a-4e2b-9e9b-9c7f6d4e5f6a
 * PUT /api/v1/snippets/c0a80101-8b9a-4e2b-9e9b-9c7f6d4e5f6a
 * DELETE /api/v1/snippets/c0a80101-8b9a-4e2b-9e9b-9c7f6d4e5f6a
 * }</pre>
 */
@Log4j2
@Tag(name = "Snippets", description = "Endpoints for managing reusable code and text snippets")
@RestApiController("/api/v1/snippets")
@RequiredArgsConstructor
public class SnippetController {

    private final SnippetService snippetService;

    /**
     * Retrieves all shared snippets.
     *
     * @return list of {@link SnippetShareDTO} records
     */
    @Operation(summary = "Get all snippets", description = "Retrieves an array of all existing code snippets.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Snippets retrieved successfully",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            array = @ArraySchema(schema = @Schema(implementation = SnippetShareDTO.class))))})
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<SnippetShareDTO> getAllSnippets() {
        log.info("Request received to fetch all snippets");
        return snippetService.getAllSnippets();
    }

    /**
     * Creates and shares a new snippet.
     *
     * @param snippetShareDTO snippet creation payload
     * @return the created {@link SnippetShareDTO}
     */
    @Operation(summary = "Create snippet",
        description = "Creates a new snippet and broadcasts a WebSocket notification.")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Snippet created successfully",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = SnippetShareDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid snippet payload provided",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))})
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public SnippetShareDTO createSnippet(@RequestBody final SnippetShareDTO snippetShareDTO) {
        log.info("Request received to create a new snippet");
        return snippetService.shareSnippet(snippetShareDTO);
    }

    /**
     * Retrieves a single snippet by its identifier.
     *
     * @param id snippet unique identifier
     * @return the matching {@link SnippetShareDTO}
     */
    @Operation(summary = "Get snippet by ID", description = "Retrieves a specific snippet matching the provided UUID.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Snippet retrieved successfully",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = SnippetShareDTO.class))),
        @ApiResponse(responseCode = "404", description = "Snippet not found",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))})
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public SnippetShareDTO getSnippet(
        @Parameter(description = "Snippet unique identifier", example = "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d")
        @PathVariable final UUID id) {
        log.info("Request received to fetch snippet with ID: '{}'", id);
        return snippetService.getSnippet(id);
    }

    /**
     * Updates an existing snippet.
     *
     * @param id snippet unique identifier
     * @param snippetShareDTO updated snippet data
     * @return the updated {@link SnippetShareDTO}
     */
    @Operation(summary = "Update snippet",
        description = "Updates title, content, language, or tags of an existing snippet.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Snippet updated successfully",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = SnippetShareDTO.class))),
        @ApiResponse(responseCode = "404", description = "Snippet not found",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))})
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public SnippetShareDTO updateSnippet(
        @Parameter(description = "Snippet unique identifier", example = "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d")
        @PathVariable final UUID id, @RequestBody final SnippetShareDTO snippetShareDTO) {
        log.info("Request received to update snippet with ID: '{}'", id);
        return snippetService.updateSnippet(id, snippetShareDTO);
    }

    /**
     * Deletes a snippet by its unique identifier.
     *
     * @param id snippet unique identifier
     * @return empty response with HTTP 204 No Content
     */
    @Operation(summary = "Delete snippet", description = "Permanently deletes a snippet from the system.")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Snippet deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Snippet not found",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))})
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSnippet(
        @Parameter(description = "Snippet unique identifier", example = "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d")
        @PathVariable final UUID id) {
        log.info("Request received to delete snippet with ID: '{}'", id);
        snippetService.deleteSnippet(id);
        return ResponseEntity.noContent().build();
    }
}