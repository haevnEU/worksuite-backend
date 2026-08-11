package de.haevn.worksuite.cheatsheet;

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
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * REST controller exposing endpoints to retrieve, create, update, and remove developer cheat sheet items.
 */
@Log4j2
@Tag(name = "Cheat Sheets", description = "Endpoints for managing command syntax, flag references, and code examples")
@RestApiController("/api/v1/cheat-sheet")
@RequiredArgsConstructor
public class CheatSheetController {

    private final CheatSheetService cheatSheetService;

    @Operation(summary = "List cheat sheets",
        description = "Retrieves all cheat sheet items, optionally filtered by category.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Cheat sheets retrieved successfully",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            array = @ArraySchema(schema = @Schema(implementation = CheatSheetResponseDto.class))))})
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<CheatSheetResponseDto> getAllCheatSheets(
        @Parameter(description = "Filter by category key (e.g. git, docker, bash)", example = "git")
        @RequestParam(name = "category", required = false) final String category) {
        log.info("Request received to fetch cheat sheets. Category filter: '{}'", category);
        return cheatSheetService.getAllCheatSheets(category);
    }

    @Operation(summary = "Get cheat sheet by ID",
        description = "Fetches a single cheat sheet entry by its unique identifier.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Cheat sheet found and returned",
        content = @Content(schema = @Schema(implementation = CheatSheetResponseDto.class))),
        @ApiResponse(responseCode = "404", description = "Cheat sheet not found",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))})
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CheatSheetResponseDto getCheatSheetById(
        @Parameter(description = "Cheat sheet unique identifier", example = "550e8400-e29b-41d4-a716-446655440000")
        @PathVariable final UUID id) {
        log.info("Request received to fetch cheat sheet by ID: '{}'", id);
        return cheatSheetService.getCheatSheetById(id);
    }

    @Operation(summary = "Create cheat sheet", description = "Persists a new cheat sheet entry.")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Cheat sheet created successfully",
        content = @Content(schema = @Schema(implementation = CheatSheetResponseDto.class))),
        @ApiResponse(responseCode = "400", description = "Validation failure or malformed payload",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))})
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public CheatSheetResponseDto createCheatSheet(@Valid @RequestBody final CheatSheetRequestDto requestDto) {
        log.info("Request received to create cheat sheet titled '{}' in category '{}'", requestDto.title(),
            requestDto.category());
        return cheatSheetService.createCheatSheet(requestDto);
    }

    @Operation(summary = "Update cheat sheet", description = "Updates an existing cheat sheet entry.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Cheat sheet updated successfully",
        content = @Content(schema = @Schema(implementation = CheatSheetResponseDto.class))),
        @ApiResponse(responseCode = "400", description = "Validation failure or malformed payload",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Cheat sheet not found",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))})
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public CheatSheetResponseDto updateCheatSheet(
        @Parameter(description = "Cheat sheet unique identifier", example = "550e8400-e29b-41d4-a716-446655440000")
        @PathVariable final UUID id, @Valid @RequestBody final CheatSheetRequestDto requestDto) {
        log.info("Request received to update cheat sheet ID: '{}'", id);
        return cheatSheetService.updateCheatSheet(id, requestDto);
    }

    @Operation(summary = "Delete cheat sheet", description = "Permanently removes a cheat sheet entry by ID.")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Cheat sheet deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Cheat sheet not found",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))})
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCheatSheet(
        @Parameter(description = "Cheat sheet unique identifier", example = "550e8400-e29b-41d4-a716-446655440000")
        @PathVariable final UUID id) {
        log.info("Request received to delete cheat sheet ID: '{}'", id);
        cheatSheetService.deleteCheatSheet(id);
    }
}