package com.gaekdam.gaekdambe;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
class RedisIT extends IntegrationTestBase {

    @Autowired StringRedisTemplate redisTemplate;

    @DisplayName("Redis 컨테이너에 set/get이 된다")
    @Test
    void redis_set_get() {
        redisTemplate.opsForValue().set("ping", "pong");
        String v = redisTemplate.opsForValue().get("ping");
        assertThat(v).isEqualTo("pong");
    }
}
