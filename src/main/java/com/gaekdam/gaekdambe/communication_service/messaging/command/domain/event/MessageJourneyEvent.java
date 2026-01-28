package com.gaekdam.gaekdambe.communication_service.messaging.command.domain.event;

import lombok.Getter;

@Getter
public class MessageJourneyEvent {

    private final Long stageCode;
    private final Long reservationCode;
    private final Long stayCode;

    /**
     * reservationCode 또는 stayCode 중 하나만 사용
     */
    public MessageJourneyEvent(
            Long stageCode,
            Long reservationCode,
            Long stayCode
    ) {
        this.stageCode = stageCode;
        this.reservationCode = reservationCode;
        this.stayCode = stayCode;
    }
}
