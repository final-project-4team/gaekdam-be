package com.gaekdam.gaekdambe.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.StringUtils;

@Configuration
public class RedisConfig {

  @Value("${spring.data.redis.host}")
  private String redisHost;

  @Value("${spring.data.redis.port}")
  private int redisPort;

  @Value("${spring.data.redis.password:}")
  private String redisPassword;

  @Bean
  public LettuceConnectionFactory redisConnectionFactory() {
    RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
    redisStandaloneConfiguration.setPort(redisPort);
    redisStandaloneConfiguration.setHostName(redisHost);

      if (StringUtils.hasText(redisPassword)) {
          redisStandaloneConfiguration.setPassword(redisPassword);
      }

    return new LettuceConnectionFactory(
        redisStandaloneConfiguration
    // new RedisStandaloneConfiguration(redisHost, redisPort)
    );
  }

  @Bean
  public RedisTemplate<String, Object> redisTemplate(
      LettuceConnectionFactory connectionFactory) {
    RedisTemplate<String, Object> template = new RedisTemplate<>();

    template.setConnectionFactory(connectionFactory);

    // key는 String
    template.setKeySerializer(new StringRedisSerializer());

    // value는 JSON 직렬화  RedisSerializer.json()
    template.setValueSerializer(RedisSerializer.json());

    return template;
  }
}
