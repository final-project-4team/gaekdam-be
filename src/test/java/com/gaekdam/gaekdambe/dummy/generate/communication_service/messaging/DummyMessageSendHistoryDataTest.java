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

        /* =========================
           예약 기반 메시지 (SCHEDULED 위주)
           ========================= */
        for (Reservation reservation : reservations) {

            for (MessageRule rule : rules) {

                if (rule.getReferenceEntityType() != ReferenceEntityType.RESERVATION) continue;

                historyRepository.save(
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
            }
        }

        /* =========================
           투숙 기반 메시지 (이미 발송된 데이터)
           ========================= */
        for (Stay stay : stays) {

            if (stay.getActualCheckinAt() == null) continue;

            for (MessageRule rule : rules) {

                if (rule.getReferenceEntityType() != ReferenceEntityType.STAY) continue;

                historyRepository.save(
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
            }
        }
    }
}
