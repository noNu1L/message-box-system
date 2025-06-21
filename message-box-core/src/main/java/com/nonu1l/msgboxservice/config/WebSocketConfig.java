package com.nonu1l.msgboxservice.config;

import com.nonu1l.msgboxservice.service.ClientWebSocketHandler;
import com.nonu1l.msgboxservice.service.SimpleTestWebSocketHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@Slf4j
public class WebSocketConfig implements WebSocketConfigurer {

    private final SimpleTestWebSocketHandler simpleTestWebSocketHandler;
    private final ClientWebSocketHandler clientWebSocketHandler;

    public WebSocketConfig(SimpleTestWebSocketHandler simpleTestWebSocketHandler, ClientWebSocketHandler clientWebSocketHandler) {
        this.simpleTestWebSocketHandler = simpleTestWebSocketHandler;
        this.clientWebSocketHandler = clientWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        log.info(">>> Registering WebSocket handler for /ws/test");
        registry.addHandler(simpleTestWebSocketHandler, "/ws/test")
                .setAllowedOrigins("*");

        log.info(">>> Registering WebSocket handler for /ws/client");
        registry.addHandler(clientWebSocketHandler, "/ws/client")
                .setAllowedOrigins("*");
    }
} 