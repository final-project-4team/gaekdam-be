package com.gaekdam.gaekdambe.dummy.generate.communication_service.messaging;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.entity.MessageJourneyStage;
import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.entity.MessageTemplate;
import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.enums.LanguageCode;
import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.enums.VisitorType;
import com.gaekdam.gaekdambe.communication_service.messaging.command.infrastructure.repository.MessageJourneyStageRepository;
import com.gaekdam.gaekdambe.communication_service.messaging.command.infrastructure.repository.MessageTemplateRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 메시지 템플릿 더미를 생성한다 (IDENTITY PK 생성 보장을 위해 batch 저장 후 flush/clear).
 */
@Component
@Transactional
public class DummyMessageTemplateSetupTest {

    private static final int BATCH = 500;

    @Autowired
    private MessageTemplateRepository repository;

    @Autowired
    private MessageJourneyStageRepository stageRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManager em;

    public void generate() {

        if (repository.count() > 0) return;

        LocalDateTime now = LocalDateTime.now();

        // 호텔 그룹 목록 조회
        List<Long> hotelGroupCodes =
                jdbcTemplate.queryForList(
                        "SELECT DISTINCT hotel_group_code FROM property",
                        Long.class
                );

        List<MessageJourneyStage> stages = stageRepository.findAll();

        List<MessageTemplate> buffer = new ArrayList<>(BATCH);

        for (Long hotelGroupCode : hotelGroupCodes) {

            for (MessageJourneyStage stage : stages) {
                if (!stage.isActive()) continue;

                for (VisitorType visitor : VisitorType.values()) {

                    MessageTemplate template = MessageTemplate.builder()
                            .hotelGroupCode(hotelGroupCode)
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
                            .createdAt(now)
                            .updatedAt(now)
                            .build();

                    buffer.add(template);

                    if (buffer.size() >= BATCH) {
                        flush(buffer);
                    }
                }
            }
        }

        if (!buffer.isEmpty()) flush(buffer);
    }

    private void flush(List<MessageTemplate> buffer) {
        repository.saveAll(buffer);

        // IDENTITY 전략은 flush 시점에 PK가 확정되므로 반드시 flush 필요
        repository.flush();
        em.clear();
        buffer.clear();
    }
}
