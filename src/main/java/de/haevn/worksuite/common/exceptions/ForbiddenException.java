package de.haevn.worksuite.common.exceptions;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends ApplicationException {

    public ForbiddenException() {
        super(HttpStatus.FORBIDDEN);
    }

    public ForbiddenException(final String message) {
        super(message, HttpStatus.FORBIDDEN);
    }

    public ForbiddenException(final Throwable cause) {
        super(HttpStatus.FORBIDDEN, cause);
    }

    public ForbiddenException(final String message, final Throwable cause) {
        super(message, HttpStatus.FORBIDDEN, cause);
    }
}
