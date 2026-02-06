package com.gaekdam.gaekdambe.unit.global.crypto;

import static org.assertj.core.api.Assertions.assertThat;

import com.gaekdam.gaekdambe.global.crypto.HmacSha256SearchHashService;
import com.gaekdam.gaekdambe.global.config.security.CryptoConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class HmacSha256SearchHashServiceTest {

    @Mock
    CryptoConfig cryptoConfig;

    private HmacSha256SearchHashService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // provide a predictable pepper
        org.mockito.Mockito.when(cryptoConfig.getHmac()).thenReturn(new CryptoConfig.Hmac() {
            public byte[] getPepperBytes() { return new byte[32]; }
        });
        service = new HmacSha256SearchHashService(cryptoConfig);
    }

    @Test
    void hmac_hashes_and_handles_null() {
        byte[] e = service.emailHash("Test@EXAMPLE.COM");
        byte[] p = service.phoneHash("010-1234-5678");
        byte[] n = service.nameHash("John Doe");
        assertThat(e).isNotNull();
        assertThat(p).isNotNull();
        assertThat(n).isNotNull();

        assertThat(service.emailHash(null)).isNull();
    }
}
