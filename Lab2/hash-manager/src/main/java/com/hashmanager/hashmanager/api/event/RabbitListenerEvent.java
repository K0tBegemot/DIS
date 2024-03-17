package com.hashmanager.hashmanager.api.event;

import com.rabbitmq.client.Channel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class RabbitListenerEvent {
    private Channel channel;
    private Long tag;
}
