package com.gaekdam.gaekdambe.dummy.generate.communication_service.messaging;

import java.time.LocalDateTime;
import java.util.List;

import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.entity.MessageJourneyStage;
import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.entity.MessageTemplate;
import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.enums.LanguageCode;
import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.enums.VisitorType;
import com.gaekdam.gaekdambe.communication_service.messaging.command.infrastructure.repository.MessageJourneyStageRepository;
import com.gaekdam.gaekdambe.communication_service.messaging.command.infrastructure.repository.MessageTemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import jakarta.transaction.Transactional;

@Component
@Transactional
public class DummyMessageTemplateSetupTest {

    @Autowired
    private MessageTemplateRepository repository;

    @Autowired
    private MessageJourneyStageRepository stageRepository;

    public void generate() {

        if (repository.count() > 0) return;

        LocalDateTime now = LocalDateTime.now();

        List<MessageJourneyStage> stages =
                stageRepository.findAll();

        for (MessageJourneyStage stage : stages) {
            if (!stage.isActive()) continue;

            for (VisitorType visitor : VisitorType.values()) {

                MessageTemplate template = MessageTemplate.builder()
                        .stageCode(stage.getStageCode())
                        .visitorType(visitor)
                        .languageCode(LanguageCode.KOR)
                        .title(stage.getStageNameKor() + " 안내 메시지")
                        .content(
                                visitor == VisitorType.FIRST
                                        ? "첫 방문 고객님을 위한 안내입니다."
                                        : "재방문 고객님을 위한 맞춤 안내입니다."
                        )
                        .conditionExpr(null)
                        .isActive(true)
                        .membershipGradeCode(
                                visitor == VisitorType.FIRST ? 1L : 2L
                        )
                        .propertyCode(1L) // 또는 랜덤
                        .createdAt(now)
                        .updatedAt(now)
                        .build();

                repository.save(template);
            }
        }
    }
}