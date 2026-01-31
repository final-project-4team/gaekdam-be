package com.gaekdam.gaekdambe;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public abstract class IntegrationTestBase {

    @Container
    static final MariaDBContainer<?> maria =
            new MariaDBContainer<>("mariadb:11.4")
                    .withDatabaseName("test")
                    .withUsername("test")
                    .withPassword("test");

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", maria::getJdbcUrl);
        r.add("spring.datasource.username", maria::getUsername);
        r.add("spring.datasource.password", maria::getPassword);
        r.add("spring.datasource.driver-class-name", () -> "org.mariadb.jdbc.Driver");
    }
}
