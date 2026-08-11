package de.haevn.worksuite.share;

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
import java.io.IOException;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;

/**
 * Controller exposing REST endpoints for uploading, listing, and managing shared workspace files.
 *
 * <p>Example HTTP requests:
 * <pre>{@code
 * POST /api/v1/share (multipart/form-data)
 * GET /api/v1/share
 * DELETE /api/v1/share/c0a80101-8b9a-4e2b-9e9b-9c7f6d4e5f6a
 * }</pre>
 */
@Log4j2
@Tag(name = "File Share", description = "Endpoints for uploading, listing, and removing shared files")
@RestApiController("/api/v1/share")
@RequiredArgsConstructor
public class ShareController {

    private final ShareService shareService;

    /**
     * Uploads and stores one or more shared files.
     *
     * @param files list of multipart file uploads
     */
    @Operation(summary = "Upload files", description = "Uploads multiple files to the shared workspace storage.")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Files uploaded and stored successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid file payload",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
        @ApiResponse(responseCode = "500", description = "File storage error occurred",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))})
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public void share(
        @Parameter(description = "List of files to upload") @RequestParam("files") final List<MultipartFile> files) {
        log.info("Request received to upload {} files", files != null ? files.size() : 0);
        shareService.share(files);
    }

    /**
     * Lists metadata for all active shared files.
     *
     * @return list of {@link FileMeta} records
     */
    @Operation(summary = "List shared files",
        description = "Retrieves an array of all active shared file metadata records.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Shared files retrieved successfully",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            array = @ArraySchema(schema = @Schema(implementation = FileMeta.class))))})
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<FileMeta> getAllSharedFiles() {
        log.info("Request received to fetch all shared files");
        return shareService.getAllSharedFiles();
    }

    /**
     * Deletes a shared file by its unique identifier.
     *
     * @param id file unique identifier
     * @throws IOException if physical deletion from disk fails
     */
    @Operation(summary = "Delete shared file", description = "Permanently deletes a shared file and its metadata.")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Shared file deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Shared file not found",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))})
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFile(
        @Parameter(description = "Shared file unique identifier", example = "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d")
        @PathVariable final UUID id) throws IOException {
        log.info("Request received to delete shared file with ID: '{}'", id);
        shareService.deleteFile(id);
    }
}