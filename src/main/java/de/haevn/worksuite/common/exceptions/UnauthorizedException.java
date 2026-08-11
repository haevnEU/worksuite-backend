package de.haevn.worksuite.common.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when authentication is missing, invalid, or expired.
 *
 * <p>Maps to HTTP {@link HttpStatus#UNAUTHORIZED} (401).
 *
 * <p>Example usage:
 * <pre>{@code
 * if (jwtToken == null || isExpired(jwtToken)) {
 *     throw new UnauthorizedException("Session has expired. Please authenticate again.");
 * }
 * }</pre>
 */
public class UnauthorizedException extends ApplicationException {

    private static final String DEFAULT_MESSAGE = "Authentication is required to access this resource.";

    /**
     * Constructs a new exception with a default unauthorized message.
     */
    public UnauthorizedException() {
        super(DEFAULT_MESSAGE, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Constructs a new exception with a custom error message.
     *
     * @param message detailed explanation of the authentication failure
     */
    public UnauthorizedException(final String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Constructs a new exception with a root cause.
     *
     * @param cause underlying cause of the authentication failure
     */
    public UnauthorizedException(final Throwable cause) {
        super(DEFAULT_MESSAGE, HttpStatus.UNAUTHORIZED, cause);
    }

    /**
     * Constructs a new exception with a custom error message and root cause.
     *
     * @param message detailed explanation of the authentication failure
     * @param cause underlying cause of the authentication failure
     */
    public UnauthorizedException(final String message, final Throwable cause) {
        super(message, HttpStatus.UNAUTHORIZED, cause);
    }
}