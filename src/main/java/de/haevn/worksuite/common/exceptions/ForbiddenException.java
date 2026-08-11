package de.haevn.worksuite.common.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when an authenticated client lacks sufficient authorization or privileges.
 *
 * <p>Maps to HTTP {@link HttpStatus#FORBIDDEN} (403).
 *
 * <p>Example usage:
 * <pre>{@code
 * if (!currentUser.hasRole("ADMIN")) {
 *     throw new ForbiddenException("Administrator role is required to modify system settings.");
 * }
 * }</pre>
 */
public class ForbiddenException extends ApplicationException {

    private static final String DEFAULT_MESSAGE = "Access to the requested resource is denied.";

    /**
     * Constructs a new exception with a default forbidden message.
     */
    public ForbiddenException() {
        super(DEFAULT_MESSAGE, HttpStatus.FORBIDDEN);
    }

    /**
     * Constructs a new exception with a custom error message.
     *
     * @param message detailed explanation of why access was denied
     */
    public ForbiddenException(final String message) {
        super(message, HttpStatus.FORBIDDEN);
    }

    /**
     * Constructs a new exception with a root cause.
     *
     * @param cause underlying cause of the authorization failure
     */
    public ForbiddenException(final Throwable cause) {
        super(DEFAULT_MESSAGE, HttpStatus.FORBIDDEN, cause);
    }

    /**
     * Constructs a new exception with a custom error message and root cause.
     *
     * @param message detailed explanation of why access was denied
     * @param cause underlying cause of the authorization failure
     */
    public ForbiddenException(final String message, final Throwable cause) {
        super(message, HttpStatus.FORBIDDEN, cause);
    }
}