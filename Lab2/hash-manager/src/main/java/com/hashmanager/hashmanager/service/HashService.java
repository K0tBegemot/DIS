package com.hashmanager.hashmanager.service;

import com.hashmanager.hashmanager.api.dto.HashCrackTaskDTO;
import com.hashmanager.hashmanager.api.dto.SimpleIdentity;
import com.hashmanager.hashmanager.api.dto.StatusDTO;
import com.hashmanager.hashmanager.api.entity.DiapasonPartEntity;
import com.hashmanager.hashmanager.api.entity.ExecutorServicePartEntity;
import com.hashmanager.hashmanager.api.entity.HashCrackTaskEntity;
import com.hashmanager.hashmanager.api.event.RabbitListenerEvent;
import com.hashmanager.hashmanager.api.mapper.HashCrackTaskMapper;
import com.hashmanager.hashmanager.exception.persistence.EntityNotFoundException;
import com.hashmanager.hashmanager.repository.ExecutorServicePartRepository;
import com.hashmanager.hashmanager.repository.HashCrackTaskRepository;
import com.rabbitmq.client.Channel;
import hashapi.api.ExecutorServicePartDTO;
import hashapi.api.WorkPartCompleteDTO;
import jakarta.validation.Valid;
import org.paukov.combinatorics3.Generator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.*;

@Service
@Transactional
public class HashService implements ApplicationEventPublisherAware {
    private static final Logger logger = LoggerFactory.getLogger(HashService.class);
    private static final List<Character> ALPHABET = List.of(
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
            'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q',
            'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'
            );
    private static final Character FIRST_CHARACTER = 'a';
    private HashCrackTaskMapper hashCrackTaskMapper;
    @Value("${executor.number}")
    private Long executorNumber;
    @Value("${executor.queue-name}")
    private String workerQueueName;
    @Value("${executor.exchange-name}")
    private String workerExchangeName;
    private HashCrackTaskRepository hashCrackTaskRepository;
    private OutboxService outboxService;
    private ApplicationEventPublisher applicationEventPublisher;
    private final ExecutorServicePartRepository executorServicePartRepository;

    public HashService(HashCrackTaskMapper hashCrackTaskMapper1, HashCrackTaskRepository hashCrackTaskRepository1,
                       OutboxService outboxService1,
                       ExecutorServicePartRepository executorServicePartRepository)
    {
        hashCrackTaskMapper = hashCrackTaskMapper1;
        hashCrackTaskRepository = hashCrackTaskRepository1;
        outboxService = outboxService1;
        this.executorServicePartRepository = executorServicePartRepository;
    }

