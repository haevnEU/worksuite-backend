package de.haevn.worksuite.common.exceptions;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends ApplicationException {

    public UnauthorizedException() {
        super(HttpStatus.UNAUTHORIZED);
    }

    public UnauthorizedException(final String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }

    public UnauthorizedException(final Throwable cause) {
        super(HttpStatus.UNAUTHORIZED, cause);
    }

    public UnauthorizedException(final String message, final Throwable cause) {
        super(message, HttpStatus.UNAUTHORIZED, cause);
    }
}
