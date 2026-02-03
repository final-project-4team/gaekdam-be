package com.gaekdam.gaekdambe.unit.global.crypto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gaekdam.gaekdambe.global.crypto.DecryptionService;
import com.gaekdam.gaekdambe.global.crypto.KmsService;
import com.gaekdam.gaekdambe.global.crypto.AesCryptoUtils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class DecryptionServiceTest {

    @Mock
    KmsService kmsService;

    private DecryptionService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new DecryptionService(kmsService);
    }

    @Test
    void decrypt_returnsNull_whenEncryptedDataNull() {
        assertThat(service.decrypt(1L, new byte[]{1}, null)).isNull();
    }

    @Test
    void getPlaintextDek_usesKmsService_onMiss_then_cache_and_clear() {
        byte[] dekEnc1 = new byte[]{9,9,9};
        byte[] dekPlain = new byte[32];
        for (int i = 0; i < dekPlain.length; i++) dekPlain[i] = (byte) (i + 1);

        when(kmsService.decryptDataKey(org.mockito.ArgumentMatchers.any())).thenReturn(dekPlain);

        // first decrypt -> kms called
        String payload = "sensitive-info";
        byte[] encryptedPayload = AesCryptoUtils.encrypt(payload, dekPlain);

        String res1 = service.decrypt(555L, dekEnc1, encryptedPayload);
        assertThat(res1).isEqualTo(payload);
        verify(kmsService, times(1)).decryptDataKey(org.mockito.ArgumentMatchers.any());

        // second decrypt with same cache key -> should not call kms again
        String res2 = service.decrypt(555L, new byte[]{8,8,8}, encryptedPayload);
        assertThat(res2).isEqualTo(payload);
        verify(kmsService, times(1)).decryptDataKey(org.mockito.ArgumentMatchers.any());

        // clear and call -> kms called again
        service.clearCache();
        String res3 = service.decrypt(555L, new byte[]{7,7,7}, encryptedPayload);
        assertThat(res3).isEqualTo(payload);
        verify(kmsService, times(2)).decryptDataKey(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void decrypt_throws_on_invalid_ciphertext() {
        byte[] dekPlain = new byte[32];
        when(kmsService.decryptDataKey(org.mockito.ArgumentMatchers.any())).thenReturn(dekPlain);

        byte[] bad = new byte[]{1,2,3,4};
        assertThrows(RuntimeException.class, () -> service.decrypt(1L, new byte[]{1}, bad));
    }
}
