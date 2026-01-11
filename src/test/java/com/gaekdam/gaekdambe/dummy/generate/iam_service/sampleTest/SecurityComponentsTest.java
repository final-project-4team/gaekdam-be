package com.gaekdam.gaekdambe.dummy.generate.iam_service.sampleTest;

import com.gaekdam.gaekdambe.global.config.jwt.JwtTokenProvider;
import com.gaekdam.gaekdambe.global.crypto.AesCryptoUtils;
import com.gaekdam.gaekdambe.global.crypto.SearchHashService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.security.SecureRandom;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class SecurityComponentsTest {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private SearchHashService searchHashService;

    @Test
    @DisplayName("JWT 토큰 생성 및 검증 테스트")
    void testJwtTokenWorkflow() {
        // Given: 테스트할 사용자 ID와 권한 정보
        String userId = "testUser";
        String role = "ROLE_USER";

        //Access Token과 Refresh Token을 생성
        String accessToken = jwtTokenProvider.createAccessToken(userId, role);
        String refreshToken = jwtTokenProvider.createRefreshToken(userId, role);

        //  토큰이 확인.
        assertThat(accessToken).isNotNull();
        assertThat(refreshToken).isNotNull();

        // 생성된 토큰이 유효한지 검증
        assertThat(jwtTokenProvider.validateToken(accessToken)).isTrue();
        assertThat(jwtTokenProvider.validateToken(refreshToken)).isTrue();

        //토큰에서 사용자 정보 타입이 올바르게 추출되는지 확인합니다.
        assertThat(jwtTokenProvider.getUsername(accessToken)).isEqualTo(userId);
        assertThat(jwtTokenProvider.getRole(accessToken)).isEqualTo(role);
        assertThat(jwtTokenProvider.getTokenType(accessToken)).isEqualTo("access");
        assertThat(jwtTokenProvider.getTokenType(refreshToken)).isEqualTo("refresh");
    }

    @Test
    @DisplayName("AES-256 암호화 및 복호화 정합성 테스트")
    void testAesEncryptionDecryption() {
        //  암호화할 원본 데이터와 32바이트 AES 키를 준비합니다.
        String originalText = "테스트 비밀 데이터 1234";
        byte[] key = new byte[32];
        new SecureRandom().nextBytes(key);

        // 데이터를 암호화하고, 다시 복호화를 수행
        byte[] encrypted = AesCryptoUtils.encrypt(originalText, key);
        String decrypted = AesCryptoUtils.decrypt(encrypted, key);

        // 암 복호화된 데이터는 원본과 일치검사
        assertThat(encrypted).isNotNull();
        assertThat(decrypted).isEqualTo(originalText);
    }

    @Test
    @DisplayName("HMAC 검색용 해시 일관성 테스트")
    void testHmacHashing() {
        // Given
        String email = "test@example.com";
        String phone = "010-1234-5678";
        String name = "홍길동";

        //  동일한 입력값으로 해시를 두 번 생성합니다.
        byte[] emailHash1 = searchHashService.emailHash(email);
        byte[] emailHash2 = searchHashService.emailHash(email);

        byte[] phoneHash1 = searchHashService.phoneHash(phone);
        byte[] phoneHash2 = searchHashService.phoneHash(phone);

        byte[] nameHash1 = searchHashService.nameHash(name);
        byte[] nameHash2 = searchHashService.nameHash(name);

        //  동일한 입력값에 대해서는 항상 동일한 해시값이 반환
        assertThat(emailHash1).isEqualTo(emailHash2);
        assertThat(phoneHash1).isEqualTo(phoneHash2);
        assertThat(nameHash1).isEqualTo(nameHash2);

        //  서로 다른 입력값은 다른 해시값
        byte[] diffEmailHash = searchHashService.emailHash("other@example.com");
        assertThat(emailHash1).isNotEqualTo(diffEmailHash);
    }
}
