package com.gaekdam.gaekdambe.global.config.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Base64;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "crypto")
public class CryptoConfig {

    private Hmac hmac = new Hmac();
    private LocalKek localKek = new LocalKek();
    private AwsKms awsKms=new AwsKms();


    @Getter
    @Setter
    public static class Hmac {
        private String pepperB64;

        public byte[] getPepperBytes() {
            return Base64.getDecoder().decode(pepperB64);
        }
    }

    @Getter
    @Setter
    public static class LocalKek {
        private String keyB64;

        public byte[] getKeyBytes() {
            return Base64.getDecoder().decode(keyB64);
        }
    }
    @Getter
    @Setter
    public static class AwsKms {
        private String KeyId;
        private String secretAccessKey;

    }
}
