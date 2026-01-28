package com.gaekdam.gaekdambe.communication_service.messaging.query.dto.request;

import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.enums.MessageSendStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class MessageSendHistorySearchRequest {

    private Long hotelGroupCode; //  필수
    private Long propertyCode;   // 필터용

    private Long stageCode;
    private Long reservationCode;
    private Long stayCode;
    private MessageSendStatus status;

    private LocalDateTime fromDate;
    private LocalDateTime toDate;
}
