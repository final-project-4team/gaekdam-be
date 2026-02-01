package com.gaekdam.gaekdambe;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {

                "spring.data.redis.repositories.enabled=false",

                "spring.task.scheduling.enabled=false",

                "DB_HOST=dummy",
                "DB_PORT=3306",
                "DB_NAME=dummy",
                "DB_USERNAME=dummy",
                "DB_PASSWORD=dummy",

                "REDIS_HOST=dummy",
                "REDIS_PORT=6379",
                "REDIS_PASSWORD=",

                "MAIL_USERNAME=dummy",
                "MAIL_PASSWORD=dummy",

                "JWT_SECRET_B64=QUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUE=",

                "CRYPTO_LOCAL_KEK_B64=P32UXwOxycfN2L772hcLLC+jPcaaOzAzWiL2jdjPm+o=",
                "CRYPTO_HMAC_PEPPER_B64=og9OBhk6xH4Quwa+mWk37sISpvQ2C0ONg5l6jJRCf30="
        }
)
@ActiveProfiles("test")
public @interface IntegrationTest {
}
