package de.haevn.worksuite.about;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Data transfer object representing route usage analysis data.
 *
 * <p>Example usage:
 * <pre>{@code
 * RouteUsageMetricDTO dto = RouteUsageMetricDTO.fromModel(routeMetricEntity);
 * }</pre>
 *
 * @param id unique metric record identifier
 * @param controllerClass controller class name
 * @param controllerMethod handler method name
 * @param httpMethod HTTP method (GET, POST, etc.)
 * @param pattern URL path pattern
 * @param invocationCount total request count
 * @param firstSeenAt cataloged timestamp
 * @param lastInvokedAt last execution timestamp
 */
@Schema(description = "Data transfer object representing API route usage statistics")
public record RouteUsageMetricDTO(

    @Schema(description = "Unique metric identifier", example = "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d")
    UUID id,

    @Schema(description = "Controller class name", example = "NoteController")
    String controllerClass,

    @Schema(description = "Controller method name", example = "getNoteById")
    String controllerMethod,

    @Schema(description = "HTTP method", example = "GET")
    String httpMethod,

    @Schema(description = "Route path pattern", example = "/api/v1/notes/{id}")
    String pattern,

    @Schema(description = "Total number of invocations", example = "42")
    long invocationCount,

    @Schema(description = "Registration timestamp", example = "2026-08-17T18:00:00.000Z")
    Instant firstSeenAt,

    @Schema(description = "Last access timestamp", example = "2026-08-17T20:45:00.000Z")
    Instant lastInvokedAt
) {

    /**
     * Maps a {@link RouteUsageMetric} entity to its corresponding {@link RouteUsageMetricDTO}.
     *
     * @param metric the persistent {@link RouteUsageMetric} entity
     * @return the populated {@link RouteUsageMetricDTO}
     */
    public static RouteUsageMetricDTO fromModel(final RouteUsageMetric metric) {
        Objects.requireNonNull(metric, "RouteUsageMetric must not be null");
        return new RouteUsageMetricDTO(
            metric.getId(),
            metric.getControllerClass(),
            metric.getControllerMethod(),
            metric.getHttpMethod(),
            metric.getPattern(),
            metric.getInvocationCount(),
            metric.getFirstSeenAt(),
            metric.getLastInvokedAt()
        );
    }
}