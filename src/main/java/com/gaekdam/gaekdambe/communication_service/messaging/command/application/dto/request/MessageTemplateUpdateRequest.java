package com.gaekdam.gaekdambe.communication_service.messaging.command.application.dto.request;

import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.enums.LanguageCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MessageTemplateUpdateRequest {
    private LanguageCode languageCode;
    private String title;
    private String content;
    private String conditionExpr;
    private Boolean isActive;
}
