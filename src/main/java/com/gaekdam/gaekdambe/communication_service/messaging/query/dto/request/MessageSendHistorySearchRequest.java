package com.gaekdam.gaekdambe.communication_service.messaging.query.dto.request;

import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.enums.MessageSendStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class MessageSendHistorySearchRequest {

    /** SaaS 기준 */
    private Long hotelGroupCode;

    /** 지점 필터 */
    private Long propertyCode;

    private Long stageCode;
    private Long reservationCode;
    private Long stayCode;

    private MessageSendStatus status;

    private LocalDateTime fromDate;
    private LocalDateTime toDate;
}
