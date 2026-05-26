package com.superleague.config;

import com.superleague.websocket.LiveScoreWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final LiveScoreWebSocketHandler liveScoreHandler;

    public WebSocketConfig(LiveScoreWebSocketHandler liveScoreHandler) {
        this.liveScoreHandler = liveScoreHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(liveScoreHandler, "/ws/live")
                .setAllowedOrigins("*");
    }
}
