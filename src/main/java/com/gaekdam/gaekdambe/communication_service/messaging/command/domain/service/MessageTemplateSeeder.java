package com.gaekdam.gaekdambe.communication_service.messaging.command.domain.service;

import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.entity.MessageJourneyStage;
import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.entity.MessageTemplate;
import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.enums.LanguageCode;
import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.enums.VisitorType;
import com.gaekdam.gaekdambe.communication_service.messaging.command.infrastructure.repository.MessageJourneyStageRepository;
import com.gaekdam.gaekdambe.communication_service.messaging.command.infrastructure.repository.MessageTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MessageTemplateSeeder {

    private final MessageJourneyStageRepository stageRepository;
    private final MessageTemplateRepository templateRepository;

    /**
     * 기본 메시지 템플릿 생성
     * - hotel_group + stage + visitor_type 단위
     * - 이미 존재하면 생성하지 않음 (idempotent)
     */
    @Transactional
    public void seed(Long hotelGroupCode) {

        LocalDateTime now = LocalDateTime.now();

        List<MessageJourneyStage> stages = stageRepository.findAll();

        for (MessageJourneyStage stage : stages) {
            seedOne(hotelGroupCode, stage, VisitorType.FIRST, now);
            seedOne(hotelGroupCode, stage, VisitorType.REPEAT, now);
        }
    }

    private void seedOne(
            Long hotelGroupCode,
            MessageJourneyStage stage,
            VisitorType visitorType,
            LocalDateTime now
    ) {

        boolean exists =
                templateRepository.existsByHotelGroupCodeAndStageCodeAndVisitorType(
                        hotelGroupCode,
                        stage.getStageCode(),
                        visitorType
                );

        if (exists) {
            return;
        }

        MessageTemplate template = MessageTemplate.builder()
                .hotelGroupCode(hotelGroupCode)
                .stageCode(stage.getStageCode())
                .visitorType(visitorType) //
                .languageCode(LanguageCode.KOR)
                .title(defaultTitle(stage, visitorType))
                .content(defaultContent(stage.getStageNameEng(), visitorType))
                .isActive(true)
                .createdAt(now)
                .updatedAt(now)
                .build();

        templateRepository.save(template);
    }

    private String defaultTitle(MessageJourneyStage stage, VisitorType visitorType) {
        return visitorType == VisitorType.FIRST
                ? stage.getStageNameKor() + " 안내 (첫 방문)"
                : stage.getStageNameKor() + " 안내";
    }

    /**
     * 실제 기본 메시지 문구 정의
     */
    private String defaultContent(String stage, VisitorType visitor) {

        boolean first = visitor == VisitorType.FIRST;

        return switch (stage) {

            // ======================
            // 예약
            // ======================
            case "RESERVATION_CONFIRMED" ->
                    first
                            ? "안녕하세요 😊\n첫 방문을 환영합니다.\n예약이 정상적으로 확정되었습니다."
                            : "다시 찾아주셔서 감사합니다 😊\n예약이 정상적으로 확정되었습니다.";

            case "RESERVATION_CANCELLED" ->
                    "예약이 취소 처리되었습니다.\n변경 사항이 있으시면 언제든지 문의해주세요.";

            case "NO_SHOW" ->
                    "예약하신 일정에 방문 이력이 확인되지 않았습니다.\n문의가 필요하시면 연락 부탁드립니다.";

            // ======================
            // 체크인
            // ======================
            case "CHECKIN_PLANNED" ->
                    first
                            ? "곧 첫 체크인 예정입니다!\n편안한 투숙을 준비하고 있습니다."
                            : "곧 체크인 예정입니다.\n다시 뵙게 되어 반갑습니다.";

            case "CHECKIN_CONFIRMED" ->
                    first
                            ? "체크인이 완료되었습니다 😊\n첫 투숙이 즐거운 시간이 되시길 바랍니다."
                            : "체크인이 완료되었습니다.\n편안한 시간 보내세요.";

            // ======================
            // 체크아웃
            // ======================
            case "CHECKOUT_PLANNED" ->
                    "오늘 체크아웃 예정입니다.\n이용 내역을 확인해 주세요.";

            case "CHECKOUT_CONFIRMED" ->
                    first
                            ? "첫 투숙이 마무리되었습니다.\n이용해 주셔서 감사합니다 😊"
                            : "이번 투숙도 함께해 주셔서 감사합니다.\n다시 뵙기를 기다리겠습니다.";

            default ->
                    "안내 메시지입니다.";
        };
    }
}