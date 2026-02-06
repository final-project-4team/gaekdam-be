package com.gaekdam.gaekdambe.communication_service.messaging.command.domain.resolver;

import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.entity.MessageJourneyStage;
import com.gaekdam.gaekdambe.communication_service.messaging.command.infrastructure.repository.MessageJourneyStageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MessageStageResolver {

    private final MessageJourneyStageRepository stageRepository;

    /**
     * stageNameEng → DB stage_code 조회
     * 하드코딩 금지
     */
    public Long resolveStageCode(String stageNameEng) {

        MessageJourneyStage stage =
                stageRepository.findByStageNameEng(stageNameEng)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "MessageJourneyStage not found: " + stageNameEng
                                )
                        );

        return stage.getStageCode();
    }
}
