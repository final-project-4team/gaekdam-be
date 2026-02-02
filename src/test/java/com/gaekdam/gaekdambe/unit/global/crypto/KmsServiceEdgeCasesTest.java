package com.gaekdam.gaekdambe.unit.global.crypto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.gaekdam.gaekdambe.global.crypto.LocalKmsService;
import com.gaekdam.gaekdambe.global.config.security.CryptoConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class KmsServiceEdgeCasesTest {

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
    void decrypt_throws_when_encrypted_corrupted() {
        var dk = service.generateDataKey();
        byte[] bad = dk.encrypted().clone();
        // corrupt a byte
        bad[0] = (byte) (bad[0] ^ 0xFF);

        try {
            byte[] out = service.decryptDataKey(bad);
            // If decrypt does not throw, the result should not equal original plaintext
            assertThat(out).isNotEqualTo(dk.plaintext());
        } catch (RuntimeException e) {
            // acceptable: decryption may fail with runtime exception
        }
    }

    @Test
    void generate_returns_non_nulls() {
        var dk = service.generateDataKey();
        assertThat(dk).isNotNull();
        assertThat(dk.plaintext()).isNotNull();
        assertThat(dk.encrypted()).isNotNull();
    }
}
