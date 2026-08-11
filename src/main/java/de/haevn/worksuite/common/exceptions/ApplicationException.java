package de.haevn.worksuite.common.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApplicationException extends RuntimeException {
    protected final HttpStatus status;

    public ApplicationException(final HttpStatus status) {
        this.status = status;
    }

    public ApplicationException(final String message, final HttpStatus status) {
        super(message);
        this.status = status;
    }

    public ApplicationException(final HttpStatus status, final Throwable cause) {
        super(cause);
        this.status = status;
    }

    public ApplicationException(final String message, final HttpStatus status, final Throwable cause) {
        super(message, cause);
        this.status = status;
    }
}
