package com.gaekdam.gaekdambe.unit.global.crypto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.gaekdam.gaekdambe.global.crypto.AesCryptoUtils;
import org.junit.jupiter.api.Test;

class AesCryptoUtilsAdditionalTest {

    @Test
    void encrypt_decrypt_roundtrip_and_null_handling() {
        byte[] key = new byte[32];
        for (int i = 0; i < key.length; i++) key[i] = (byte) i;

        String plaintext = "Hello World";
        byte[] encrypted = AesCryptoUtils.encrypt(plaintext, key);
        assertThat(encrypted).isNotNull();

        String decrypted = AesCryptoUtils.decrypt(encrypted, key);
        assertThat(decrypted).isEqualTo(plaintext);

        // null handling
        assertThat(AesCryptoUtils.encrypt(null, key)).isNull();
        assertThat(AesCryptoUtils.decrypt(null, key)).isNull();
    }

    @Test
    void decrypt_throws_on_bad_key() {
        byte[] key = new byte[32];
        for (int i = 0; i < key.length; i++) key[i] = (byte) i;
        byte[] encrypted = AesCryptoUtils.encrypt("secret", key);
        byte[] badKey = new byte[32];
        badKey[0] = 1;
        assertThrows(RuntimeException.class, () -> AesCryptoUtils.decrypt(encrypted, badKey));
    }
}
