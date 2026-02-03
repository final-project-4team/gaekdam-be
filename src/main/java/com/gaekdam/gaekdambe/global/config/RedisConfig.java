package com.gaekdam.gaekdambe.global.config;

import jakarta.annotation.PostConstruct;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;


@Configuration
@Slf4j
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(
            LettuceConnectionFactory connectionFactory) {

        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(RedisSerializer.json());

        return template;
    }

    @Autowired
    private Environment environment;

    public RedisConfig(Environment environment) {
        this.environment = environment;
    }

    @PostConstruct
    public void logRedisConfig() {
        log.info(
                "Redis SSL enabled = {}, protocols = {}",
                environment.getProperty("spring.data.redis.ssl.enabled"),
                environment.getProperty("spring.data.redis.ssl.protocols")
        );
    }
}
