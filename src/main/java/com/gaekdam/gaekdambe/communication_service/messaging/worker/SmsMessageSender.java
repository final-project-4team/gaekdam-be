package com.gaekdam.gaekdambe.communication_service.messaging.worker;

import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.entity.MessageSendHistory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component("SMS")
public class SmsMessageSender implements MessageSender {

    @Value("${messaging.sms.enabled:false}")
    private boolean smsEnabled;


    @Override
    public String send(MessageSendHistory history) {

        if (!smsEnabled) {
            log.info(
                    "[SMS BLOCKED] sendCode={}, stage={}, to={}, reason=sms disabled",
                    history.getSendCode(),
                    history.getStageCode(),
                    history.getToPhone()
            );
            // 실제 발송 안 함 + 정상 흐름 유지
            return "SMS-BLOCKED-" + history.getSendCode();
        }


        // 나중에 여기에 CoolSMS(SOLAPI) 실제 발송 로직이 들어간다
        // 지금은 절대 넣지 말 것

        return "SMS-SENT-" + history.getSendCode();
    }

    }