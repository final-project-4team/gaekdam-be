package com.gaekdam.gaekdambe.unit.global.crypto;

import static org.assertj.core.api.Assertions.assertThat;

import com.gaekdam.gaekdambe.global.crypto.HexUtils;
import org.junit.jupiter.api.Test;

class HexUtilsTest {

    @Test
    void toHex_handles_null_and_bytes() {
        assertThat(HexUtils.toHex(null)).isNull();
        assertThat(HexUtils.toHex(new byte[] {0x0F, (byte)0xA0})).isEqualTo("0FA0");
    }
}
