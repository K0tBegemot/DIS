package com.hashmanager.hashmanager.api.entity;

import com.hashmanager.hashmanager.api.dto.SimpleId;
import com.rabbitmq.client.MessageProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "outbox_rabbit_table")
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Getter
@Setter
@ToString
public class RabbitOutboxEntity extends BaseEntity{
    @Column(name = "routing_key", nullable = false)
    private String routingKey;
    @Column(name = "message", nullable = false)
    @ToString.Exclude
    private byte[] message;
    @Embedded
    private RabbitMessageProperties properties;
    @Column(name = "exchange_name", nullable = false)
    private String exchangeName;
}