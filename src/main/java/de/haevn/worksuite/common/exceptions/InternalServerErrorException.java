package de.haevn.worksuite.common.exceptions;

import org.springframework.http.HttpStatus;

public class InternalServerErrorException extends ApplicationException {

    public InternalServerErrorException() {
        super(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public InternalServerErrorException(final String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public InternalServerErrorException(final Throwable cause) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, cause);
    }

    public InternalServerErrorException(final String message, final Throwable cause) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR, cause);
    }
}
