package com.gaekdam.gaekdambe.communication_service.messaging.command.application.dto.request;

import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.enums.LanguageCode;
import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.enums.VisitorType;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MessageTemplateCreateRequest {
    private VisitorType visitorType;
    private LanguageCode languageCode;
    private String title;
    private String content;
    private String conditionExpr;
    private boolean isActive;


    private Long stageCode;
}
