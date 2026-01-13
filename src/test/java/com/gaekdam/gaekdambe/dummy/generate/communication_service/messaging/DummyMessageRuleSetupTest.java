package com.gaekdam.gaekdambe.dummy.generate.communication_service.messaging;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.entity.MessageJourneyStage;
import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.entity.MessageRule;
import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.entity.MessageTemplate;
import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.enums.MessageChannel;
import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.enums.ReferenceEntityType;
import com.gaekdam.gaekdambe.communication_service.messaging.command.infrastructure.repository.MessageJourneyStageRepository;
import com.gaekdam.gaekdambe.communication_service.messaging.command.infrastructure.repository.MessageRuleRepository;
import com.gaekdam.gaekdambe.communication_service.messaging.command.infrastructure.repository.MessageTemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import jakarta.transaction.Transactional;

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
        int priority = 1;

        List<MessageJourneyStage> stages =
                stageRepository.findAll();

        for (MessageJourneyStage stage : stages) {
            if (!stage.isActive()) continue;

            List<MessageTemplate> templates =
                    templateRepository.findByStageCode(stage.getStageCode());

            for (MessageTemplate template : templates) {

                MessageRule rule = MessageRule.builder()
                        .stageCode(stage.getStageCode())
                        .templateCode(template.getTemplateCode())
                        .referenceEntityType(
                                stage.getStageNameEng().contains("CHECKIN")
                                        ? ReferenceEntityType.STAY
                                        : ReferenceEntityType.RESERVATION
                        )
                        .offsetMinutes(0)
                        .visitorType(template.getVisitorType())
                        .channel(MessageChannel.SMS)
                        .isEnabled(true)
                        .priority(priority++)
                        .description(
                                "자동발송 룰 - " + stage.getStageNameKor()
                        )
                        .membershipGradeCode(
                                template.getMembershipGradeCode()
                        )
                        .createdAt(now)
                        .updatedAt(now)
                        .build();

                ruleRepository.save(rule);
            }
        }
    }
}