    @Transactional
    public SimpleIdentity startHashCrackTask(HashCrackTaskDTO newTask)
    {
        HashCrackTaskEntity newTaskEntity = hashCrackTaskMapper.dtoToEntity(newTask);
        Long maxLength = newTask.getMaxLength();
        logger.debug("Максимальная длинна рассчитываемого слова : {}", maxLength);
        Map<Integer, Long> wordNumberMap = new HashMap<>();
        Long sum = 0L;
        for(int i = 1; i <= maxLength; i++)
        {
            long combinationNumber = Generator.permutation(ALPHABET).withRepetitions(i).stream().count();
            sum = sum + combinationNumber;
            wordNumberMap.put(i, combinationNumber);
            logger.debug("Количество слов длинны {} - {}", i, combinationNumber);
        }
        logger.debug("Необходимо перебрать слов - {}", sum);
        Map<Long, ExecutorServicePartEntity> executorMap = new HashMap<>();
        for(long i = 0; i < executorNumber; i++)
        {
            ExecutorServicePartEntity executor = new ExecutorServicePartEntity();
            executor.setExecutorId(i);
            newTaskEntity.addServicePart(executor);
            executorMap.put(i, executor);
        }
        for(int i = 1; i <= maxLength; i++)
        {
            logger.debug("Распределение диапазонов слов одной длинны между исполнителями. Длинна - {}", i);
            Long wordNumber = wordNumberMap.get(i);
            Long executorWordNumber = wordNumber / executorNumber;
            Long lastExecutorDiff = wordNumber % executorNumber;
            logger.debug("Количество слов для каждого исполнителя: {}. Остаток: {}", executorWordNumber, lastExecutorDiff);
            if(executorWordNumber == 0)
            {
                logger.debug("Задачу нет смысла разделять. Всё достанется первому исполнителю. Количество слов: {}", lastExecutorDiff);
                ExecutorServicePartEntity executor = executorMap.get(0L);
                DiapasonPartEntity part = new DiapasonPartEntity();
                part.setWordNumber(lastExecutorDiff);
                part.setFirstWordIndex(0L);
                part.setWordLength((long) i);
                executor.addDiapason(part);
            }else{
                long firstIndex = 0;
                for(long j = 0; j < executorNumber; j++)
                {
                    ExecutorServicePartEntity executor = executorMap.get(j);
                    DiapasonPartEntity part = new DiapasonPartEntity();
                    long workerWordNumber = (j == executorWordNumber - 1 ? executorWordNumber + lastExecutorDiff : executorWordNumber);
                    part.setWordNumber(workerWordNumber);
                    part.setFirstWordIndex(firstIndex);
                    part.setWordLength((long) i);
                    executor.addDiapason(part);
                    firstIndex += workerWordNumber;
                }
            }

        }
        newTaskEntity = hashCrackTaskRepository.save(newTaskEntity);
        List<ExecutorServicePartEntity> partList = newTaskEntity.getParts();
        List<ExecutorServicePartDTO> dtoList = new LinkedList<>();
        for (ExecutorServicePartEntity entity:
             partList) {
            dtoList.add(hashCrackTaskMapper.prtEntityToDTO(entity, newTaskEntity.getHash()));
        }
        logger.debug("Задача с идентификатором {} была разделена на следующие части : {}", newTaskEntity.getId(), dtoList);
        outboxService.addToOutbox(dtoList, workerExchangeName, workerQueueName);
        return new SimpleIdentity(newTaskEntity.getId());
    }
    @Transactional
    public StatusDTO getTaskStatus(Long taskId)
    {
        Optional<HashCrackTaskEntity> entityOptional = hashCrackTaskRepository.findById(taskId);
        if(entityOptional.isEmpty())
        {
            throw new EntityNotFoundException();
        }
        HashCrackTaskEntity entity = entityOptional.get();
        List<ExecutorServicePartEntity> partList = entity.getParts();
        boolean isCompleted = true;
        for(ExecutorServicePartEntity partEntity : partList)
        {
            if(!partEntity.getIsCompleted())
            {
                isCompleted = false;
                break;
            }
        }
        if(isCompleted)
        {
            entity.setStatus(HashCrackTaskEntity.Status.READY);
            hashCrackTaskRepository.save(entity);
        }
        return hashCrackTaskMapper.hashTaskToDTO(entity);
    }
    @RabbitListener(id = "${manager.queue-name}", queues = "${manager.queue-name}")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void workPartCompleteListener(@Payload @Valid WorkPartCompleteDTO completeDTO, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag){
        logger.debug("Получен ответ по одной из задач анализа : {} . Тег получения сообщения : {}", completeDTO, tag);
        Optional<HashCrackTaskEntity> entityOptional = hashCrackTaskRepository.findById(completeDTO.getTaskId());
        if(entityOptional.isEmpty())
        {
            logger.debug("Задача анализа с идентификатором {} не найдена", completeDTO.getTaskId());
            throw new EntityNotFoundException();
        }
        HashCrackTaskEntity entity = entityOptional.get();
        entity.addDataPartAll(completeDTO.getData());
        Optional<ExecutorServicePartEntity> partEntityOptional = executorServicePartRepository.findByExecutorIdAndParentTask_Id(completeDTO.getExecutorId(), completeDTO.getTaskId());
        if(partEntityOptional.isEmpty())
        {
            logger.debug("Часть задачи анализа {} для исполнителя с идентификатором {} не была найдена", completeDTO.getTaskId(), completeDTO.getExecutorId());
            throw new EntityNotFoundException();
        }
        ExecutorServicePartEntity partEntity = partEntityOptional.get();
        if(!partEntity.getIsCompleted())
        {
            hashCrackTaskRepository.save(entity);
            applicationEventPublisher.publishEvent(new RabbitListenerEvent(channel, tag));
            partEntity.setIsCompleted(true);
            executorServicePartRepository.save(partEntity);
        }else{
            logger.debug("Часть задачи анализа {} для исполнителя с идентификатором {} уже была выполнена. Повторный ответ будет выброшен.");
        }
    }
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void afterSuccessfulWorkComplete(RabbitListenerEvent event) throws Exception{
        logger.debug("Задача с тегом получения сообщения успешно сохранена {}", event.getTag());
        event.getChannel().basicAck(event.getTag(), false);
    }
    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    public void afterRollbackWorkComplete(RabbitListenerEvent event) throws Exception{
        logger.debug("Задача с тегом получения сообщения {} не была сохранена из-за откаченной транзакции", event.getTag());
        event.getChannel().basicNack(event.getTag(), false, true);
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher1) {
        applicationEventPublisher = applicationEventPublisher1;
    }
}