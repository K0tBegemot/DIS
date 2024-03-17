package com.hashmanager.hashmanager.api.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hashmanager.hashmanager.api.entity.RabbitMessageHeaders;
import com.hashmanager.hashmanager.api.entity.RabbitMessageProperties;
import com.hashmanager.hashmanager.api.entity.RabbitOutboxEntity;
import com.hashmanager.hashmanager.api.event.OutboxEvent;
import lombok.NoArgsConstructor;
import org.mapstruct.*;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.support.converter.AbstractJavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR,
        componentModel = MappingConstants.ComponentModel.SPRING,
        injectionStrategy = InjectionStrategy.FIELD,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
@NoArgsConstructor
public abstract class OutboxMapper {
    @Autowired
    protected Jackson2JsonMessageConverter mapper;
    @Autowired
    protected ObjectMapper objectMapper;

    public OutboxMapper(Jackson2JsonMessageConverter mapper1, ObjectMapper objectMapper1) {
        mapper = mapper1;
        objectMapper = objectMapper1;
    }

    public RabbitOutboxEntity convertObjectToOutboxMessage(Object message, String exchangeName, String routingKey) {
        Message tempMessage = null;
        if (message != null) {
            tempMessage = mapper.toMessage(message, null);
        }
        if (tempMessage == null) {
            return null;
        }
        return RabbitOutboxEntity.builder()
                .exchangeName(exchangeName)
                .routingKey(routingKey)
                .message(tempMessage.getBody())
                .properties(
                        RabbitMessageProperties.builder()
                                .contentEncoding(tempMessage.getMessageProperties().getContentEncoding())
                                .contentLength(tempMessage.getMessageProperties().getContentLength())
                                .contentType(tempMessage.getMessageProperties().getContentType())
                                .headers(
                                        RabbitMessageHeaders.builder()
                                                .classIdField(tempMessage.getMessageProperties().getHeader(AbstractJavaTypeMapper.DEFAULT_CLASSID_FIELD_NAME))
                                                .build()
                                )
                                .build()
                )
                .build();
    }

    @Mapping(target = "outboxEntityId", source = "id")
    public abstract OutboxEvent convertOutboxEntityToEvent(RabbitOutboxEntity entity);

    public abstract List<OutboxEvent> convertListOutboxEntityToEvent(List<RabbitOutboxEntity> list);
}