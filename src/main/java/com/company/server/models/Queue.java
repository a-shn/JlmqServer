package com.company.server.models;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Queue {
    private String queue;
    private Boolean hasConsumer;
}
