package com.gaekdam.gaekdambe.communication_service.messaging.command.application.service;

import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.entity.MessageRule;
import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.enums.VisitorType;
import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.event.MessageJourneyEvent;
import com.gaekdam.gaekdambe.communication_service.messaging.command.infrastructure.repository.MessageRuleRepository;
import com.gaekdam.gaekdambe.communication_service.messaging.query.mapper.MessagingVisitorTypeQueryMapper;
import com.gaekdam.gaekdambe.reservation_service.stay.command.domain.entity.Stay;
import com.gaekdam.gaekdambe.reservation_service.stay.command.infrastructure.repository.StayRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageScheduleCommandService {

    private final MessageRuleRepository ruleRepository;
    private final HistorySaveService historySaveService;

    private final StayRepository stayRepository;
    private final MessagingVisitorTypeQueryMapper visitorTypeQueryMapper;

    public void schedule(MessageJourneyEvent event) {

        Long reservationCode = event.getReservationCode();
        Long stayCode = event.getStayCode();

        // stay 기반 이벤트는 reservationCode 보정 (visitorType 판정 위해 필요)
        if (reservationCode == null && stayCode != null) {
            Stay stay = stayRepository.findById(stayCode).orElseThrow();
            reservationCode = stay.getReservationCode();
        }

        VisitorType visitorType = visitorTypeQueryMapper.resolveVisitorType(reservationCode);

        List<MessageRule> rules =
                ruleRepository.findAll().stream()
                        .filter(MessageRule::isEnabled)
                        .filter(r -> r.getStageCode().equals(event.getStageCode()))
                        .filter(r -> r.getVisitorType() == null || r.getVisitorType() == visitorType)
                        .toList();

        for (MessageRule rule : rules) {
            historySaveService.saveHistory(event, rule);
        }
    }
}
