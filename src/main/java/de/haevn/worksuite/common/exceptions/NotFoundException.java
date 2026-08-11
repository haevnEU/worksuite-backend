package de.haevn.worksuite.common.exceptions;

import org.springframework.http.HttpStatus;

public class NotFoundException extends ApplicationException {

    public NotFoundException() {
        super(HttpStatus.NOT_FOUND);
    }

    public NotFoundException(final String message) {
        super(message, HttpStatus.NOT_FOUND);
    }

    public NotFoundException(final Throwable cause) {
        super(HttpStatus.NOT_FOUND, cause);
    }

    public NotFoundException(final String message, final Throwable cause) {
        super(message, HttpStatus.NOT_FOUND, cause);
    }
}
