package de.haevn.worksuite.common.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a client request is invalid, malformed, or fails semantic validation.
 *
 * <p>Maps to HTTP {@link HttpStatus#BAD_REQUEST} (400).
 *
 * <p>Example usage:
 * <pre>{@code
 * if (request.endDate().isBefore(request.startDate())) {
 *     throw new BadRequestException("End date cannot precede start date.");
 * }
 * }</pre>
 */
public class BadRequestException extends ApplicationException {

    private static final String DEFAULT_MESSAGE = "Invalid or malformed request payload.";

    /**
     * Constructs a new exception with a default bad request message.
     */
    public BadRequestException() {
        super(DEFAULT_MESSAGE, HttpStatus.BAD_REQUEST);
    }

    /**
     * Constructs a new exception with a custom error message.
     *
     * @param message detailed explanation of the validation failure
     */
    public BadRequestException(final String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }

    /**
     * Constructs a new exception with a root cause.
     *
     * @param cause underlying cause of the failure
     */
    public BadRequestException(final Throwable cause) {
        super(DEFAULT_MESSAGE, HttpStatus.BAD_REQUEST, cause);
    }

    /**
     * Constructs a new exception with a custom error message and root cause.
     *
     * @param message detailed explanation of the validation failure
     * @param cause underlying cause of the failure
     */
    public BadRequestException(final String message, final Throwable cause) {
        super(message, HttpStatus.BAD_REQUEST, cause);
    }
}