package com.gaekdam.gaekdambe.communication_service.messaging.command.domain.service;

import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.entity.MessageJourneyStage;
import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.entity.MessageRule;
import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.entity.MessageTemplate;
import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.enums.MessageChannel;
import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.enums.ReferenceEntityType;
import com.gaekdam.gaekdambe.communication_service.messaging.command.infrastructure.repository.MessageJourneyStageRepository;
import com.gaekdam.gaekdambe.communication_service.messaging.command.infrastructure.repository.MessageRuleRepository;
import com.gaekdam.gaekdambe.communication_service.messaging.command.infrastructure.repository.MessageTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MessageRuleSeeder {

    private final MessageTemplateRepository templateRepository;
    private final MessageRuleRepository ruleRepository;
    private final MessageJourneyStageRepository stageRepository;

    /**
     * 호텔그룹 단위 메시지 룰 기본값 시딩
     *
     * 정책 요약
     * - Template 1건당 Rule 1건
     * - visitorType은 Template 기준으로 고정 (FIRST / REPEAT)
     * - CHECKIN_PLANNED / CHECKOUT_PLANNED → 2시간 전 발송
     * - 나머지 stage → 즉시 발송
     * - 이미 존재하는 Rule은 생성하지 않음 (idempotent)
     */
    @Transactional
    public void seed(Long hotelGroupCode) {

        LocalDateTime now = LocalDateTime.now();

        //  stage 기준으로 loop
        List<MessageJourneyStage> stages = stageRepository.findAll();

        for (MessageJourneyStage stage : stages) {

            //  해당 stage + hotelGroup 템플릿만 조회
            List<MessageTemplate> templates =
                    templateRepository.findByHotelGroupCodeAndStageCode(
                            hotelGroupCode,
                            stage.getStageCode()
                    );

            for (MessageTemplate template : templates) {

                boolean exists =
                        ruleRepository.existsByHotelGroupCodeAndStageCodeAndTemplateCode(
                                hotelGroupCode,
                                stage.getStageCode(),
                                template.getTemplateCode()
                        );

                if (exists) continue;

                int offsetMinutes = resolveOffsetMinutes(stage.getStageCode());

                MessageRule rule = MessageRule.builder()
                        .hotelGroupCode(hotelGroupCode)
                        .stageCode(stage.getStageCode())
                        .templateCode(template.getTemplateCode())
                        .referenceEntityType(resolveReferenceEntityType(stage.getStageCode()))
                        .visitorType(template.getVisitorType())
                        .channel(MessageChannel.SMS)
                        .offsetMinutes(offsetMinutes)
                        .priority(1)
                        .isEnabled(true)
                        .createdAt(now)
                        .updatedAt(now)
                        .build();

                ruleRepository.save(rule);
            }
        }
    }

    /**
     * stage별 발송 시점 정책
     *
     * - CHECKIN_PLANNED  : 체크인 예정 2시간 전
     * - CHECKOUT_PLANNED : 체크아웃 예정 2시간 전
     * - 그 외            : 즉시 발송
     */
    private int resolveOffsetMinutes(Long stageCode) {

        MessageJourneyStage stage =
                stageRepository.findById(stageCode)
                        .orElseThrow(() ->
                                new IllegalStateException("MessageJourneyStage not found: " + stageCode)
                        );

        return switch (stage.getStageNameEng()) {
            case "CHECKIN_PLANNED", "CHECKOUT_PLANNED" -> -120;
            default -> 0;
        };
    }

    /**
     * stage별 기준 엔티티 결정
     *
     * - 예약 기준  : RESERVATION
     * - 투숙 기준  : STAY
     */
    private ReferenceEntityType resolveReferenceEntityType(Long stageCode) {

        MessageJourneyStage stage =
                stageRepository.findById(stageCode)
                        .orElseThrow(() ->
                                new IllegalStateException("MessageJourneyStage not found: " + stageCode)
                        );

        return switch (stage.getStageNameEng()) {

            // 예약 기준 메시지
            case "RESERVATION_CONFIRMED",
                 "RESERVATION_CANCELLED",
                 "NOSHOW_CONFIRMED",
                 "CHECKIN_PLANNED" -> ReferenceEntityType.RESERVATION;

            // 투숙 기준 메시지
            case "CHECKIN_CONFIRMED",
                 "CHECKOUT_PLANNED",
                 "CHECKOUT_CONFIRMED" -> ReferenceEntityType.STAY;

            default -> throw new IllegalStateException(
                    "Unknown stage for referenceEntityType mapping: " + stage.getStageNameEng()
            );
        };
    }
}