package com.gaekdam.gaekdambe.communication_service.messaging.demo;

import com.solapi.sdk.message.exception.SolapiEmptyResponseException;
import com.solapi.sdk.message.exception.SolapiMessageNotReceivedException;
import com.solapi.sdk.message.exception.SolapiUnknownException;
import com.solapi.sdk.message.model.Message;
import com.solapi.sdk.message.service.DefaultMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CoolSmsSmokeTestService {

    private final DefaultMessageService messageService;

    public void send(String to) {
        Message message = new Message();
        message.setFrom("01082802984");
        message.setTo(to);
        message.setText("[객담] CoolSMS 연동 테스트 문자입니다.");

        try {
            messageService.send(message);
        } catch (SolapiMessageNotReceivedException e) {
            throw new RuntimeException(e);
        } catch (SolapiEmptyResponseException e) {
            throw new RuntimeException(e);
        } catch (SolapiUnknownException e) {
            throw new RuntimeException(e);
        }
    }
}
