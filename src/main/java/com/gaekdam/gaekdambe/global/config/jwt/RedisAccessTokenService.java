package com.gaekdam.gaekdambe.global.config.jwt;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisAccessTokenService {

  private final StringRedisTemplate redisTemplate;

  private String getKey(String userId) {
    return "access:" + userId;
  }


  public void save(String userId, String accessToken) {

    String key = getKey(userId);

    try {
      redisTemplate.opsForValue().set(
          key,
          accessToken);
      log.info("Redis save OK. key={}", key);
    } catch (Exception e) {
      log.error("Redis save FAILED", e);
      throw e;
    }

    log.debug("accessToken 저장 완료. userId={}", userId);
  }

  // 유효성 검증
  public boolean isValid(String userId, String accessTokenToken) {
    String stored = redisTemplate.opsForValue().get(getKey(userId));
    return stored != null && stored.equals(accessTokenToken);
  }

  // 삭제
  public void delete(String userId) {
    redisTemplate.delete(getKey(userId));
    log.debug("accessToken 삭제 완료. userId={}", userId);
  }
}
