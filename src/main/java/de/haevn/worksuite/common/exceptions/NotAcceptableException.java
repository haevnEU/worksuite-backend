package de.haevn.worksuite.common.exceptions;

import org.springframework.http.HttpStatus;

public class NotAcceptableException extends ApplicationException {

    public NotAcceptableException() {
        super(HttpStatus.NOT_ACCEPTABLE);
    }

    public NotAcceptableException(final String message) {
        super(message, HttpStatus.NOT_ACCEPTABLE);
    }

    public NotAcceptableException(final Throwable cause) {
        super(HttpStatus.NOT_ACCEPTABLE, cause);
    }

    public NotAcceptableException(final String message, final Throwable cause) {
        super(message, HttpStatus.NOT_ACCEPTABLE, cause);
    }
}
