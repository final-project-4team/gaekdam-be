package com.gaekdam.gaekdambe.communication_service.messaging.query.dto.response;

import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.enums.MessageSendStatus;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class MessageSendHistoryResponse {

    private Long sendCode;

    private Long stageCode;
    private Long reservationCode;
    private Long stayCode;

    private Long ruleCode;
    private Long templateCode;

    private LocalDateTime scheduledAt;
    private LocalDateTime sentAt;

    private MessageSendStatus status;
    private String failReason;
    private String externalMessageId;
}
