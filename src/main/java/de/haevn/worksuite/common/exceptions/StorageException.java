package de.haevn.worksuite.common.exceptions;

import org.springframework.http.HttpStatus;

public class StorageException extends ApplicationException {

    public StorageException() {
        super(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public StorageException(final String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public StorageException(final Throwable cause) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, cause);
    }

    public StorageException(final String message, final Throwable cause) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR, cause);
    }
}
