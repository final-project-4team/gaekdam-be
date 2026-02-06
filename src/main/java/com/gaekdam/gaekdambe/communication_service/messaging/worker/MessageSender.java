package com.gaekdam.gaekdambe.communication_service.messaging.worker;


import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.entity.MessageSendHistory;

public interface MessageSender {
    String send(MessageSendHistory history);
}