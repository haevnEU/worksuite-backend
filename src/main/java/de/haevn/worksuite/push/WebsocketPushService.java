package de.haevn.worksuite.push;
import de.haevn.worksuite.push.events.WsEvent;
import tools.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class WebsocketPushService {

    private final PushWebSocketHandler webSocketHandler;
    private final ObjectMapper objectMapper;

    public WebsocketPushService(final PushWebSocketHandler webSocketHandler, final ObjectMapper objectMapper) {
        this.webSocketHandler = webSocketHandler;
        this.objectMapper = objectMapper;
    }

    public void dispatch(final WsEvent event) {
        if (webSocketHandler.getActiveSessionsCount() == 0) {
            log.trace("No active WebSocket sessions. Skipping broadcast for event from {}", event.source());
            return;
        }

        try {
            final String jsonPayload = objectMapper.writeValueAsString(event);
            webSocketHandler.broadcast(jsonPayload);
        } catch (Exception e) {
            log.error("Error serializing or broadcasting WsEvent from {}: {}", event.source(), e.getMessage(), e);
        }
    }
}