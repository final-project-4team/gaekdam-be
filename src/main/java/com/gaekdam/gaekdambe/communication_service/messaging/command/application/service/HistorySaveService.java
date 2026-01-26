package com.gaekdam.gaekdambe.communication_service.messaging.command.application.service;

import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.entity.MessageRule;
import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.entity.MessageSendHistory;
import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.enums.MessageSendStatus;
import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.event.MessageJourneyEvent;
import com.gaekdam.gaekdambe.communication_service.messaging.command.infrastructure.repository.MessageSendHistoryRepository;
import com.gaekdam.gaekdambe.reservation_service.stay.command.domain.entity.Stay;
import com.gaekdam.gaekdambe.reservation_service.stay.command.infrastructure.repository.StayRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class HistorySaveService {

    private final MessageSendHistoryRepository historyRepository;
    private final StayRepository stayRepository;

    /**
     * MessageSendHistory 단건 저장
     * 중복 발생 시 이 트랜잭션만 롤백
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveHistory(MessageJourneyEvent event, MessageRule rule) {

        Long reservationCode = event.getReservationCode();
        Long stayCode = event.getStayCode();

        if (reservationCode == null && stayCode != null) {
            Stay stay = stayRepository.findById(stayCode)
                    .orElseThrow();
            reservationCode = stay.getReservationCode();
        }

        MessageSendHistory history = MessageSendHistory.builder()
                .stageCode(event.getStageCode())
                .reservationCode(reservationCode)
                .stayCode(stayCode)
                .ruleCode(rule.getRuleCode())
                .templateCode(rule.getTemplateCode())
                .channel(rule.getChannel())
                .scheduledAt(
                        LocalDateTime.now().plusMinutes(rule.getOffsetMinutes())
                )
                .status(MessageSendStatus.SCHEDULED)
                .build();

        try {
            historyRepository.save(history);
        } catch (DataIntegrityViolationException e) {
            // 중복 → 정상 스킵
        }
    }
}
