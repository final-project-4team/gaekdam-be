package com.gaekdam.gaekdambe;


import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ContextIT extends IntegrationTestBase {

    @Test
    void contextLoads() {
        // 스프링 + DB 컨테이너 뜨면 성공
    }
}
