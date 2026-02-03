package com.gaekdam.gaekdambe.communication_service.messaging.bootstrap;

import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.entity.MessageJourneyStage;
import com.gaekdam.gaekdambe.communication_service.messaging.command.infrastructure.repository.MessageJourneyStageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class MessageJourneyStageSeed {

    private final MessageJourneyStageRepository repository;

    @Transactional
    public void seed() {

        seedIfNotExists("RESERVATION_CONFIRMED", "예약 확정");
        seedIfNotExists("RESERVATION_CANCELLED", "예약 취소");
        seedIfNotExists("NOSHOW_CONFIRMED", "노쇼 확정");
        seedIfNotExists("CHECKIN_PLANNED", "체크인 예정");
        seedIfNotExists("CHECKIN_CONFIRMED", "체크인 확정");
        seedIfNotExists("CHECKOUT_PLANNED", "체크아웃 예정");
        seedIfNotExists("CHECKOUT_CONFIRMED", "체크아웃 확정");
    }

    private void seedIfNotExists(String eng, String kor) {
        if (repository.existsByStageNameEng(eng)) return;

        repository.save(
                MessageJourneyStage.builder()
                        .stageNameEng(eng)
                        .stageNameKor(kor)
                        .isActive(true)
                        .build()
        );
    }
}