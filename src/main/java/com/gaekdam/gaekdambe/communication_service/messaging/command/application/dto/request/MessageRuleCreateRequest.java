package com.gaekdam.gaekdambe.communication_service.messaging.command.application.dto.request;

import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.enums.MessageChannel;
import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.enums.ReferenceEntityType;
import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.enums.VisitorType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MessageRuleCreateRequest {
    private ReferenceEntityType referenceEntityType; // RESERVATION, STAY
    private int offsetMinutes;
    private VisitorType visitorType; // nullable 가능
    private MessageChannel channel;
    private boolean isEnabled;
    private int priority;
    private String description;

    private Long hotelGroupCode;
    private Long stageCode;
    private Long templateCode;
    private Long membershipGradeCode;
}