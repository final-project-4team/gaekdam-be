package com.gaekdam.gaekdambe.communication_service.messaging.command.application.dto.request;

import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.enums.MessageChannel;
import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.enums.VisitorType;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MessageRuleUpdateRequest {
    private int offsetMinutes;
    private VisitorType visitorType; // nullable 가능
    private MessageChannel channel;
    private Boolean  isEnabled;
    private int priority;
    private String description;

    private Long templateCode;
}
