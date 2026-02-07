package com.gaekdam.gaekdambe.communication_service.messaging.demo;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DemoSmsRequest {


    private Long reservationCode;
    private Long stageCode;
    private Long senderPhoneCode;
    private String toPhone;
}
