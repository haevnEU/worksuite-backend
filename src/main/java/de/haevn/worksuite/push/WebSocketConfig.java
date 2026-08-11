package de.haevn.worksuite.push;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final PushWebSocketHandler pushWebSocketHandler;

    public WebSocketConfig(final PushWebSocketHandler pushWebSocketHandler) {
        this.pushWebSocketHandler = pushWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(final WebSocketHandlerRegistry registry) {
        registry.addHandler(pushWebSocketHandler, "/api/ws").setAllowedOriginPatterns("*");
    }
}