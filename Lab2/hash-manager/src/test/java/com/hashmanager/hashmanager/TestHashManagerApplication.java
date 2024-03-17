package com.hashmanager.hashmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
@Import(Infrastructure.class)
public class TestHashManagerApplication {

    public static void main(String[] args) {
        SpringApplication.from(HashManagerApplication::main).with(TestHashManagerApplication.class).run(args);
    }

}