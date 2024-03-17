package com.hashworker.api.entity;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.rabbit.connection.CorrelationData;

public class ConsumerAckCorrelationData extends CorrelationData {
    private final Channel channel;
    private final Long deliveryTag;
    public ConsumerAckCorrelationData(String id, Channel channel1, Long deliveryTag1)
    {
        super(id);
        channel = channel1;
        deliveryTag = deliveryTag1;
    }
}