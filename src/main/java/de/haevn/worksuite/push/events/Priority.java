package de.haevn.worksuite.push.events;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Severity level classification for real-time WebSocket notification events.
 *
 * <p>Example usage:
 * <pre>{@code
 * Priority priority = Priority.INFO;
 * }</pre>
 */
@Schema(description = "Severity levels for real-time WebSocket notifications")
public enum Priority {

    @Schema(description = "Informational event notification") INFO,

    @Schema(description = "Successful operation event notification") SUCCESS,

    @Schema(description = "Warning event indicating potential issues") WARN,

    @Schema(description = "Error event indicating an operation failure") ERROR,

    @Schema(description = "Critical system-level error notification") CRITICAL
}