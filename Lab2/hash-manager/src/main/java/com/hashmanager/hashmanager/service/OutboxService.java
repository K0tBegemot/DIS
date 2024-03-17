package com.hashmanager.hashmanager.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hashmanager.hashmanager.api.entity.RabbitMessageProperties;
import com.hashmanager.hashmanager.api.entity.RabbitOutboxEntity;
import com.hashmanager.hashmanager.api.event.OutboxEvent;
import com.hashmanager.hashmanager.api.mapper.OutboxMapper;
import com.hashmanager.hashmanager.exception.OutboxException;
import com.hashmanager.hashmanager.exception.persistence.EntityNotFoundException;
import com.hashmanager.hashmanager.repository.RabbitOutboxRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.AbstractJavaTypeMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@Service
public class OutboxService implements ApplicationEventPublisherAware {
    @Component
    public static class RabbitConfirmCallback implements RabbitTemplate.ConfirmCallback{
        private static final Logger logger = LoggerFactory.getLogger(RabbitConfirmCallback.class);
        private RabbitOutboxRepository repository;
        public RabbitConfirmCallback(RabbitOutboxRepository repository1){
            repository = repository1;
        }
        @Override
        @Transactional
        public void confirm(CorrelationData correlationData, boolean ack, String cause) {
            if(correlationData == null)
            {
                logger.error("Отсутствуют корреляционные данные.");
                throw new OutboxException();
            }
            Optional<RabbitOutboxEntity> entityOptional = repository.findById(Long.parseLong(correlationData.getId()));
            if(entityOptional.isEmpty())
            {
                logger.error("Корелляционные данные c идентификатором {} . В базе данных отсутствует Outbox ивент с таким идентификатором.", correlationData.getId());
                throw new EntityNotFoundException();
            }
            if(ack)
            {
                logger.debug("Удаление Outbox ивента с идентификатором {}", correlationData.getId());
                RabbitOutboxEntity entity = entityOptional.get();
                repository.deleteById(entity.getId());
                return;
            }
            logger.warn("Сообщение с id : {} не было принято брокером. Оно будет переотправлено позже. Причина : {}.", correlationData.getId(), cause);
        }
    }
    private static final Logger logger = LoggerFactory.getLogger(OutboxService.class);
    private OutboxMapper outboxMapper;
    private RabbitOutboxRepository rabbitOutboxRepository;
    private ApplicationEventPublisher applicationEventPublisher;
    private RabbitTemplate template;
    private ObjectMapper objectMapper;
    public OutboxService(OutboxMapper outboxMapper1, RabbitOutboxRepository rabbitOutboxRepository1, @Qualifier("myRabbitTemplate") RabbitTemplate template1, ObjectMapper objectMapper1)
    {
        outboxMapper = outboxMapper1;
        rabbitOutboxRepository = rabbitOutboxRepository1;
        template = template1;
        objectMapper = objectMapper1;
    }
    @Transactional(propagation = Propagation.MANDATORY)
    public void addToOutbox(Iterable<? extends Object> iterable, String exchangeName, String routingKey)
    {
        List<RabbitOutboxEntity> outboxList = new LinkedList<>();
        for(Object dto : iterable)
        {
            outboxList.add(outboxMapper.convertObjectToOutboxMessage(dto, exchangeName, routingKey));
        }
        logger.debug("Сохранение списка Outbox ивентов в базу данных. Список : {}", outboxList);
        outboxList = rabbitOutboxRepository.saveAll(outboxList);
        List<OutboxEvent> eventList = outboxMapper.convertListOutboxEntityToEvent(outboxList);
        for(OutboxEvent event : eventList)
        {
            applicationEventPublisher.publishEvent(event);
        }
    }
    @Transactional(propagation = Propagation.MANDATORY)
    public void addToOutbox(Object notify, String exchangeName, String routingKey)
    {
        RabbitOutboxEntity entity = outboxMapper.convertObjectToOutboxMessage(notify, exchangeName, routingKey);
        logger.debug("Сохранение Outbox ивента в базу данных. Ивент : {}", entity);
        entity = rabbitOutboxRepository.save(entity);
        OutboxEvent event = outboxMapper.convertOutboxEntityToEvent(entity);
        applicationEventPublisher.publishEvent(event);
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher1) {
        applicationEventPublisher = applicationEventPublisher1;
    }
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendOutboxEvent(OutboxEvent outboxEvent)
    {
        logger.debug("Получен транзакционный ивент. Получение Outbox ивента из базы данных. Ивент : {} .", outboxEvent);
        handleOutboxEvent(outboxEvent);
    }

    private void handleOutboxEvent(OutboxEvent outboxEvent){
        Optional<RabbitOutboxEntity> outboxEntityOptional = rabbitOutboxRepository.findById(outboxEvent.getOutboxEntityId());
        if(outboxEntityOptional.isPresent())
        {
            RabbitOutboxEntity entity = outboxEntityOptional.get();
            handleOutboxEntity(entity);
            return;
        }
        logger.error("Outbox entity с идентификатором {} не найдена. Такого не должно быть при нормальной работе программы", outboxEvent.getOutboxEntityId());
    }
    private void handleOutboxEntity(RabbitOutboxEntity entity){
        try {
            logger.debug("Отправка Outbox ивента в RabbitMQ. Entity : {}", entity);
            RabbitMessageProperties entityProperties = entity.getProperties();
            if(entityProperties.getContentType().equals(""))
            {
                logger.debug("Формат данных не поддерживается. Сообщение не будет отправлено.");
                rabbitOutboxRepository.deleteById(entity.getId());
                return;
            }
            MessageProperties properties = new MessageProperties();
            properties.setContentType(entityProperties.getContentType());
            properties.setContentEncoding(entityProperties.getContentEncoding());
            properties.setContentLength(entityProperties.getContentLength());
            properties.setHeader(AbstractJavaTypeMapper.DEFAULT_CLASSID_FIELD_NAME, entityProperties.getHeaders().getClassIdField());
            Message message = new Message(entity.getMessage(), properties);
            template.send(entity.getExchangeName(), entity.getRoutingKey(), message, new CorrelationData(Long.toString(entity.getId())));
        }catch (AmqpException e)
        {
            logger.warn("Отправка outbox ивента произошла безуспешно: Outbox event: {}. Ошибка: {}", entity, e);
            return;
        }catch (Exception j)
        {
            logger.warn("Отправка outbox ивента произошла безуспешно: Outbox event: {}. Ошибка: {}", entity, j);
            return;
        }
    }
    @Async("recoveryExecutor")
    @Scheduled(initialDelay = 10000, fixedDelay = 10000)
    @Transactional
    public void scheduleRecoveryTask() {
        logger.debug("Выполнение потока восстановления для отправки Outbox ивентов.");
        List<RabbitOutboxEntity> notRecoveredList = rabbitOutboxRepository.findSkipLockedBy();
        logger.debug("Получение блокировок и последующие попытки отправок на следующие ивенты : {}", notRecoveredList);
        for(RabbitOutboxEntity entity : notRecoveredList)
        {
            handleOutboxEntity(entity);
        }
    }
}
