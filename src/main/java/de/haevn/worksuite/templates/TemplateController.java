package de.haevn.worksuite.templates;

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
import java.util.Optional;
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
 * Controller exposing REST endpoints for managing and sharing text templates.
 *
 * <p>Example HTTP requests:
 * <pre>{@code
 * GET /api/v1/share/templates?tag=git
 * POST /api/v1/share/templates
 * GET /api/v1/share/templates/c0a80101-8b9a-4e2b-9e9b-9c7f6d4e5f6a
 * PUT /api/v1/share/templates/c0a80101-8b9a-4e2b-9e9b-9c7f6d4e5f6a
 * DELETE /api/v1/share/templates/c0a80101-8b9a-4e2b-9e9b-9c7f6d4e5f6a
 * }</pre>
 */
@Log4j2
@Tag(name = "Templates", description = "Endpoints for creating, updating, and querying text and boilerplate templates")
@RestApiController("/api/v1/share/templates")
@RequiredArgsConstructor
public class TemplateController {

    private final TemplateService templateService;

    /**
     * Retrieves all templates with optional tag filtering.
     *
     * @param tag optional tag filter
     * @return list of matching {@link TemplateShareDTO} records
     */
    @Operation(summary = "Get all templates",
        description = "Retrieves an array of all text templates, optionally filtered by tag.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Templates retrieved successfully",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            array = @ArraySchema(schema = @Schema(implementation = TemplateShareDTO.class))))})
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<TemplateShareDTO> getAllTemplates(
        @Parameter(description = "Optional tag filter", example = "git") @RequestParam final Optional<String> tag) {
        log.info("Request received to fetch templates with tag filter present: {}", tag.isPresent());
        return templateService.getAllTemplates(tag);
    }

    /**
     * Retrieves a single template by its identifier.
     *
     * @param id template unique identifier
     * @return the matching {@link TemplateShareDTO}
     */
    @Operation(summary = "Get template by ID",
        description = "Retrieves a specific template matching the provided UUID.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Template retrieved successfully",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = TemplateShareDTO.class))),
        @ApiResponse(responseCode = "404", description = "Template not found",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))})
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public TemplateShareDTO getTemplate(
        @Parameter(description = "Template unique identifier", example = "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d")
        @PathVariable final UUID id) {
        log.info("Request received to fetch template with ID: '{}'", id);
        return templateService.getTemplate(id);
    }

    /**
     * Creates and shares a new template.
     *
     * @param templateShareDTO template creation payload
     * @return the created {@link TemplateShareDTO}
     */
    @Operation(summary = "Create template",
        description = "Creates a new template and broadcasts a WebSocket notification.")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Template created successfully",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = TemplateShareDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid template payload supplied",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))})
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public TemplateShareDTO createTemplate(@RequestBody final TemplateShareDTO templateShareDTO) {
        log.info("Request received to create a new template");
        return templateService.shareTemplate(templateShareDTO);
    }

    /**
     * Updates an existing template.
     *
     * @param id template unique identifier
     * @param templateShareDTO updated template data
     * @return the updated {@link TemplateShareDTO}
     */
    @Operation(summary = "Update template",
        description = "Updates title, content, platform, or tags of an existing template.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Template updated successfully",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = TemplateShareDTO.class))),
        @ApiResponse(responseCode = "404", description = "Template not found",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))})
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public TemplateShareDTO updateTemplate(
        @Parameter(description = "Template unique identifier", example = "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d")
        @PathVariable final UUID id, @RequestBody final TemplateShareDTO templateShareDTO) {
        log.info("Request received to update template with ID: '{}'", id);
        return templateService.updateTemplate(id, templateShareDTO);
    }

    /**
     * Deletes a template by its unique identifier.
     *
     * @param id template unique identifier
     * @return empty response with HTTP 204 No Content
     */
    @Operation(summary = "Delete template", description = "Permanently deletes a template from the system.")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Template deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Template not found",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))})
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTemplate(
        @Parameter(description = "Template unique identifier", example = "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d")
        @PathVariable final UUID id) {
        log.info("Request received to delete template with ID: '{}'", id);
        templateService.deleteTemplate(id);
        return ResponseEntity.noContent().build();
    }
}