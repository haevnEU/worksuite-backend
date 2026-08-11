package de.haevn.worksuite.about;

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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * REST controller exposing endpoints for inspecting API route usage statistics and performing dead code analysis.
 *
 * <p>Example HTTP requests:
 * <pre>{@code
 * GET /api/v1/metrics/routes
 * GET /api/v1/metrics/routes/unused
 * GET /api/v1/metrics/routes/top
 * PUT /api/v1/metrics/routes/reset?httpMethod=GET&pattern=/api/v1/notes/{id}
 * }</pre>
 */
@Log4j2
@Tag(name = "Route Metrics", description = "Endpoints for analyzing REST API endpoint usage, access frequencies, and dead routes")
@RestApiController("/api/v1/metrics/routes")
@RequiredArgsConstructor
public class RouteUsageMetricController {

    private final RouteUsageMetricService metricService;

    /**
     * Retrieves all recorded API route usage metrics.
     *
     * @return list of {@link RouteUsageMetricDTO} records
     */
    @Operation(
        summary = "Get all route metrics",
        description = "Retrieves invocation metrics, request counts, and timestamps for all cataloged API endpoints."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Route metrics retrieved successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                array = @ArraySchema(schema = @Schema(implementation = RouteUsageMetricDTO.class))
            )
        )
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<RouteUsageMetricDTO> getAllMetrics() {
        log.info("Request received to fetch all route metrics");
        return metricService.getAllMetrics();
    }

    /**
     * Retrieves unused API routes for dead code analysis.
     *
     * @return list of {@link RouteUsageMetricDTO} records with an invocation count of zero
     */
    @Operation(
        summary = "Get unused routes",
        description = "Retrieves all registered API routes that have never received any requests (invocation count = 0)."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Unused route metrics retrieved successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                array = @ArraySchema(schema = @Schema(implementation = RouteUsageMetricDTO.class))
            )
        )
    })
    @GetMapping(value = "/unused", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<RouteUsageMetricDTO> getUnusedRoutes() {
        log.info("Request received to fetch unused route metrics (dead code analysis)");
        return metricService.getUnusedRoutes();
    }

    /**
     * Retrieves the most frequently invoked API routes ordered by usage frequency.
     *
     * @return list of {@link RouteUsageMetricDTO} records sorted by invocation count descending
     */
    @Operation(
        summary = "Get top accessed routes",
        description = "Retrieves API routes ordered by invocation count in descending order."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Top route metrics retrieved successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                array = @ArraySchema(schema = @Schema(implementation = RouteUsageMetricDTO.class))
            )
        )
    })
    @GetMapping(value = "/top", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<RouteUsageMetricDTO> getMostUsedRoutes() {
        log.info("Request received to fetch most frequently used route metrics");
        return metricService.getMostUsedRoutes();
    }

    /**
     * Resets the invocation metrics for a specific route pattern.
     *
     * @param httpMethod the HTTP request method (e.g., "GET", "POST")
     * @param pattern the route URL path pattern (e.g., "/api/v1/notes/{id}")
     * @return empty response with HTTP 204 No Content if reset, or HTTP 404 Not Found
     */
    @Operation(
        summary = "Reset route metric",
        description = "Resets the invocation count to 0 and clears the last-invoked timestamp for the given route."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Route metric reset successfully"),
        @ApiResponse(
            responseCode = "404",
            description = "Route metric for specified HTTP method and pattern not found",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
        )
    })
    @PutMapping("/reset")
    public ResponseEntity<Void> resetMetric(
        @Parameter(description = "HTTP request method", example = "GET")
        @RequestParam final String httpMethod,
        @Parameter(description = "URL path pattern", example = "/api/v1/notes/{id}")
        @RequestParam final String pattern
    ) {
        log.info("Request received to reset metric for [{} {}]", httpMethod, pattern);
        final boolean reset = metricService.resetMetric(httpMethod, pattern);
        return reset ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}