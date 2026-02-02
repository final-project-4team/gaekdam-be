package com.gaekdam.gaekdambe.unit.global.crypto;

import static org.assertj.core.api.Assertions.assertThat;

import com.gaekdam.gaekdambe.global.crypto.Normalizer;
import org.junit.jupiter.api.Test;

class NormalizerTest {

    @Test
    void email_normalize_and_null() {
        assertThat(Normalizer.email(null)).isNull();
        assertThat(Normalizer.email("  ABC@EXAMPLE.COM  ")).isEqualTo("abc@example.com");
    }

    @Test
    void phone_normalize() {
        assertThat(Normalizer.phone(null)).isNull();
        assertThat(Normalizer.phone("010-1234-5678")).isEqualTo("01012345678");
    }

    @Test
    void name_normalize() {
        assertThat(Normalizer.name(null)).isNull();
        assertThat(Normalizer.name("  John   Doe  ")).isEqualTo("John Doe");
    }
}
