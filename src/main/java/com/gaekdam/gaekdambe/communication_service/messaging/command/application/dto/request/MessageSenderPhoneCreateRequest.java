package com.gaekdam.gaekdambe.communication_service.messaging.command.application.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MessageSenderPhoneCreateRequest {
    private String phoneNumber;
    private String label;
}
