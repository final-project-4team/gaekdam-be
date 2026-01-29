package com.gaekdam.gaekdambe.dummy.generate.communication_service.messaging;

import java.time.LocalDateTime;
import java.util.List;

import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.entity.MessageJourneyStage;
import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.entity.MessageRule;
import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.entity.MessageTemplate;
import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.enums.MessageChannel;
import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.enums.ReferenceEntityType;
import com.gaekdam.gaekdambe.communication_service.messaging.command.infrastructure.repository.MessageJourneyStageRepository;
import com.gaekdam.gaekdambe.communication_service.messaging.command.infrastructure.repository.MessageRuleRepository;
import com.gaekdam.gaekdambe.communication_service.messaging.command.infrastructure.repository.MessageTemplateRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 템플릿 기반으로 룰 더미를 생성한다 (templateCode NULL 방어로 데이터 무결성 보장).
 */
@Component
@Transactional
public class DummyMessageRuleSetupTest {

    @Autowired
    private MessageRuleRepository ruleRepository;

    @Autowired
    private MessageJourneyStageRepository stageRepository;

    @Autowired
    private MessageTemplateRepository templateRepository;

    public void generate() {

        if (ruleRepository.count() > 0) return;

        LocalDateTime now = LocalDateTime.now();

        // 템플릿이 먼저 생성되어 있어야 하며, IDENTITY PK(templateCode) 확정이 필요
        templateRepository.flush();

        List<MessageTemplate> templates = templateRepository.findAll();

        for (MessageTemplate template : templates) {

            if (template.getTemplateCode() == null) {
                throw new IllegalStateException(
                        "TemplateCode not generated (IDENTITY flush missing). stageCode=" + template.getStageCode()
                );
            }

            MessageJourneyStage stage =
                    stageRepository.findById(template.getStageCode())
                            .orElseThrow();

            ReferenceEntityType refType =
                    switch (stage.getStageNameEng()) {
                        case "CHECKIN_CONFIRMED",
                             "CHECKOUT_PLANNED",
                             "CHECKOUT_CONFIRMED" -> ReferenceEntityType.STAY;
                        default -> ReferenceEntityType.RESERVATION;
                    };

            MessageRule rule = MessageRule.builder()
                    .hotelGroupCode(template.getHotelGroupCode())
                    .stageCode(template.getStageCode())
                    .templateCode(template.getTemplateCode())
                    .referenceEntityType(refType)
                    .visitorType(template.getVisitorType())
                    .channel(MessageChannel.SMS)
                    .offsetMinutes(0)
                    .priority(1)
                    .isEnabled(true)
                    .description("자동 발송 룰 - " + stage.getStageNameKor())
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            ruleRepository.save(rule);
        }
    }
}
