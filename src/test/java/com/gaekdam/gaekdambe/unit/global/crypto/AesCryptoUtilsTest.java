package com.gaekdam.gaekdambe.unit.global.crypto;

import static org.assertj.core.api.Assertions.assertThat;

import com.gaekdam.gaekdambe.global.crypto.AesCryptoUtils;
import org.junit.jupiter.api.Test;

class AesCryptoUtilsTest {

    @Test
    void decrypt_encrypts_and_decrypts_roundtrip() throws Exception {
        String plaintext = "hello world";
        byte[] key = new byte[16];
        for (int i = 0; i < key.length; i++) key[i] = (byte) i;

        byte[] encrypted = AesCryptoUtils.encrypt(plaintext, key);
        String decrypted = AesCryptoUtils.decrypt(encrypted, key);

        assertThat(decrypted).isEqualTo(plaintext);
    }
}
