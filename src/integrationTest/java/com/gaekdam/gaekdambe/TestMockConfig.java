package com.gaekdam.gaekdambe;

import com.gaekdam.gaekdambe.global.smtp.MailSendService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class TestMockConfig {

    @Bean
    @Primary
    public MailSendService mailSendService() {
        return Mockito.mock(MailSendService.class);
    }
}

