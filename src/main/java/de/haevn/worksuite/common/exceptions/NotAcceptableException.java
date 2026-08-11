package de.haevn.worksuite.common.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when the server cannot produce a response matching the {@code Accept} headers requested by the client.
 *
 * <p>Maps to HTTP {@link HttpStatus#NOT_ACCEPTABLE} (406).
 *
 * <p>Example usage:
 * <pre>{@code
 * if (!supportedMediaTypes.contains(clientAcceptHeader)) {
 *     throw new NotAcceptableException("Unsupported Accept header: " + clientAcceptHeader);
 * }
 * }</pre>
 */
public class NotAcceptableException extends ApplicationException {

    private static final String DEFAULT_MESSAGE = "Cannot produce a response matching the requested media type.";

    /**
     * Constructs a new exception with a default not acceptable message.
     */
    public NotAcceptableException() {
        super(DEFAULT_MESSAGE, HttpStatus.NOT_ACCEPTABLE);
    }

    /**
     * Constructs a new exception with a custom error message.
     *
     * @param message detailed explanation of the unsupported format
     */
    public NotAcceptableException(final String message) {
        super(message, HttpStatus.NOT_ACCEPTABLE);
    }

    /**
     * Constructs a new exception with a root cause.
     *
     * @param cause underlying cause of the failure
     */
    public NotAcceptableException(final Throwable cause) {
        super(DEFAULT_MESSAGE, HttpStatus.NOT_ACCEPTABLE, cause);
    }

    /**
     * Constructs a new exception with a custom error message and root cause.
     *
     * @param message detailed explanation of the unsupported format
     * @param cause underlying cause of the failure
     */
    public NotAcceptableException(final String message, final Throwable cause) {
        super(message, HttpStatus.NOT_ACCEPTABLE, cause);
    }
}