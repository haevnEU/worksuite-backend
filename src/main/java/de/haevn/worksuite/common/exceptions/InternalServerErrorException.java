package de.haevn.worksuite.common.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Generic runtime exception thrown when an unexpected internal server failure occurs during processing.
 *
 * <p>Maps to HTTP {@link HttpStatus#INTERNAL_SERVER_ERROR} (500).
 *
 * <p>Example usage:
 * <pre>{@code
 * try {
 *     return xmlMapper.writeValueAsString(schemaDto);
 * } catch (JsonProcessingException e) {
 *     throw new InternalServerErrorException("Failed to generate XML schema representation.", e);
 * }
 * }</pre>
 */
public class InternalServerErrorException extends ApplicationException {

    private static final String DEFAULT_MESSAGE = "An unexpected internal server error occurred.";

    /**
     * Constructs a new exception with a default internal error message.
     */
    public InternalServerErrorException() {
        super(DEFAULT_MESSAGE, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Constructs a new exception with a custom error message.
     *
     * @param message detailed description of the internal error
     */
    public InternalServerErrorException(final String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Constructs a new exception with a root cause.
     *
     * @param cause underlying cause of the server failure
     */
    public InternalServerErrorException(final Throwable cause) {
        super(DEFAULT_MESSAGE, HttpStatus.INTERNAL_SERVER_ERROR, cause);
    }

    /**
     * Constructs a new exception with a custom error message and root cause.
     *
     * @param message detailed description of the internal error
     * @param cause underlying cause of the server failure
     */
    public InternalServerErrorException(final String message, final Throwable cause) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR, cause);
    }
}