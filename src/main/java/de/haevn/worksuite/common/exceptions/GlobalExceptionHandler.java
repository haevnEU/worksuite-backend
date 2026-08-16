package de.haevn.worksuite.common.exceptions;

import java.time.Instant;
import java.util.stream.Collectors;
import lombok.extern.log4j.Log4j2;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Log4j2
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ErrorResponseDTO> handleApplicationException(final ApplicationException ex) {
        log.warn("Application exception [{}]: {}", ex.getStatus(), ex.getMessage());
        return buildResponse(ex.getStatus(), ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationException(final MethodArgumentNotValidException ex) {
        final String errorDetails = ex.getBindingResult().getFieldErrors().stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.joining(", "));

        log.warn("Validation failed: {}", errorDetails);
        return buildResponse(HttpStatus.BAD_REQUEST, errorDetails.isBlank() ? "Validation failed" : errorDetails);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleUnexpectedException(final Exception ex) {
        log.error("Unhandled server exception: {}", ex.getMessage(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected internal server error occurred.");
    }

    private ResponseEntity<ErrorResponseDTO> buildResponse(final HttpStatus status, final String message) {
        final String correlationId = MDC.get("correlationId");
        final ErrorResponseDTO body = new ErrorResponseDTO(
            status.value(),
            status.getReasonPhrase(),
            message,
            Instant.now(),
            correlationId
        );
        return ResponseEntity.status(status).body(body);
    }
}