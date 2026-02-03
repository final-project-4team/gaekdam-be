package com.gaekdam.gaekdambe.communication_service.messaging.worker;

import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.entity.MessageSendHistory;
import org.springframework.stereotype.Component;

@Component("KAKAO")
public class KakaoMessageSender implements MessageSender {

    @Override
    public String send(MessageSendHistory history) {
        // 실제 카카오 발송 로직 위치
        return "KAKAO-" + history.getSendCode();
    }
}