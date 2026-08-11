package de.haevn.worksuite.common.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Base abstract runtime exception for application-specific domain and business errors.
 *
 * <p>Carries an associated {@link HttpStatus} code that is mapped by
 * {@link GlobalExceptionHandler} to generate standard HTTP error response envelopes.
 *
 * <p>Example usage:
 * <pre>{@code
 * public class CustomBusinessException extends ApplicationException {
 *     public CustomBusinessException(String message) {
 *         super(message, HttpStatus.UNPROCESSABLE_ENTITY);
 *     }
 * }
 * }</pre>
 */
@Getter
public class ApplicationException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "An application error occurred";

    /**
     * The HTTP status code associated with this exception.
     */
    protected final HttpStatus status;

    /**
     * Constructs a new exception with the specified HTTP status and a default error message.
     *
     * @param status the HTTP status code representing the error
     */
    public ApplicationException(final HttpStatus status) {
        super(DEFAULT_MESSAGE);
        this.status = status;
    }

    /**
     * Constructs a new exception with the specified message and HTTP status.
     *
     * @param message detailed error description
     * @param status the HTTP status code representing the error
     */
    public ApplicationException(final String message, final HttpStatus status) {
        super(message);
        this.status = status;
    }

    /**
     * Constructs a new exception with the specified HTTP status and underlying cause.
     *
     * @param status the HTTP status code representing the error
     * @param cause the root cause of the error
     */
    public ApplicationException(final HttpStatus status, final Throwable cause) {
        super(DEFAULT_MESSAGE, cause);
        this.status = status;
    }

    /**
     * Constructs a new exception with the specified message, HTTP status, and underlying cause.
     *
     * @param message detailed error description
     * @param status the HTTP status code representing the error
     * @param cause the root cause of the error
     */
    public ApplicationException(final String message, final HttpStatus status, final Throwable cause) {
        super(message, cause);
        this.status = status;
    }
}