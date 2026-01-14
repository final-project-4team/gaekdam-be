package com.gaekdam.gaekdambe.communication_service.messaging.query.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MessageTemplateResponse {

    private Long templateCode;
    private Long stageCode;
    private String visitorType;
    private String languageCode;
    private String title;
    private boolean isActive;
    private Long membershipGradeCode;
}
