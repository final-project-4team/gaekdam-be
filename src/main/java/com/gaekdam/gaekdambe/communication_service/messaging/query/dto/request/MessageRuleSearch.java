package com.gaekdam.gaekdambe.communication_service.messaging.query.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MessageRuleSearch {

    private Long propertyCode;   // 로그인에서 주입
    private Long stageCode;      // 선택
    private Boolean isEnabled;   // 선택
}
