package com.company.server.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class Message {
    private Long id;
    private String queue;
    private String message;
    private LocalDateTime time;
    private Status status;

    public enum Status {
        RECEIVED, ACKNOWLEDGED, MALFORMED, COMPLETED
    }
}
