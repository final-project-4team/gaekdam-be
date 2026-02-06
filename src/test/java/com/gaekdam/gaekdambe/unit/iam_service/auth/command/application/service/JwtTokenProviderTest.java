package com.gaekdam.gaekdambe.unit.iam_service.auth.command.application.service;

import com.gaekdam.gaekdambe.global.config.jwt.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;


import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private final String secretKey = "testSecretKeytestSecretKeytestSecretKeytestSecretKey"; // > 32bytes
    private final String secretKeyBase64 = Base64.getEncoder()
            .encodeToString(secretKey.getBytes(StandardCharsets.UTF_8));
    private final long accessExp = 100000L;
    private final long refreshExp = 200000L;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(secretKeyBase64, accessExp, refreshExp);
    }

    @Test
    @DisplayName("createAccessToken: 토큰 생성 및 클레임 확인")
    void createAccessToken() {
        // given
        String userId = "testUser";
        String role = "ADMIN";
        Long hgCode = 10L;
        Long propCode = 20L;

        // when
        String token = jwtTokenProvider.createAccessToken(userId, role, hgCode, propCode);

        // then
        assertThat(token).isNotNull();
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
        assertThat(jwtTokenProvider.getUsername(token)).isEqualTo(userId);
        assertThat(jwtTokenProvider.getRole(token)).isEqualTo(role);
        assertThat(jwtTokenProvider.getHotelGroupCode(token)).isEqualTo(hgCode);
        assertThat(jwtTokenProvider.getPropertyCode(token)).isEqualTo(propCode);
        assertThat(jwtTokenProvider.getTokenType(token)).isEqualTo("access");
    }

    @Test
    @DisplayName("validateToken: 만료된 토큰 검증 실패")
    void validateToken_expired() {
        // given
        // create expired provider
        JwtTokenProvider shortProvider = new JwtTokenProvider(secretKeyBase64, -100L, -100L);
        String token = shortProvider.createAccessToken("u", "r", 1L, 1L);

        // when
        boolean isValid = jwtTokenProvider.validateToken(token);

        // then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("validateToken: 잘못된 서명 토큰 검증 실패")
    void validateToken_invalidSignature() {
        // given
        String otherKey = "otherSecretKeyotherSecretKeyotherSecretKeyotherSecretKey";
        String otherBase64 = Base64.getEncoder().encodeToString(otherKey.getBytes());
        JwtTokenProvider otherProvider = new JwtTokenProvider(otherBase64, accessExp, refreshExp);
        String token = otherProvider.createAccessToken("u", "r", 1L, 1L);

        // when
        boolean isValid = jwtTokenProvider.validateToken(token);

        // then
        assertThat(isValid).isFalse();
    }
}
