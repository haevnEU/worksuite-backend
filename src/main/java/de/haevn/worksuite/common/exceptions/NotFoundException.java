package de.haevn.worksuite.common.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a requested resource, entity, or document cannot be located.
 *
 * <p>Maps to HTTP {@link HttpStatus#NOT_FOUND} (404).
 *
 * <p>Example usage:
 * <pre>{@code
 * Note note = noteRepository.findById(noteId)
 *     .orElseThrow(() -> new NotFoundException("Note with ID " + noteId + " does not exist."));
 * }</pre>
 */
public class NotFoundException extends ApplicationException {

    private static final String DEFAULT_MESSAGE = "The requested resource was not found.";

    /**
     * Constructs a new exception with a default not found message.
     */
    public NotFoundException() {
        super(DEFAULT_MESSAGE, HttpStatus.NOT_FOUND);
    }

    /**
     * Constructs a new exception with a custom error message.
     *
     * @param message detailed explanation identifying the missing resource
     */
    public NotFoundException(final String message) {
        super(message, HttpStatus.NOT_FOUND);
    }

    /**
     * Constructs a new exception with a root cause.
     *
     * @param cause underlying cause of the lookup failure
     */
    public NotFoundException(final Throwable cause) {
        super(DEFAULT_MESSAGE, HttpStatus.NOT_FOUND, cause);
    }

    /**
     * Constructs a new exception with a custom error message and root cause.
     *
     * @param message detailed explanation identifying the missing resource
     * @param cause underlying cause of the lookup failure
     */
    public NotFoundException(final String message, final Throwable cause) {
        super(message, HttpStatus.NOT_FOUND, cause);
    }
}