package com.gaekdam.gaekdambe.communication_service.messaging.query.dto.response;

import lombok.Getter;

@Getter
public class MessageJourneyStageResponse {

    private Long stageCode;
    private String stageNameKor;
    private String stageNameEng;
}
