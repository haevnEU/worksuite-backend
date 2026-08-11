package de.haevn.worksuite.push;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Log4j2
@Component
public class PushWebSocketHandler extends TextWebSocketHandler {

    // Thread-sicheres Set für aktive Sessions
    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();

    @Override
    public void afterConnectionEstablished(final WebSocketSession session) {
        sessions.add(session);
        log.debug("WebSocket Session opened: {}", session.getId());
    }

    @Override
    public void afterConnectionClosed(final WebSocketSession session, final CloseStatus status) {
        sessions.remove(session);
        log.debug("WebSocket Session closed: {} (Status: {})", session.getId(), status);
    }

    @Override
    public void handleTransportError(final WebSocketSession session, final Throwable exception) {
        log.warn("Transport error on session {}: {}", session.getId(), exception.getMessage());
        sessions.remove(session);
        try {
            session.close(CloseStatus.SERVER_ERROR);
        } catch (IOException ignored) {
        }
    }

    public void broadcast(final String payloadJson) {
        final TextMessage message = new TextMessage(payloadJson);

        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                // Synchronisation pro Session verhindert "IllegalStateException: The remote endpoint was in state..."
                synchronized (session) {
                    try {
                        session.sendMessage(message);
                    } catch (IOException e) {
                        log.error("Failed to send message to session {}: {}", session.getId(), e.getMessage());
                        sessions.remove(session);
                    }
                }
            } else {
                sessions.remove(session);
            }
        }
    }

    public int getActiveSessionsCount() {
        return sessions.size();
    }
}