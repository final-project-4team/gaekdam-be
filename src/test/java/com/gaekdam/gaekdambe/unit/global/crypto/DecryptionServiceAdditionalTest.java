package com.gaekdam.gaekdambe.unit.global.crypto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import com.gaekdam.gaekdambe.global.crypto.AesCryptoUtils;
import com.gaekdam.gaekdambe.global.crypto.DecryptionService;
import com.gaekdam.gaekdambe.global.crypto.KmsService;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class DecryptionServiceAdditionalTest {

    @Mock
    KmsService kmsService;

    private DecryptionService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new DecryptionService(kmsService);
    }

    @Test
    void decrypt_handles_null_dekEnc_and_null_encryptedData() {
        // if encryptedData null -> returns null regardless of dekEnc
        assertThat(service.decrypt(1L, null, null)).isNull();
        // if dekEnc null but encryptedData present -> still calls KMS via cache loader
        byte[] dekPlain = new byte[32];
        for (int i = 0; i < 32; i++) dekPlain[i] = (byte) i;
        when(kmsService.decryptDataKey(org.mockito.ArgumentMatchers.any())).thenReturn(dekPlain);
        String payload = "v";
        byte[] encrypted = AesCryptoUtils.encrypt(payload, dekPlain);
        String res = service.decrypt(2L, null, encrypted);
        assertThat(res).isEqualTo(payload);
    }

    @Test
    void decrypt_propagates_kms_exception() {
        doThrow(new RuntimeException("KMS error")).when(kmsService)
            .decryptDataKey(org.mockito.ArgumentMatchers.any());

        byte[] dekEnc = new byte[]{1,2,3};
        byte[] encrypted = new byte[]{0}; // won't reach Aes decrypt due to kms exception
        assertThrows(RuntimeException.class, () -> service.decrypt(1L, dekEnc, encrypted));
    }

    @Test
    void cache_concurrent_access_multiple_keys() throws InterruptedException {
        byte[] dekPlain1 = new byte[32];
        byte[] dekPlain2 = new byte[32];
        for (int i = 0; i < 32; i++) { dekPlain1[i] = (byte) i; dekPlain2[i] = (byte) (i + 1);} 
        // when called for different cache keys, return corresponding plaintexts
        when(kmsService.decryptDataKey(org.mockito.ArgumentMatchers.any()))
            .thenAnswer(invocation -> {
                byte[] arg = invocation.getArgument(0);
                if (arg != null && arg.length > 0 && arg[0] == 9) return dekPlain1;
                return dekPlain2;
            });

        String payload1 = "p1";
        String payload2 = "p2";
        byte[] enc1 = AesCryptoUtils.encrypt(payload1, dekPlain1);
        byte[] enc2 = AesCryptoUtils.encrypt(payload2, dekPlain2);

        int threads = 10;
        ExecutorService ex = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);

        for (int i = 0; i < threads; i++) {
            final int idx = i;
            ex.submit(() -> {
                try {
                    start.await();
                    long key = (idx % 2 == 0) ? 100L : 200L; // two cache keys
                    byte[] dekEnc = (idx % 2 == 0) ? new byte[]{9} : new byte[]{8};
                    String res = service.decrypt(key, dekEnc, (idx % 2 == 0) ? enc1 : enc2);
                    assertThat(res).isIn(payload1, payload2);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    done.countDown();
                }
            });
        }
        start.countDown();
        done.await();
        ex.shutdownNow();
    }
}
