package de.haevn.worksuite.common.exceptions;

import java.time.Instant;

public record ErrorResponseDTO(
    int status,
    String error,
    String message,
    Instant timestamp,
    String correlationId
) {
    public ErrorResponseDTO(int status, String error, String message, Instant timestamp) {
        this(status, error, message, timestamp, null);
    }
}