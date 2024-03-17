package com.hashworker.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class TaskExecutorConfig {
    @Bean
    public ExecutorService hashTaskExecutor()
    {
        return Executors.newFixedThreadPool(12);
    }
}