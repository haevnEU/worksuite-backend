package de.haevn.worksuite.common.exceptions;

import java.time.Instant;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Log4j2
@RestControllerAdvice
public class GlobalExceptionHandler {

    private ResponseEntity<ErrorResponseDTO> buildErrorResponse(final HttpStatus status, final Throwable cause) {
        log.error("An error occurred while processing the request: {}", cause.getMessage(), cause);
        final ErrorResponseDTO body =
            new ErrorResponseDTO(status.value(), status.getReasonPhrase(), cause.getMessage(), Instant.now());
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ErrorResponseDTO> handleApplicationException(final ApplicationException ex) {
        return buildErrorResponse(ex.getStatus(), ex);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleException(final Exception ex) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex);
    }
}