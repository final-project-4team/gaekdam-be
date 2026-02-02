package com.gaekdam.gaekdambe.unit.global.crypto;

import static org.assertj.core.api.Assertions.assertThat;

import com.gaekdam.gaekdambe.global.crypto.MaskingUtils;
import org.junit.jupiter.api.Test;

class MaskingUtilsTest {

    @Test
    void maskName_various() {
        assertThat(MaskingUtils.maskName(null)).isEqualTo("***");
        assertThat(MaskingUtils.maskName("")).isEqualTo("***");
        assertThat(MaskingUtils.maskName("A")).isEqualTo("A");
        assertThat(MaskingUtils.maskName("AB")).isEqualTo("A*");
        assertThat(MaskingUtils.maskName("ABC")).isEqualTo("A*C");
        assertThat(MaskingUtils.maskName("ABCDE")).isEqualTo("A***E");
    }

    @Test
    void maskPhone_various() {
        assertThat(MaskingUtils.maskPhone(null)).isEqualTo("***-****-****");
        assertThat(MaskingUtils.maskPhone("010-1234-5678")).isEqualTo("010-****-5678");
        assertThat(MaskingUtils.maskPhone("0212345678")).isEqualTo("02-****-5678");
        assertThat(MaskingUtils.maskPhone("xyz")).isEqualTo("***-****-****");
    }

    @Test
    void maskEmail_various() {
        assertThat(MaskingUtils.maskEmail(null)).isEqualTo("***@***.***");
        assertThat(MaskingUtils.maskEmail("a@b.com")).isEqualTo("a***@b.com");
        assertThat(MaskingUtils.maskEmail("abcd@domain.com")).isEqualTo("abc***@domain.com");
        assertThat(MaskingUtils.maskEmail("invalidemail")).isEqualTo("***@***.***");
    }
}
