package com.hashworker.config;

import com.hashworker.service.HashService;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
public class RabbitConfiguration {
    @Bean
    public TopicExchange executorExchange(@Value("${executor.exchange-name}") String exchangeName) {
        return new TopicExchange(exchangeName, true, false, null);
    }
    @Bean
    public TopicExchange managerExchange(@Value("${manager.exchange-name}")String exchangeName){
        return new TopicExchange(exchangeName, true, false, null);
    }
    @Bean
    public Queue executorQueue(@Value("${executor.queue-name}") String queueName)
    {
        return new Queue(queueName, true, false, false);
    }
    @Bean
    public Binding executorBinding(@Qualifier("executorExchange") TopicExchange executorExchange,
                                  @Qualifier("executorQueue") Queue executorQueue,
                                  @Value("${executor.queue-name}") String directKey
    ){
        return BindingBuilder.bind(executorQueue).to(executorExchange).with(directKey);
    }
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory)
    {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jacksonMessageConverter());
        return template;
    }
    @Bean
    public Jackson2JsonMessageConverter jacksonMessageConverter(){
        return new Jackson2JsonMessageConverter();
    }
}