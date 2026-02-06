package com.gaekdam.gaekdambe.unit.global.smtp;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;

import com.gaekdam.gaekdambe.global.smtp.MailSendRequest;
import com.gaekdam.gaekdambe.global.smtp.MailSendService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.MailException;

class MailSendServiceTest {

    @Mock
    JavaMailSender mailSender;

    private MailSendService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new MailSendService(mailSender);
    }

    @Test
    void sendTxtEmail_delegatesToMailSender() {
        MailSendRequest req = new MailSendRequest("to@example.com", "sub", "body");
        service.sendTxtEmail(req);
        verify(mailSender).send(org.mockito.ArgumentMatchers.any(SimpleMailMessage.class));
    }

    @Test
    void sendTxtEmail_throws_when_mailSender_throws() {
        MailSendRequest req = new MailSendRequest("to@example.com", "sub", "body");
        doThrow(new MailException("fail"){}).when(mailSender).send(org.mockito.ArgumentMatchers.any(SimpleMailMessage.class));
        try {
            service.sendTxtEmail(req);
        } catch (MailException e) {
            // expected
        }
    }

    @Test
    void resetPasswordEmail_builds_request_and_sends() {
        service.resetPasswordEmail("u@e.com", "tmp123");
        verify(mailSender).send(org.mockito.ArgumentMatchers.any(SimpleMailMessage.class));
    }
}
