package com.hashmanager.hashmanager.api.event;

import com.hashmanager.hashmanager.api.entity.RabbitOutboxEntity;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class OutboxEvent {
    private Long outboxEntityId;
}