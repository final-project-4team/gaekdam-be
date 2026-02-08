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
        // DB
        r.add("spring.datasource.url", maria::getJdbcUrl);
        r.add("spring.datasource.username", maria::getUsername);
        r.add("spring.datasource.password", maria::getPassword);
        r.add("spring.datasource.driver-class-name",
                () -> "org.mariadb.jdbc.Driver");

        // 필수 placeholder들
        r.add("JWT_SECRET_B64",
                () -> "og9OBhk6xH4Quwa+mWk37sISpvQ2C0ONg5l6jJRCf30=");
        r.add("CRYPTO_HMAC_PEPPER_B64",
                () -> "og9OBhk6xH4Quwa+mWk37sISpvQ2C0ONg5l6jJRCf30=");
        r.add("CRYPTO_LOCAL_KEK_B64",
                () -> "P32UXwOxycfN2L772hcLLC+jPcaaOzAzWiL2jdjPm+o=");

        // Redis health check 끔
        r.add("management.health.redis.enabled", () -> "false");
        r.add("spring.data.redis.host", () -> "localhost");
        r.add("spring.data.redis.port", () -> "6379");

        // Mail placeholder
        r.add("MAIL_USERNAME", () -> "test@example.com");
        r.add("MAIL_PASSWORD", () -> "dummy");

        r.add("SOLAPI_API_KEY", () -> "dummy");
        r.add("SOLAPI_API_SECRET", () -> "dummy");
    }
}
