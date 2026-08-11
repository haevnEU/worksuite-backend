package de.haevn.worksuite.common.exceptions;

import org.springframework.http.HttpStatus;

public class BadRequestException extends ApplicationException {

    public BadRequestException() {
        super(HttpStatus.BAD_REQUEST);
    }

    public BadRequestException(final String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }

    public BadRequestException(final Throwable cause) {
        super(HttpStatus.BAD_REQUEST, cause);
    }

    public BadRequestException(final String message, final Throwable cause) {
        super(message, HttpStatus.BAD_REQUEST, cause);
    }
}
