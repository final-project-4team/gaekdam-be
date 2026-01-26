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
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Component
@Transactional
public class DummyMessageSendHistoryDataTest {

    @Autowired
    private MessageSendHistoryRepository historyRepository;
    @Autowired
    private MessageRuleRepository ruleRepository;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private StayRepository stayRepository;

    public void generate() {

        if (historyRepository.count() > 0) return;

        List<MessageRule> rules = ruleRepository.findAll();
        List<Reservation> reservations = reservationRepository.findAll();
        List<Stay> stays = stayRepository.findAll();

        Random random = new Random();

        /* =========================
           예약 기반 메시지
           ========================= */
        for (Reservation reservation : reservations.subList(0, Math.min(1000, reservations.size()))) {

            MessageRule rule = rules.get(random.nextInt(rules.size()));
            if (rule.getReferenceEntityType() != ReferenceEntityType.RESERVATION) continue;

            LocalDateTime scheduledAt =
                    reservation.getReservedAt().plusMinutes(rule.getOffsetMinutes());

            historyRepository.save(
                    MessageSendHistory.builder()
                            .stageCode(rule.getStageCode())
                            .reservationCode(reservation.getReservationCode())
                            .stayCode(null)
                            .ruleCode(rule.getRuleCode())
                            .templateCode(rule.getTemplateCode())
                            .channel(rule.getChannel())          // 중요
                            .scheduledAt(scheduledAt)
                            .sentAt(scheduledAt.plusSeconds(5))
                            .status(MessageSendStatus.SENT)
                            .externalMessageId("MSG-" + UUID.randomUUID())
                            .build()
            );
        }

        /* =========================
           투숙 기반 메시지
           ========================= */
        for (Stay stay : stays.subList(0, Math.min(1000, stays.size()))) {

            if (stay.getActualCheckinAt() == null) continue;

            MessageRule rule = rules.get(random.nextInt(rules.size()));
            if (rule.getReferenceEntityType() != ReferenceEntityType.STAY) continue;

            LocalDateTime scheduledAt =
                    stay.getActualCheckinAt().plusMinutes(rule.getOffsetMinutes());

            historyRepository.save(
                    MessageSendHistory.builder()
                            .stageCode(rule.getStageCode())
                            .reservationCode(stay.getReservationCode())
                            .stayCode(stay.getStayCode())
                            .ruleCode(rule.getRuleCode())
                            .templateCode(rule.getTemplateCode())
                            .channel(rule.getChannel())          // 중요
                            .scheduledAt(scheduledAt)
                            .sentAt(scheduledAt.plusMinutes(1))
                            .status(MessageSendStatus.SENT)
                            .externalMessageId("MSG-" + UUID.randomUUID())
                            .build()
            );
        }
    }
}
