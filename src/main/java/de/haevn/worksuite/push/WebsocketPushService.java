package de.haevn.worksuite.push;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.haevn.worksuite.push.events.WsEvent;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

/**
 * Service orchestrating JSON serialization and broadcasting of {@link WsEvent} instances across active WebSocket sessions.
 *
 * <p>Example usage:
 * <pre>{@code
 * @Autowired
 * private WebsocketPushService pushService;
 *
 * pushService.dispatch(new WsEvent(RetroService.class, Priority.INFO, "Retro created"));
 * }</pre>
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class WebsocketPushService {

    private final PushWebSocketHandler webSocketHandler;
    private final ObjectMapper objectMapper;

    /**
     * Serializes a {@link WsEvent} to JSON and broadcasts it to all connected WebSocket clients.
     *
     * @param event the {@link WsEvent} to serialize and transmit
     */
    public void dispatch(final WsEvent event) {
        Objects.requireNonNull(event, "WsEvent must not be null");

        if (webSocketHandler.getActiveSessionsCount() == 0) {
            log.trace("No active WebSocket sessions found. Skipping broadcast for event from source '{}'",
                event.source());
            return;
        }

        try {
            final String jsonPayload = serializeEvent(event);
            webSocketHandler.broadcast(jsonPayload);
        } catch (Exception ex) {
            log.error("Failed to serialize or broadcast WsEvent from source '{}': {}", event.source(), ex.getMessage(),
                ex);
        }
    }

    /**
     * Serializes a given {@link WsEvent} object into a JSON string using the injected {@link ObjectMapper}.
     *
     * <p>Example usage:
     * <pre>{@code
     * String json = serializeEvent(event);
     * }</pre>
     *
     * @param event the event payload to serialize
     * @return serialized JSON string
     * @throws Exception if object serialization fails
     */
    private String serializeEvent(final WsEvent event) throws Exception {
        return objectMapper.writeValueAsString(event);
    }
}