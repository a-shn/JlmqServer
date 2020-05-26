package com.company.server.services;

import com.company.server.models.Queue;
import com.company.server.repositories.QueuesRepository;
import lombok.AllArgsConstructor;
import lombok.Synchronized;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


@Service
@AllArgsConstructor
public class JlmqSubscriptionService {
    @Autowired
    private QueuesRepository queuesRepository;

    private final Map<String, WebSocketSession> subscriptions = new HashMap<>();

    public JlmqSubscriptionService() {
    }

    @Synchronized
    public void subscribe(WebSocketSession session, String queueName) {
        if (session.getAttributes().containsKey("queue")) {
            throw new IllegalStateException("consumer is already subscribed!");
        }

        Optional<Queue> optional = queuesRepository.findByName(queueName);
        if (!optional.isPresent()) {
            throw new IllegalStateException("no queue with this name!");
        }
        if (optional.get().getHasConsumer()) {
            throw new IllegalStateException("queue already has a consumer!");
        }

        session.getAttributes().put("queue", queueName);
        subscriptions.put(queueName, session);
    }

    public void unsubscribe(WebSocketSession session) {
        String queueName = (String) session.getAttributes().get("queue");
        subscriptions.put(queueName, null);
        session.getAttributes().remove("queue");
    }

    public Optional<WebSocketSession> subscriber(String queue) {
        return Optional.ofNullable(subscriptions.get(queue));
    }
}
