package com.hashmanager.hashmanager.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.SimpleAsyncTaskScheduler;

@EnableScheduling
@EnableAsync
@Configuration
public class ScheduleConfig {
    @Bean
    public TaskExecutor recoveryExecutor(){
        return new SimpleAsyncTaskExecutor();
    }
}