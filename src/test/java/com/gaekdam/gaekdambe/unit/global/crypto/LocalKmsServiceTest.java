package com.gaekdam.gaekdambe.unit.global.crypto;

import static org.assertj.core.api.Assertions.assertThat;

import com.gaekdam.gaekdambe.global.crypto.LocalKmsService;
import com.gaekdam.gaekdambe.global.config.security.CryptoConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class LocalKmsServiceTest {

    @Mock
    CryptoConfig cryptoConfig;

    private LocalKmsService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        org.mockito.Mockito.when(cryptoConfig.getLocalKek()).thenReturn(new CryptoConfig.LocalKek() {
            public byte[] getKeyBytes() { return new byte[16]; }
        });
        service = new LocalKmsService(cryptoConfig);
    }

    @Test
    void generate_and_decrypt_roundtrip() {
        var dk = service.generateDataKey();
        assertThat(dk).isNotNull();
        assertThat(dk.plaintext()).isNotNull();
        assertThat(dk.encrypted()).isNotNull();

        byte[] decrypted = service.decryptDataKey(dk.encrypted());
        assertThat(decrypted).isNotNull();
        assertThat(decrypted).isEqualTo(dk.plaintext());
    }
}
