package com.gaekdam.gaekdambe.communication_service.messaging.demo;

import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.entity.MessageSendHistory;
import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.entity.MessageSenderPhone;
import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.entity.MessageTemplate;
import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.enums.MessageChannel;
import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.enums.MessageSendStatus;
import com.gaekdam.gaekdambe.communication_service.messaging.command.infrastructure.repository.MessageSendHistoryRepository;
import com.gaekdam.gaekdambe.communication_service.messaging.command.infrastructure.repository.MessageSenderPhoneRepository;
import com.gaekdam.gaekdambe.communication_service.messaging.command.infrastructure.repository.MessageTemplateRepository;
import com.solapi.sdk.message.model.Message;
import com.solapi.sdk.message.service.DefaultMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "messaging.sms.enabled", havingValue = "true")
@Transactional
public class DemoSmsService {

    // 실제 SOLAPI에 등록된 발신번호 (고정)
    private static final String REAL_FROM_PHONE = "01082802984";

    // DEMO 전용 ruleCode (중복 허용용)
    private static final Long DEMO_RULE_CODE = -1L;

    private final MessageTemplateRepository messageTemplateRepository;
    private final MessageSenderPhoneRepository senderPhoneRepository;
    private final MessageSendHistoryRepository historyRepository;
    private final DefaultMessageService messageService;

    public void sendOne(DemoSmsRequest request, Long hotelGroupCode) {

        /* ===============================
         * 0. 요청값 검증 (중요)
         * =============================== */
        if (request.getSenderPhoneCode() == null) {
            throw new IllegalArgumentException("발신번호(senderPhoneCode)는 필수입니다.");
        }

        if (request.getToPhone() == null || request.getToPhone().isBlank()) {
            throw new IllegalArgumentException("수신번호(toPhone)는 필수입니다.");
        }

        /* ===============================
         * 1. 템플릿 조회 (호텔그룹 + 여정)
         * =============================== */
        List<MessageTemplate> templates =
                messageTemplateRepository.findByHotelGroupCodeAndStageCode(
                        hotelGroupCode,
                        request.getStageCode()
                );

        if (templates.isEmpty()) {
            throw new IllegalArgumentException("해당 여정의 템플릿이 없습니다.");
        }

        // DEMO: 첫 번째 템플릿 사용
        MessageTemplate template = templates.get(0);

        /* ===============================
         * 2. 선택한 발신번호 조회
         * =============================== */
        MessageSenderPhone senderPhone =
                senderPhoneRepository.findById(request.getSenderPhoneCode())
                        .orElseThrow(() ->
                                new IllegalArgumentException("선택한 발신번호가 존재하지 않습니다.")
                        );

        // 호텔 그룹 검증 (보안)
        if (!senderPhone.getHotelGroupCode().equals(hotelGroupCode)) {
            throw new IllegalStateException("다른 호텔 그룹의 발신번호입니다.");
        }

        String selectedFromPhone = senderPhone.getPhoneNumber();

        /* ===============================
         * 3. 발송 히스토리 생성 (DEMO)
         * =============================== */
        MessageSendHistory history = MessageSendHistory.builder()
                .reservationCode(request.getReservationCode())
                .stageCode(request.getStageCode())
                .ruleCode(DEMO_RULE_CODE) // 중복 허용용
                .templateCode(template.getTemplateCode())
                .channel(MessageChannel.SMS)
                .status(MessageSendStatus.PROCESSING)
                .fromPhone(REAL_FROM_PHONE) // 실제 발송 번호
                .toPhone(request.getToPhone())
                .scheduledAt(LocalDateTime.now())
                .build();

        historyRepository.save(history);

        /* ===============================
         * 4. 문자 내용 구성
         * =============================== */
        String text =
                "[객담 DEMO 메시지]\n"
                        + "예약코드: " + request.getReservationCode() + "\n"
                        + "선택 발신번호: " + selectedFromPhone + "\n\n"
                        + template.getContent();

        /* ===============================
         * 5. 실제 발송
         * =============================== */
        Message message = new Message();
        message.setFrom(REAL_FROM_PHONE);
        message.setTo(request.getToPhone());
        message.setText(text);

        try {
            messageService.send(message);
            history.markSent("DEMO");
        } catch (Exception e) {
            log.warn("[DEMO SMS FAILED] reservation={}, stage={}",
                    request.getReservationCode(),
                    request.getStageCode(), e);
            history.markFailed("SOLAPI 발송 실패");
        }
    }
}
