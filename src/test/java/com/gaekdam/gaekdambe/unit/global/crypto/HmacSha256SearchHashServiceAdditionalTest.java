package com.gaekdam.gaekdambe.unit.global.crypto;

import static org.assertj.core.api.Assertions.assertThat;

import com.gaekdam.gaekdambe.global.crypto.HmacSha256SearchHashService;
import com.gaekdam.gaekdambe.global.config.security.CryptoConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class HmacSha256SearchHashServiceAdditionalTest {

    @Mock
    CryptoConfig cryptoConfig;

    private HmacSha256SearchHashService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // small pepper for determinism
        org.mockito.Mockito.when(cryptoConfig.getHmac()).thenReturn(new CryptoConfig.Hmac() {
            public byte[] getPepperBytes() { return "pepperpepperpepperpepperpepper!!".getBytes(); }
        });
        service = new HmacSha256SearchHashService(cryptoConfig);
    }

    @Test
    void null_inputs_return_null() {
        assertThat(service.emailHash(null)).isNull();
        assertThat(service.phoneHash(null)).isNull();
        assertThat(service.nameHash(null)).isNull();
    }

    @Test
    void consistent_hashes_for_same_input() {
        byte[] a = service.emailHash("Test@Example.com");
        byte[] b = service.emailHash("Test@Example.com");
        assertThat(a).isEqualTo(b);
    }
}
