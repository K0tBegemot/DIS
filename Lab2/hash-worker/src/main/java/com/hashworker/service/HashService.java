package com.hashworker.service;

import com.rabbitmq.client.Channel;
import hashapi.api.DiapasonPartDTO;
import hashapi.api.ExecutorServicePartDTO;
import hashapi.api.WorkPartCompleteDTO;
import jakarta.validation.Valid;
import org.paukov.combinatorics3.Generator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.connection.ThreadChannelConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.core.task.TaskExecutor;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

@Service
public class HashService{
    private static final Logger logger = LoggerFactory.getLogger(HashService.class);
    private static final List<Character> ALPHABET = List.of(
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
            'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q',
            'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'
    );
    private static final Character FIRST_CHARACTER = 'a';
    @Value("${manager.exchange-name}")
    private String managerExchange;
    @Value("${manager.queue-name}")
    private String managerRoutingKey;
    private ExecutorService executorService;
    private RabbitTemplate rabbitTemplate;
    public HashService(RabbitTemplate rabbitTemplate1, @Qualifier("hashTaskExecutor") ExecutorService executorService1)
    {
        rabbitTemplate = rabbitTemplate1;
        executorService = executorService1;
    }

    private static String characterListToString(List<Character> characterList){
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < characterList.size(); i++)
        {
            builder.append(characterList.get(i));
        }
        return builder.toString();
    }

    @RabbitListener(id = "${executor.queue-name}", queues = "${executor.queue-name}")
    public void executeTask(@Payload @Valid ExecutorServicePartDTO dto, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException, ExecutionException, InterruptedException {
        logger.debug("Получена задача рассчёта хеша : {} . Delivery tag : {} .", dto, tag);
        Future<WorkPartCompleteDTO> result = executorService.submit(() -> {
            try {
                List<String> resultArray = new ArrayList<>();
                String hash = dto.getHash();
                MessageDigest digest = MessageDigest.getInstance("MD5");
                for (DiapasonPartDTO part:
                     dto.getParts()) {
                    logger.debug("Часть {} задачи {} . Рассчёт диапазона {} .", dto.getExecutorId(), dto.getTaskId(), part);
                    Long wordNumber = part.getWordNumber();
                    var iterator = Generator.permutation(ALPHABET).withRepetitions(Math.toIntExact(part.getWordLength())).stream().iterator();
                    for(int i = 0; i < part.getFirstWordIndex(); i++)
                    {
                        iterator.next();
                    }
                    for (int i = 0; i < wordNumber; i++) {
                        List<Character> combination = iterator.next();
                        String combinationString = new String(characterListToString(combination));
                        if(combinationString.equals("10"))
                        {
                            logger.debug("Строка 10 присутствует, но хещ не тот...");
                        }
                        String digestString = String.format("%032x", new BigInteger(1, digest.digest(combinationString.getBytes(StandardCharsets.UTF_8))));
                        if(hash.equals(digestString))
                        {
                            logger.debug("Часть {} задачи {} . Найдено совпадение по хешу : {}", dto.getExecutorId(), dto.getTaskId(), combinationString);
                            resultArray.add(combinationString);
                        }
                    }
                }
                return WorkPartCompleteDTO.builder().taskId(dto.getTaskId()).executorId(dto.getExecutorId()).data(resultArray).build();
            }catch(NoSuchAlgorithmException e)
            {
                return null;
            }
        });
        WorkPartCompleteDTO completeDTO = result.get();
        logger.debug("Часть {} задачи {} рассчитана. Результат : {} .", dto.getExecutorId(), dto.getTaskId(), completeDTO);
        if(completeDTO == null)
        {
            logger.error("При рассчёте задачи {} что-то пошло не так. Задача будет возвращена в очередь", dto);
            channel.basicNack(tag, false, true);
        }
        Boolean isConfirmed = rabbitTemplate.invoke((t) -> {
            t.convertAndSend(managerExchange, managerRoutingKey, completeDTO);
            return t.waitForConfirms(10000);
        });
            if (isConfirmed != null && isConfirmed) {
                logger.debug("Результат задачи {} был успешно помещен в очередь.", dto);
                channel.basicAck(tag, false);
            }else{
                logger.debug("При передаче результата задачи {} в очередь либо вышел таймаут, либо произошла ошибка. Задаа будет возвращена в очередь.", dto);
                channel.basicNack(tag, false, true);
            }
    }
}