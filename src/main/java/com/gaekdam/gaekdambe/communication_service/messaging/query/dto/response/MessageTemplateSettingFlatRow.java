package com.gaekdam.gaekdambe.communication_service.messaging.query.dto.response;

import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.enums.LanguageCode;
import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.enums.VisitorType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageTemplateSettingFlatRow {

    private Long stageCode;
    private String stageNameKor;

    private Long templateCode;
    private VisitorType visitorType;
    private LanguageCode languageCode;
    private String title;
    private Boolean isActive;
}

