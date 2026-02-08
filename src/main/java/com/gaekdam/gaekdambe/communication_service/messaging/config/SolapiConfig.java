package com.gaekdam.gaekdambe.communication_service.messaging.config;

import com.solapi.sdk.SolapiClient;
import com.solapi.sdk.message.service.DefaultMessageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SolapiConfig {

    @Bean
    @ConditionalOnProperty(name = "messaging.sms.enabled", havingValue = "true")
    public DefaultMessageService solapiMessageService(
            @Value("${SOLAPI_API_KEY}") String apiKey,
            @Value("${SOLAPI_API_SECRET}") String apiSecret
    ) {
        return SolapiClient.INSTANCE.createInstance(apiKey, apiSecret);
    }
}

