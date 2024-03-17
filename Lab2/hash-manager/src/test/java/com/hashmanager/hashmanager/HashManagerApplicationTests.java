package com.hashmanager.hashmanager;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(Infrastructure.class)
class HashManagerApplicationTests {
    @Test
    void contextLoads() {
    }
}