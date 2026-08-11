package de.haevn.worksuite.common.exceptions;

import java.time.Instant;

public record ErrorResponseDTO(int status, String error, String message, Instant timestamp) {
}
