package de.haevn.worksuite.push.events;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.Objects;

/**
 * Real-time event payload dispatched to active WebSocket clients.
 *
 * <p>Example usage:
 * <pre>{@code
 * WsEvent event = new WsEvent(
 *     NoteService.class,
 *     Priority.INFO,
 *     "Note created: Sprint 24 Planning"
 * );
 * }</pre>
 *
 * @param source the name of the emitting class or component
 * @param priority the severity level of the event
 * @param payload the message body or serialized JSON content
 * @param timestamp the exact instant when the event was recorded
 */
@Schema(description = "Payload envelope for WebSocket event broadcasting")
public record WsEvent(

    @Schema(description = "Originating component or service name", example = "NoteService") String source,

    @Schema(description = "Notification priority", example = "INFO") Priority priority,

    @Schema(description = "Event description message or JSON payload",
        example = "Note created: Sprint 24 Planning") String payload,

    @Schema(description = "Timestamp when the event occurred",
        example = "2026-08-17T16:40:00.000Z") Instant timestamp) {

    /**
     * Convenience constructor capturing the source simple name and setting the current {@link Instant#now()}.
     *
     * @param source source class emitting the event
     * @param priority notification severity
     * @param payload event text or payload content
     */
    public WsEvent(final Class<?> source, final Priority priority, final String payload) {
        this(Objects.requireNonNull(source, "Source class must not be null").getSimpleName(), priority, payload,
            Instant.now());
    }
}