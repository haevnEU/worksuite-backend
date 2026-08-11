package de.haevn.worksuite.common.exceptions;

import java.time.Instant;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.log4j.Log4j2;
import org.slf4j.MDC;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Centralized exception translator across all REST controllers.
 *
 * <p>Translates domain exceptions, validation violations, and unexpected runtime errors into
 * standardized {@link ErrorResponseDTO} responses.
 *
 * <p>Example handling flow:
 * <pre>{@code
 * // Throwing inside service or controller:
 * throw new ApplicationException(HttpStatus.NOT_FOUND, "Entity not found");
 *
 * // Automatically intercepted and mapped to:
 * // HTTP 404 -> ErrorResponseDTO
 * }</pre>
 */
@Log4j2
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String MDC_CORRELATION_ID = "correlationId";

    /**
     * Handles custom business-level {@link ApplicationException} errors.
     *
     * @param ex the intercepted application exception
     * @return a formatted {@link ResponseEntity} containing {@link ErrorResponseDTO}
     */
    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ErrorResponseDTO> handleApplicationException(final ApplicationException ex) {
        log.warn("Application exception [{}] occurred: {}", ex.getStatus(), ex.getMessage());
        return buildResponse(ex.getStatus(), ex.getMessage());
    }

    /**
     * Handles Jakarta/Spring parameter validation errors triggered by {@code @Valid}.
     *
     * @param ex the method argument validation exception
     * @return a 400 Bad Request {@link ResponseEntity} with combined validation messages
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationException(final MethodArgumentNotValidException ex) {
        final String errorDetails =
            ex.getBindingResult().getFieldErrors().stream().map(DefaultMessageSourceResolvable::getDefaultMessage)
                .filter(Objects::nonNull).collect(Collectors.joining("; "));

        final String finalMessage = errorDetails.isBlank() ? "Validation failed for request payload." : errorDetails;
        log.warn("Payload validation failed: {}", finalMessage);
        return buildResponse(HttpStatus.BAD_REQUEST, finalMessage);
    }

    /**
     * Fallback handler for all unhandled {@link Exception} instances to prevent leaking stack traces.
     *
     * @param ex the caught unhandled exception
     * @return a 500 Internal Server Error {@link ResponseEntity}
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleUnexpectedException(final Exception ex) {
        log.error("Unhandled internal server exception", ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected internal server error occurred.");
    }

    /**
     * Constructs a standardized {@link ResponseEntity} containing an {@link ErrorResponseDTO}.
     *
     * <p>Extracts the active correlation ID from {@link org.slf4j.MDC} and attaches the current timestamp.
     *
     * <p>Example usage:
     * <pre>{@code
     * ResponseEntity<ErrorResponseDTO> response = buildResponse(
     *     HttpStatus.BAD_REQUEST,
     *     "Invalid parameter: id cannot be negative"
     * );
     * }</pre>
     *
     * @param status the HTTP status code representing the error
     * @param message detailed description of the error
     * @return a {@link ResponseEntity} wrapping the populated {@link ErrorResponseDTO}
     */
    private ResponseEntity<ErrorResponseDTO> buildResponse(final HttpStatus status, final String message) {
        final String correlationId = MDC.get(MDC_CORRELATION_ID);
        final ErrorResponseDTO body =
            new ErrorResponseDTO(status.value(), status.getReasonPhrase(), message, Instant.now(), correlationId);
        return ResponseEntity.status(status).body(body);
    }
}