package de.haevn.worksuite.push;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * Configuration registering WebSocket endpoints and assigning handler mappings.
 *
 * <p>Exposes the {@link PushWebSocketHandler} on the {@code /api/ws} endpoint with configurable CORS origin patterns.
 *
 * <p>Example usage:
 * <pre>{@code
 * // Clients connect via ws://localhost:8080/api/ws
 * }</pre>
 */
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private static final String WS_ENDPOINT_PATH = "/api/ws";
    private static final String[] ALLOWED_ORIGIN_PATTERNS = {"*"};

    private final PushWebSocketHandler pushWebSocketHandler;

    /**
     * Registers the {@link PushWebSocketHandler} to handle incoming WebSocket connections.
     *
     * @param registry the {@link WebSocketHandlerRegistry} to configure
     */
    @Override
    public void registerWebSocketHandlers(@NonNull final WebSocketHandlerRegistry registry) {
        registry.addHandler(pushWebSocketHandler, WS_ENDPOINT_PATH).setAllowedOriginPatterns(ALLOWED_ORIGIN_PATTERNS);
    }
}