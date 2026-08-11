package de.haevn.worksuite.download;

import de.haevn.worksuite.common.RestApiController;
import de.haevn.worksuite.common.exceptions.ErrorResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Controller exposing REST endpoints for generating exports and streaming downloaded resources.
 *
 * <p>Example HTTP request:
 * <pre>{@code
 * POST /api/v1/download/WEEKLY_MEETING_PROTOCOL
 * Content-Type: application/json
 *
 * {
 *   "id": "c0a80101-8b9a-4e2b-9e9b-9c7f6d4e5f6a",
 *   "isDraft": false
 * }
 * }</pre>
 */
@Log4j2
@Tag(name = "Downloads & Exports", description = "Endpoints for compiling PDF exports and downloading shared files")
@RestApiController("/api/v1/download")
@RequiredArgsConstructor
public class DownloadController {

    private final DownloadService downloadService;

    /**
     * Executes synchronous resource generation and file download dispatching.
     *
     * @param type target {@link DownloadModule} operation
     * @param dto request payload containing entity IDs and rendering preferences
     * @return binary file stream response
     * @throws IOException if reading or transferring data fails
     */
    @Operation(summary = "Download or export resource",
        description = "Generates and streams requested documents, PDFs, or remote attachments based on the specified module type.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "File generated and streamed successfully",
        content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE)),
        @ApiResponse(responseCode = "404", description = "Target entity or resource not found",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request payload parameters",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))})
    @PostMapping(value = "/{type}",
        produces = {MediaType.APPLICATION_PDF_VALUE, MediaType.APPLICATION_OCTET_STREAM_VALUE})
    public ResponseEntity<Resource> downloadSynchronous(
        @Parameter(description = "Export module category", example = "WEEKLY_MEETING_PROTOCOL") @PathVariable
        final DownloadModule type, @RequestBody final RequestDTO dto) throws IOException {
        log.info("Processing synchronous download request for module: '{}'", type);
        return downloadService.downloadSynchronous(type, dto);
    }
}