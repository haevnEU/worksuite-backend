package de.haevn.worksuite.about;

import de.haevn.worksuite.common.RestApiController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * REST API controller providing system diagnostic endpoints, application info, and liveness probes.
 *
 * <p>Example HTTP invocation:
 * <pre>{@code
 * GET /api/v1/about HTTP/1.1
 * Host: localhost:8080
 * Accept: application/json
 * }</pre>
 */
@Tag(name = "System Info & Diagnostics",
    description = "Endpoints exposing build metadata, environment diagnostics, and health probes")
@Log4j2
@RequiredArgsConstructor
@RestApiController("/api/v1/about")
public class AboutController {

    private final AboutService aboutService;

    /**
     * Retrieves runtime information, build properties, and connectivity metrics via {@link AboutService}.
     *
     * @return a {@link ResponseEntity} wrapping the {@link AboutSystemInfoResponse}
     */
    @Operation(summary = "Get system diagnostics and build info",
        description = "Returns runtime status, build version, environment configurations, and database metrics.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "System info successfully retrieved",
        content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = AboutSystemInfoResponse.class))),
        @ApiResponse(responseCode = "500", description = "Internal error during diagnostics collection",
            content = @Content)})
    @GetMapping
    public ResponseEntity<AboutSystemInfoResponse> getSystemInfo() {
        final AboutSystemInfoResponse systemInfo = aboutService.getSystemInfo();
        return ResponseEntity.ok(systemInfo);
    }

    /**
     * Lightweight liveness probe endpoint for connectivity testing and keep-alive checks.
     *
     * @return a constant response string {@code "pongs"}
     */
    @Operation(summary = "Service liveness ping",
        description = "Lightweight endpoint returning a plain string to confirm service availability.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Service is responsive",
        content = @Content(mediaType = "text/plain", schema = @Schema(type = "string", example = "pongs")))})
    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("pongs");
    }
}