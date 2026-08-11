package de.haevn.worksuite.notes;

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
 * Controller exposing REST endpoints for managing notes and text snippets.
 *
 * <p>Example HTTP requests:
 * <pre>{@code
 * GET /api/v1/notes
 * POST /api/v1/notes
 * GET /api/v1/notes/c0a80101-8b9a-4e2b-9e9b-9c7f6d4e5f6a
 * PUT /api/v1/notes/c0a80101-8b9a-4e2b-9e9b-9c7f6d4e5f6a
 * DELETE /api/v1/notes/c0a80101-8b9a-4e2b-9e9b-9c7f6d4e5f6a
 * }</pre>
 */
@Log4j2
@Tag(name = "Notes", description = "Endpoints for creating, managing, and retrieving notes")
@RestApiController("/api/v1/notes")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;

    /**
     * Creates a new note.
     *
     * @param noteDTO note payload to store
     * @return the saved {@link NoteDTO}
     */
    @Operation(summary = "Create note",
        description = "Persists a new note entry and triggers a WebSocket notification.")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Note created successfully",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = NoteDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid note payload provided",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))})
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public NoteDTO createNote(@RequestBody final NoteDTO noteDTO) {
        log.info("Request received to create a new note");
        return noteService.create(noteDTO);
    }

    /**
     * Retrieves all saved notes.
     *
     * @return list of {@link NoteDTO} entries
     */
    @Operation(summary = "Get all notes", description = "Retrieves an array of all existing note records.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Notes retrieved successfully",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            array = @ArraySchema(schema = @Schema(implementation = NoteDTO.class))))})
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<NoteDTO> getAllNotes() {
        log.info("Request received to fetch all notes");
        return noteService.getAll();
    }

    /**
     * Retrieves a single note by its unique identifier.
     *
     * @param id the unique note identifier
     * @return the found {@link NoteDTO}
     */
    @Operation(summary = "Get note by ID", description = "Retrieves a specific note matching the provided UUID.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Note found and returned",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = NoteDTO.class))),
        @ApiResponse(responseCode = "404", description = "Note not found",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))})
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public NoteDTO getNoteById(
        @Parameter(description = "Note unique identifier", example = "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d")
        @PathVariable final UUID id) {
        log.info("Request received to fetch note with ID: '{}'", id);
        return noteService.getById(id);
    }

    /**
     * Updates an existing note.
     *
     * @param id identifier of the note to modify
     * @param noteDTO updated note payload
     * @return the updated {@link NoteDTO}
     */
    @Operation(summary = "Update note",
        description = "Updates title, content, and ticket references of an existing note.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Note updated successfully",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = NoteDTO.class))),
        @ApiResponse(responseCode = "404", description = "Note with specified ID not found",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))})
    @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public NoteDTO updateNote(
        @Parameter(description = "Note unique identifier", example = "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d")
        @PathVariable final UUID id, @RequestBody final NoteDTO noteDTO) {
        log.info("Request received to update note with ID: '{}'", id);
        return noteService.update(id, noteDTO);
    }

    /**
     * Deletes a note by its identifier.
     *
     * @param id identifier of the note to remove
     * @return empty response with HTTP 204 No Content
     */
    @Operation(summary = "Delete note", description = "Permanently deletes a note from the system.")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Note deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Note not found",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))})
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNote(
        @Parameter(description = "Note unique identifier", example = "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d")
        @PathVariable final UUID id) {
        log.info("Request received to delete note with ID: '{}'", id);
        noteService.delete(id);
        return ResponseEntity.noContent().build();
    }
}