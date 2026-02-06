package com.gaekdam.gaekdambe.communication_service.messaging.demo;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DemoSmsRequest {

    private Long reservationCode;
    private Long stageCode;        // 히스토리용
    private Long templateCode;     // 실제 발송용
    private String toPhone;
}
