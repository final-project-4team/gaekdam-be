package com.gaekdam.gaekdambe.unit.iam_service.auth.command.application.service;

import com.gaekdam.gaekdambe.global.config.jwt.RefreshTokenService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @InjectMocks
    private RefreshTokenService service;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Test
    @DisplayName("save: 리프레시 토큰 저장")
    void save() {
        // given
        String userId = "user1";
        String token = "refresh-token";
        long expire = 1000L;

        given(redisTemplate.opsForValue()).willReturn(valueOperations);

        // when
        service.save(userId, token, expire);

        // then
        verify(valueOperations).set(eq("refresh:" + userId), eq(token), eq(Duration.ofMillis(expire)));
    }

    @Test
    @DisplayName("isValid: 토큰 유효성 검증 성공")
    void isValid_success() {
        // given
        String userId = "user1";
        String token = "refresh-token";

        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get("refresh:" + userId)).willReturn(token);

        // when
        boolean result = service.isValid(userId, token);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("isValid: 토큰 불일치 시 실패")
    void isValid_fail_mismatch() {
        // given
        String userId = "user1";
        String token = "refresh-token";
        String storedToken = "other-token";

        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get("refresh:" + userId)).willReturn(storedToken);

        // when
        boolean result = service.isValid(userId, token);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("isValid: 저장된 토큰 없으면 실패")
    void isValid_fail_null() {
        // given
        String userId = "user1";
        String token = "refresh-token";

        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get("refresh:" + userId)).willReturn(null);

        // when
        boolean result = service.isValid(userId, token);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("delete: 토큰 삭제")
    void delete() {
        // given
        String userId = "user1";

        // when
        service.delete(userId);

        // then
        verify(redisTemplate).delete("refresh:" + userId);
    }
}
