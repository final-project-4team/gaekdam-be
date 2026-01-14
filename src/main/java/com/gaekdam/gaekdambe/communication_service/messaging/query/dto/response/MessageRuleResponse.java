package com.gaekdam.gaekdambe.communication_service.messaging.query.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MessageRuleResponse {

    private Long ruleCode;
    private Long stageCode;
    private Long templateCode;
    private String referenceEntityType;
    private String channel;
    private boolean isEnabled;
    private int priority;
}
