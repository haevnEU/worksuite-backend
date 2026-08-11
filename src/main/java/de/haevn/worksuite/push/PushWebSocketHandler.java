package de.haevn.worksuite.push;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.log4j.Log4j2;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * WebSocket text handler managing client connections and broadcasting real-time notification messages.
 *
 * <p>Maintains an active set of connected {@link WebSocketSession} instances and synchronizes writes per session
 * to prevent concurrent write collisions.
 *
 * <p>Example usage:
 * <pre>{@code
 * @Autowired
 * private PushWebSocketHandler pushHandler;
 *
 * pushHandler.broadcast("{\"source\":\"NoteService\",\"payload\":\"New note added\"}");
 * }</pre>
 */
@Log4j2
@Component
public class PushWebSocketHandler extends TextWebSocketHandler {

    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();

    /**
     * Registers newly established WebSocket connections.
     *
     * @param session the newly opened {@link WebSocketSession}
     */
    @Override
    public void afterConnectionEstablished(@NonNull final WebSocketSession session) {
        sessions.add(session);
        log.debug("WebSocket session opened: '{}'", session.getId());
    }

    /**
     * Handles transport-level exceptions on active WebSocket channels.
     *
     * @param session the affected {@link WebSocketSession}
     * @param exception the transport exception encountered
     */
    @Override
    public void handleTransportError(@NonNull final WebSocketSession session, @NonNull final Throwable exception) {
        log.warn("Transport error on WebSocket session '{}': {}", session.getId(), exception.getMessage());
        sessions.remove(session);
        closeQuietly(session, CloseStatus.SERVER_ERROR);
    }

    /**
     * Unregisters closed WebSocket sessions.
     *
     * @param session the closing {@link WebSocketSession}
     * @param status the closure status code
     */
    @Override
    public void afterConnectionClosed(@NonNull final WebSocketSession session, @NonNull final CloseStatus status) {
        sessions.remove(session);
        log.debug("WebSocket session closed: '{}' (Status: '{}')", session.getId(), status);
    }

    /**
     * Broadcasts a JSON message payload across all currently connected client sessions.
     *
     * @param payloadJson the serialized JSON string to transmit
     */
    public void broadcast(final String payloadJson) {
        final TextMessage message = new TextMessage(payloadJson);

        for (final WebSocketSession session : sessions) {
            if (session.isOpen()) {
                sendMessageSynchronized(session, message);
            } else {
                sessions.remove(session);
            }
        }
    }

    /**
     * Returns the total count of currently connected WebSocket sessions.
     *
     * @return active session count
     */
    public int getActiveSessionsCount() {
        return sessions.size();
    }

    /**
     * Synchronously sends a text message to a specific session, safely removing it upon failure.
     *
     * <p>Example usage:
     * <pre>{@code
     * sendMessageSynchronized(session, textMessage);
     * }</pre>
     *
     * @param session the target {@link WebSocketSession}
     * @param message the {@link TextMessage} to transmit
     */
    private void sendMessageSynchronized(final WebSocketSession session, final TextMessage message) {
        synchronized (session) {
            try {
                session.sendMessage(message);
            } catch (IOException ex) {
                log.error("Failed to send WebSocket message to session '{}'", session.getId(), ex);
                sessions.remove(session);
            }
        }
    }

    /**
     * Safely closes a {@link WebSocketSession} without propagating checked exceptions.
     *
     * <p>Example usage:
     * <pre>{@code
     * closeQuietly(session, CloseStatus.SERVER_ERROR);
     * }</pre>
     *
     * @param session the {@link WebSocketSession} to terminate
     * @param status the {@link CloseStatus} indicator
     */
    private void closeQuietly(final WebSocketSession session, final CloseStatus status) {
        try {
            session.close(status);
        } catch (IOException ex) {
            log.trace("Error closing WebSocket session '{}' quietly", session.getId(), ex);
        }
    }
}