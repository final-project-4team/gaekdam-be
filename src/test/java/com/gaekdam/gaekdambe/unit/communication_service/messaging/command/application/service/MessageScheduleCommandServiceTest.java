package com.gaekdam.gaekdambe.unit.communication_service.messaging.command.application.service;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import com.gaekdam.gaekdambe.communication_service.messaging.command.application.service.MessageScheduleCommandService;
import com.gaekdam.gaekdambe.communication_service.messaging.command.application.service.HistorySaveService;
import com.gaekdam.gaekdambe.communication_service.messaging.query.mapper.MessageRuleQueryMapper;
import com.gaekdam.gaekdambe.communication_service.messaging.query.mapper.MessagingVisitorTypeQueryMapper;
import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.entity.MessageRule;
import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.enums.VisitorType;
import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.event.MessageJourneyEvent;
import com.gaekdam.gaekdambe.reservation_service.stay.command.domain.entity.Stay;
import com.gaekdam.gaekdambe.reservation_service.stay.command.infrastructure.repository.StayRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

class MessageScheduleCommandServiceTest {

    @Mock
    MessageRuleQueryMapper ruleQueryMapper;
    @Mock
    HistorySaveService historySaveService;
    @Mock
    StayRepository stayRepository;
    @Mock
    MessagingVisitorTypeQueryMapper visitorTypeQueryMapper;
    @Mock
    JdbcTemplate jdbcTemplate;

    private MessageScheduleCommandService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new MessageScheduleCommandService(ruleQueryMapper, historySaveService, stayRepository, visitorTypeQueryMapper, jdbcTemplate);
    }

    @Test
    void schedule_resolvesStayReservation_and_savesHistory() {
        // stay 기반 이벤트일 때 reservationCode 보정 및 룰 조회 흐름 검증
        Long stayCode = 100L;
        Long stayReservationCode = 77L;
        Stay stay = Stay.builder().stayCode(stayCode).reservationCode(stayReservationCode).build();

        when(stayRepository.findById(stayCode)).thenReturn(Optional.of(stay));

        // jdbcTemplate에서 hotelGroupCode 조회를 모킹
        when(jdbcTemplate.queryForObject(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.eq(Long.class), org.mockito.ArgumentMatchers.eq(stayReservationCode)))
                .thenReturn(11L);

        when(visitorTypeQueryMapper.resolveVisitorType(stayReservationCode)).thenReturn(VisitorType.FIRST);

        MessageRule rule = MessageRule.builder().ruleCode(1L).templateCode(2L).hotelGroupCode(11L).stageCode(5L).channel(com.gaekdam.gaekdambe.communication_service.messaging.command.domain.enums.MessageChannel.SMS).build();
        List<MessageRule> rules = Collections.singletonList(rule);

        when(ruleQueryMapper.findActiveRulesForSchedule(11L, 5L, VisitorType.FIRST)).thenReturn(rules);

        MessageJourneyEvent event = new MessageJourneyEvent(5L, null, stayCode);

        service.schedule(event);

        verify(historySaveService).saveHistory(event, rule);
    }

    @Test
    void schedule_withReservationCode_directly_savesHistory() {
        // reservationCode가 바로 주어졌을 때 경로 검증
        Long reservationCode = 55L;

        when(jdbcTemplate.queryForObject(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.eq(Long.class), org.mockito.ArgumentMatchers.eq(reservationCode)))
                .thenReturn(22L);

        when(visitorTypeQueryMapper.resolveVisitorType(reservationCode)).thenReturn(VisitorType.REPEAT);

        MessageRule rule = MessageRule.builder().ruleCode(3L).templateCode(4L).hotelGroupCode(22L).stageCode(8L).channel(com.gaekdam.gaekdambe.communication_service.messaging.command.domain.enums.MessageChannel.EMAIL).build();
        List<MessageRule> rules = Collections.singletonList(rule);

        when(ruleQueryMapper.findActiveRulesForSchedule(22L, 8L, VisitorType.REPEAT)).thenReturn(rules);

        MessageJourneyEvent event = new MessageJourneyEvent(8L, reservationCode, null);

        service.schedule(event);

        verify(historySaveService).saveHistory(event, rule);
    }
}
