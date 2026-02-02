package com.gaekdam.gaekdambe.unit.communication_service.messaging.command.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import org.mockito.MockitoAnnotations;

import com.gaekdam.gaekdambe.communication_service.messaging.command.application.service.ConditionExprEvaluator;
import com.gaekdam.gaekdambe.communication_service.messaging.command.application.service.HistorySaveService;
import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.entity.MessageRule;
import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.event.MessageJourneyEvent;
import com.gaekdam.gaekdambe.communication_service.messaging.command.infrastructure.repository.MessageJourneyStageRepository;
import com.gaekdam.gaekdambe.communication_service.messaging.command.infrastructure.repository.MessageSendHistoryRepository;
import com.gaekdam.gaekdambe.communication_service.messaging.command.infrastructure.repository.MessageTemplateRepository;
import com.gaekdam.gaekdambe.communication_service.messaging.query.mapper.MessagingConditionContextQueryMapper;
import com.gaekdam.gaekdambe.reservation_service.stay.command.infrastructure.repository.StayRepository;

class HistorySaveServiceTest {

    @Mock
    MessageSendHistoryRepository historyRepository;
    @Mock
    StayRepository stayRepository;
    @Mock
    MessageTemplateRepository templateRepository;
    @Mock
    MessagingConditionContextQueryMapper contextQueryMapper;
    @Mock
    ConditionExprEvaluator conditionExprEvaluator;
    @Mock
    MessageJourneyStageRepository stageRepository;

    private HistorySaveService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new HistorySaveService(historyRepository, stayRepository, templateRepository, contextQueryMapper, conditionExprEvaluator, stageRepository);
    }

    @Test
    void saveHistory_skipsWhenTemplateMissing() {
        MessageRule rule = MessageRule.builder().ruleCode(1L).templateCode(null).build();
        // MessageJourneyEvent(stageCode, reservationCode, stayCode)
        MessageJourneyEvent event = new MessageJourneyEvent(20L, 10L, null);

        service.saveHistory(event, rule);

        verify(historyRepository).save(org.mockito.ArgumentMatchers.any());
    }
}
