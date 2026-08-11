package de.haevn.worksuite.common.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when an I/O, writing, reading, or checksum error occurs in the file storage subsystem.
 *
 * <p>Maps to HTTP {@link HttpStatus#INTERNAL_SERVER_ERROR} (500).
 *
 * <p>Example usage:
 * <pre>{@code
 * try {
 *     fileStorageService.storeFile(fileId, multipartFile);
 * } catch (IOException e) {
 *     throw new StorageException("Failed to persist file to disk: " + multipartFile.getOriginalFilename(), e);
 * }
 * }</pre>
 */
public class StorageException extends ApplicationException {

    private static final String DEFAULT_MESSAGE = "A file storage or filesystem I/O error occurred.";

    /**
     * Constructs a new exception with a default storage error message.
     */
    public StorageException() {
        super(DEFAULT_MESSAGE, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Constructs a new exception with a custom error message.
     *
     * @param message detailed description of the storage failure
     */
    public StorageException(final String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Constructs a new exception with a root cause.
     *
     * @param cause underlying cause of the I/O or filesystem error
     */
    public StorageException(final Throwable cause) {
        super(DEFAULT_MESSAGE, HttpStatus.INTERNAL_SERVER_ERROR, cause);
    }

    /**
     * Constructs a new exception with a custom error message and root cause.
     *
     * @param message detailed description of the storage failure
     * @param cause underlying cause of the I/O or filesystem error
     */
    public StorageException(final String message, final Throwable cause) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR, cause);
    }
}