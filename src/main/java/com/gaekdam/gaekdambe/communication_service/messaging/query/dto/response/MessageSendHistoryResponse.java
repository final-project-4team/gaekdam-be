package com.gaekdam.gaekdambe.communication_service.messaging.query.dto.response;

import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.enums.MessageSendStatus;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class MessageSendHistoryResponse {

    private Long sendCode;

    private Long stageCode;
    private String stageNameKor;

    private Long templateCode;
    private String templateTitle;

    private Long reservationCode;
    private Long stayCode;

    private MessageSendStatus status;

    private LocalDateTime scheduledAt;
    private LocalDateTime sentAt;

    private String failReason;
    private String externalMessageId;
}
