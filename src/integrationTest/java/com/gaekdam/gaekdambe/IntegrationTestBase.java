package com.gaekdam.gaekdambe;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
public abstract class IntegrationTestBase {

    @Container
    static final MariaDBContainer<?> maria =
            new MariaDBContainer<>("mariadb:11.4")
                    .withDatabaseName("test")
                    .withUsername("test")
                    .withPassword("test");

    @Container
    static final GenericContainer<?> redis =
            new GenericContainer<>(DockerImageName.parse("redis:7.4"))
                    .withCommand("redis-server", "--requirepass", "testpass")
                    .withExposedPorts(6379);

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry r) {
        //  실제 DB 연결은 컨테이너 값으로 덮어씀
        r.add("spring.datasource.url", maria::getJdbcUrl);
        r.add("spring.datasource.username", maria::getUsername);
        r.add("spring.datasource.password", maria::getPassword);
        r.add("spring.datasource.driver-class-name", () -> "org.mariadb.jdbc.Driver");

        //  실제 Redis 연결도 컨테이너 값으로 덮어씀
        r.add("spring.data.redis.host", redis::getHost);
        r.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
        r.add("spring.data.redis.password", () -> "testpass");
    }
}
