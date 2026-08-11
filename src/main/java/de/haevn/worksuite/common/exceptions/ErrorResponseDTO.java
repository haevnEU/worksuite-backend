package de.haevn.worksuite.common.exceptions;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

/**
 * Standard API error response envelope.
 *
 * <p>Example usage:
 * <pre>{@code
 * ErrorResponseDTO error = new ErrorResponseDTO(
 *     HttpStatus.NOT_FOUND.value(),
 *     "Not Found",
 *     "Resource with ID 123 does not exist.",
 *     Instant.now(),
 *     "c0a80101-8b9a-4e2b-9e9b-9c7f6d4e5f6a"
 * );
 * }</pre>
 *
 * @param status the HTTP status code (e.g., 400, 404, 500)
 * @param error the HTTP status reason phrase
 * @param message detailed error description
 * @param timestamp the precise instant when the error occurred
 * @param correlationId unique tracing ID extracted from {@link org.slf4j.MDC}
 */
@Schema(description = "Standard API error response payload")
public record ErrorResponseDTO(

    @Schema(description = "HTTP status code", example = "400") int status,

    @Schema(description = "HTTP error phrase", example = "Bad Request") String error,

    @Schema(description = "Human-readable error description",
        example = "Invalid parameter: id must not be null") String message,

    @Schema(description = "Timestamp when the error occurred", example = "2026-08-17T16:15:30.000Z") Instant timestamp,

    @Schema(description = "Unique correlation ID for request tracing",
        example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890") String correlationId) {
    public ErrorResponseDTO(int status, String error, String message, Instant timestamp) {
        this(status, error, message, timestamp, null);
    }
}