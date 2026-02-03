package com.gaekdam.gaekdambe.communication_service.messaging.worker;

import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.entity.MessageSendHistory;
import org.springframework.stereotype.Component;

@Component("SMS")
public class SmsMessageSender implements MessageSender {

    @Override
    public String send(MessageSendHistory history) {
        return "SMS-" + history.getSendCode();
    }
}