package com.company.server.services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

@Service
@AllArgsConstructor
public class JlmqSessionService {
    public Boolean hasQueue(WebSocketSession session) {
        return session.getAttributes().containsKey("queue");
    }

    public String getQueue(WebSocketSession session) {
        if (hasQueue(session)) {
            return (String) session.getAttributes().get("queue");
        } else {
            throw new IllegalStateException("session has no queue.");
        }
    }
}
