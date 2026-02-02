package com.gaekdam.gaekdambe.global.config.jwt;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

  private final StringRedisTemplate redisTemplate;

  private String getKey(String userId) {
    return "refresh:" + userId;
  }

  // RefreshToken 저장
  @Transactional
  public void save(String userId, String refreshToken, long expireTimeMillis) {

    String key = getKey(userId);

      try {
          redisTemplate.opsForValue().set(
                  key,
                  refreshToken,
                  Duration.ofMillis(expireTimeMillis)
          );
          log.info("Redis save OK. key={}", key);
      } catch (Exception e) {
          log.error("Redis save FAILED", e);
          throw e;
      }

    log.debug("RefreshToken 저장 완료. userId={}, expire={}ms", userId, expireTimeMillis);
  }

  // RefreshToken 유효성 검증
  public boolean isValid(String userId, String refreshToken) {
    String stored = redisTemplate.opsForValue().get(getKey(userId));
    return stored != null && stored.equals(refreshToken);
  }

  // RefreshToken 삭제
  public void delete(String userId) {
    redisTemplate.delete(getKey(userId));
    log.debug("RefreshToken 삭제 완료. userId={}", userId);
  }
}

