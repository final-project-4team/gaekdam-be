package com.gaekdam.gaekdambe.dummy.generate.communication_service.messaging;

import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.entity.MessageRule;
import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.entity.MessageSendHistory;
import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.enums.MessageSendStatus;
import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.enums.ReferenceEntityType;
import com.gaekdam.gaekdambe.communication_service.messaging.command.infrastructure.repository.MessageRuleRepository;
import com.gaekdam.gaekdambe.communication_service.messaging.command.infrastructure.repository.MessageSendHistoryRepository;
import com.gaekdam.gaekdambe.reservation_service.reservation.command.domain.entity.Reservation;
import com.gaekdam.gaekdambe.reservation_service.reservation.command.infrastructure.repository.ReservationRepository;
import com.gaekdam.gaekdambe.reservation_service.stay.command.domain.entity.Stay;
import com.gaekdam.gaekdambe.reservation_service.stay.command.infrastructure.repository.StayRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


@Component
public class DummyMessageSendHistoryDataTest {

    private static final int BATCH = 500;

    @Autowired MessageSendHistoryRepository historyRepository;
    @Autowired MessageRuleRepository ruleRepository;
    @Autowired ReservationRepository reservationRepository;
    @Autowired StayRepository stayRepository;
    @Autowired
    EntityManager em;

    @Transactional
    public void generate() {

        if (historyRepository.count() > 0) return;

        List<MessageRule> rules = ruleRepository.findAll();

        List<MessageRule> reservationRules =
                rules.stream()
                        .filter(r -> r.getReferenceEntityType() == ReferenceEntityType.RESERVATION)
                        .toList();

        List<MessageRule> stayRules =
                rules.stream()
                        .filter(r -> r.getReferenceEntityType() == ReferenceEntityType.STAY)
                        .toList();

        List<MessageSendHistory> buffer = new ArrayList<>(BATCH);

        /* =========================
           예약 기반 (샘플링!)
           ========================= */
        for (Reservation reservation :
                reservationRepository.findAll().stream().limit(10_000).toList()) {

            for (MessageRule rule : reservationRules) {

                buffer.add(
                        MessageSendHistory.builder()
                                .stageCode(rule.getStageCode())
                                .reservationCode(reservation.getReservationCode())
                                .stayCode(null)
                                .ruleCode(rule.getRuleCode())
                                .templateCode(rule.getTemplateCode())
                                .channel(rule.getChannel())
                                .scheduledAt(reservation.getReservedAt())
                                .status(MessageSendStatus.SCHEDULED)
                                .build()
                );

                if (buffer.size() >= BATCH) {
                    historyRepository.saveAll(buffer);
                    em.flush();
                    em.clear();
                    buffer.clear();
                }
            }
        }

        /* =========================
           투숙 기반
           ========================= */
        for (Stay stay :
                stayRepository.findAll().stream().limit(10_000).toList()) {

            if (stay.getActualCheckinAt() == null) continue;

            for (MessageRule rule : stayRules) {

                buffer.add(
                        MessageSendHistory.builder()
                                .stageCode(rule.getStageCode())
                                .reservationCode(stay.getReservationCode())
                                .stayCode(stay.getStayCode())
                                .ruleCode(rule.getRuleCode())
                                .templateCode(rule.getTemplateCode())
                                .channel(rule.getChannel())
                                .scheduledAt(stay.getActualCheckinAt())
                                .sentAt(stay.getActualCheckinAt().plusMinutes(1))
                                .status(MessageSendStatus.SENT)
                                .externalMessageId("MSG-DUMMY")
                                .build()
                );

                if (buffer.size() >= BATCH) {
                    historyRepository.saveAll(buffer);
                    em.flush();
                    em.clear();
                    buffer.clear();
                }
            }
        }

        if (!buffer.isEmpty()) {
            historyRepository.saveAll(buffer);
            em.flush();
            em.clear();
        }
    }
}
