package com.gaekdam.gaekdambe.dummy.generate.communication_service.messaging;

import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.entity.MessageJourneyStage;
import com.gaekdam.gaekdambe.communication_service.messaging.command.infrastructure.repository.MessageJourneyStageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import jakarta.transaction.Transactional;

@Component
@Transactional
public class DummyMessageJourneyStageSetupTest {

    @Autowired
    private MessageJourneyStageRepository repository;

    public void generate() {

        // 이미 데이터 있으면 스킵
        if (repository.count() > 0) {
            return;
        }

        repository.save(
                MessageJourneyStage.builder()
                        .stageNameEng("RESERVATION_CONFIRMED")
                        .stageNameKor("예약 확정")
                        .isActive(true)
                        .build()
        );

        repository.save(
                MessageJourneyStage.builder()
                        .stageNameEng("CHECKIN_PLANNED")
                        .stageNameKor("체크인 예정")
                        .isActive(true)
                        .build()
        );

        repository.save(
                MessageJourneyStage.builder()
                        .stageNameEng("CHECKIN_CONFIRMED")
                        .stageNameKor("체크인 확정")
                        .isActive(true)
                        .build()
        );

        repository.save(
                MessageJourneyStage.builder()
                        .stageNameEng("CHECKOUT_PLANNED")
                        .stageNameKor("체크아웃 예정")
                        .isActive(true)
                        .build()
        );

        repository.save(
                MessageJourneyStage.builder()
                        .stageNameEng("CHECKOUT_CONFIRMED")
                        .stageNameKor("체크아웃 확정")
                        .isActive(true)
                        .build()
        );

        repository.save(
                MessageJourneyStage.builder()
                        .stageNameEng("RESERVATION_CANCELLED")
                        .stageNameKor("예약 취소")
                        .isActive(true)
                        .build()
        );

        repository.save(
                MessageJourneyStage.builder()
                        .stageNameEng("RESERVATION_UPDATED")
                        .stageNameKor("예약 변경")
                        .isActive(true)
                        .build()
        );

        repository.save(
                MessageJourneyStage.builder()
                        .stageNameEng("NOSHOW_CONFIRMED")
                        .stageNameKor("노쇼 확정")
                        .isActive(true)
                        .build()
        );
    }
}
