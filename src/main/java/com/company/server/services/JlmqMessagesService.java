package com.company.server.services;

import com.company.server.models.Message;
import com.company.server.repositories.MessagesRepository;
import com.company.server.repositories.QueuesRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Service
@AllArgsConstructor
public class JlmqMessagesService {
    private final ObjectMapper mapper;
    private final JlmqSubscriptionService subscriptionService;
    private final JlmqSessionService sessionService;

    @Autowired
    private final QueuesRepository queuesRepository;
    @Autowired
    private final MessagesRepository messagesRepository;


    @SneakyThrows
    private TextMessage toTextMessage(Message message) {
        JsonNodeFactory factory = mapper.getNodeFactory();
        ObjectNode root = factory.objectNode();

        root.put("command", "receive");
        root.put("message", message.getId());
        root.set("body", mapper.readTree(message.getMessage()));

        return new TextMessage(root.toString());
    }

    @SneakyThrows
    public void checkMessagesFor(WebSocketSession session) {
        if (!sessionService.hasQueue(session)) {
            throw new IllegalStateException("consumer not attached to any queue!");
        }

        String queue = sessionService.getQueue(session);
        List<Message> messages = messagesRepository.getFirstUnprocessedMessage(queue);

        for (Message message : messages) {
            session.sendMessage(toTextMessage(message));
        }
    }

    public void send(ObjectNode node, String queue) {
        if (!queuesRepository.findByName(queue).isPresent()) {
            throw new IllegalStateException("no such queue.");
        }

        Message message = messagesRepository.save(Message.builder()
                .message(node.toString())
                .queue(queue)
                .time(LocalDateTime.now())
                .status(Message.Status.RECEIVED)
                .build());

        subscriptionService.subscriber(queue).ifPresent(session -> {
            try {
                session.sendMessage(toTextMessage(message));
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        });
    }

    private void changeStatus(long id, Message.Status status) {
        Optional<Message> optionalMessage = messagesRepository.findById(id);
        if (optionalMessage.isPresent()) {
            Message message = optionalMessage.get();
            messagesRepository.changeStatus(message.getId(), status);
        } else {
            throw new IllegalArgumentException();
        }
    }

    public void acknowledge(WebSocketSession session, long id) {
        changeStatus(id, Message.Status.ACKNOWLEDGED);
    }

    public void malformed(WebSocketSession session, long id) {
        changeStatus(id, Message.Status.MALFORMED);
        checkMessagesFor(session);
    }

    public void completed(WebSocketSession session, long id) {
        changeStatus(id, Message.Status.COMPLETED);
        checkMessagesFor(session);
    }
}
