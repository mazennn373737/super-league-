package com.superleague.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
public class LiveScoreWebSocketHandler extends TextWebSocketHandler {

    private final Set<WebSocketSession> sessions = new CopyOnWriteArraySet<>();
    private final ObjectMapper objectMapper;

    public LiveScoreWebSocketHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
        System.out.println("WebSocket connected: " + session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
        System.out.println("WebSocket disconnected: " + session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        // Client messages can be handled here (e.g., subscription topics)
    }

    public void broadcast(Object payload) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            TextMessage message = new TextMessage(json);
            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    try {
                        session.sendMessage(message);
                    } catch (IOException e) {
                        System.err.println("Error sending to session " + session.getId() + ": " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Broadcast error: " + e.getMessage());
        }
    }

    public int getActiveConnectionCount() {
        return sessions.size();
    }
}
