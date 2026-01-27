package com.gaekdam.gaekdambe.communication_service.messaging.query.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class MessageTemplateSettingResponse {

    private Long stageCode;
    private String stageNameKor;

    // FIRST / REPEAT → template
    private Map<String, MessageTemplateSettingItem> templates;
}
