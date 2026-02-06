package com.gaekdam.gaekdambe.communication_service.messaging.demo;

import com.gaekdam.gaekdambe.communication_service.messaging.command.application.service.MessageSenderPhoneService;
import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.entity.MessageSendHistory;
import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.entity.MessageSenderPhone;
import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.entity.MessageTemplate;
import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.enums.MessageChannel;
import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.enums.MessageSendStatus;
import com.gaekdam.gaekdambe.communication_service.messaging.command.infrastructure.repository.MessageSendHistoryRepository;
import com.gaekdam.gaekdambe.communication_service.messaging.command.infrastructure.repository.MessageTemplateRepository;
import com.solapi.sdk.message.model.Message;
import com.solapi.sdk.message.service.DefaultMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class DemoSmsService {

    private static final String REAL_FROM_PHONE = "01082802984";

    private final MessageSenderPhoneService senderPhoneService;
    private final MessageSendHistoryRepository historyRepository;
    private final DefaultMessageService messageService;

    // 이미 존재하는 JPA Repository
    private final MessageTemplateRepository messageTemplateRepository;

    public void sendOne(DemoSmsRequest request, Long hotelGroupCode) {

        /* =========================
           1. 템플릿 조회 (stageCode 기준)
           ========================= */
        MessageTemplate template =
                messageTemplateRepository
                        .findById(request.getTemplateCode())
                        .orElseThrow(() ->
                                new IllegalArgumentException("템플릿이 없습니다.")
                        );

        /* =========================
           2. 발신번호 처리
           ========================= */
        MessageSenderPhone sender =
                senderPhoneService.getActiveSender(hotelGroupCode);

        String selectedFromPhone = sender.getPhoneNumber();
        String realFromPhone = REAL_FROM_PHONE;

        /* =========================
           3. 히스토리 생성
           ========================= */
        MessageSendHistory history = MessageSendHistory.builder()
                .stageCode(request.getStageCode())
                .reservationCode(request.getReservationCode())
                .ruleCode(0L)
                .templateCode(template.getTemplateCode())
                .channel(MessageChannel.SMS)
                .scheduledAt(LocalDateTime.now())
                .status(MessageSendStatus.PROCESSING)
                .fromPhone(realFromPhone)
                .toPhone(request.getToPhone())
                .build();

        historyRepository.save(history);

        /* =========================
           4. 문자 내용
           ========================= */
        String text =
                template.getContent()
                        + "\n\n[시연]"
                        + "\n선택 발신번호: " + selectedFromPhone
                        + "\n예약코드: " + request.getReservationCode();

        Message message = new Message();
        message.setFrom(realFromPhone);
        message.setTo(request.getToPhone());
        message.setText(text);

        /* =========================
           5. 발송
           ========================= */
        try {
            messageService.send(message);
            history.markSent("DEMO");
        } catch (Exception e) {
            log.warn("[DEMO SMS FAILED] reservation={}, stage={}, reason={}",
                    request.getReservationCode(),
                    request.getStageCode(),
                    e.getMessage());

            history.markFailed("SOLAPI 접수 실패");
        }
    }
}
