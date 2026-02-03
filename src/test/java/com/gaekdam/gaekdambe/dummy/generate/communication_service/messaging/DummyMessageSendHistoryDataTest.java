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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 메시지 발송 이력 더미 데이터를 생성한다 (오늘 이전 + 허용 기간만).
 */
@Component
public class DummyMessageSendHistoryDataTest {

    private static final int BATCH = 500;

    // === 허용 기간 ===
    private static final LocalDateTime JAN_START =
            LocalDateTime.of(2026, 1, 1, 0, 0);
    private static final LocalDateTime JAN_END =
            LocalDateTime.of(2026, 1, 31, 23, 59, 59);

    private static final LocalDateTime DEC_START =
            LocalDateTime.of(2026, 12, 1, 0, 0);
    private static final LocalDateTime DEC_END =
            LocalDateTime.of(2026, 12, 31, 23, 59, 59);

    @Autowired MessageSendHistoryRepository historyRepository;
    @Autowired MessageRuleRepository ruleRepository;
    @Autowired ReservationRepository reservationRepository;
    @Autowired StayRepository stayRepository;
    @Autowired JdbcTemplate jdbcTemplate;
    @Autowired EntityManager em;

    @Transactional
    public void generate() {

        // 이미 생성된 경우 재생성하지 않음
        if (historyRepository.count() > 0) return;

        List<MessageSendHistory> buffer = new ArrayList<>(BATCH);

        /* =================================================
           1. 예약 기반 (RESERVATION)
           ================================================= */
        for (Reservation reservation : reservationRepository.findAll()) {

            LocalDateTime reservedAt = reservation.getReservedAt();
            if (!isValidDummyTime(reservedAt)) continue;

            Long hotelGroupCode =
                    jdbcTemplate.queryForObject(
                            "SELECT hotel_group_code FROM property WHERE property_code = ?",
                            Long.class,
                            reservation.getPropertyCode()
                    );

            List<MessageRule> rules =
                    ruleRepository.findByHotelGroupCode(hotelGroupCode)
                            .stream()
                            .filter(r -> r.getReferenceEntityType() == ReferenceEntityType.RESERVATION)
                            .toList();

            for (MessageRule rule : rules) {

                buffer.add(
                        MessageSendHistory.builder()
                                .stageCode(rule.getStageCode())
                                .reservationCode(reservation.getReservationCode())
                                .stayCode(null)
                                .ruleCode(rule.getRuleCode())
                                .templateCode(rule.getTemplateCode())
                                .channel(rule.getChannel())
                                .scheduledAt(reservedAt)
                                .status(MessageSendStatus.SCHEDULED)
                                .build()
                );

                if (buffer.size() >= BATCH) flush(buffer);
            }
        }

        /* =================================================
           2. 투숙 기반 (STAY)
           ================================================= */
        for (Stay stay : stayRepository.findAll()) {

            LocalDateTime checkinAt = stay.getActualCheckinAt();
            if (!isValidDummyTime(checkinAt)) continue;

            Long hotelGroupCode =
                    jdbcTemplate.queryForObject(
                            """
                            SELECT p.hotel_group_code
                            FROM reservation r
                            JOIN property p ON r.property_code = p.property_code
                            WHERE r.reservation_code = ?
                            """,
                            Long.class,
                            stay.getReservationCode()
                    );

            List<MessageRule> rules =
                    ruleRepository.findByHotelGroupCode(hotelGroupCode)
                            .stream()
                            .filter(r -> r.getReferenceEntityType() == ReferenceEntityType.STAY)
                            .toList();

            for (MessageRule rule : rules) {

                buffer.add(
                        MessageSendHistory.builder()
                                .stageCode(rule.getStageCode())
                                .reservationCode(stay.getReservationCode())
                                .stayCode(stay.getStayCode())
                                .ruleCode(rule.getRuleCode())
                                .templateCode(rule.getTemplateCode())
                                .channel(rule.getChannel())
                                .scheduledAt(checkinAt)
                                .sentAt(checkinAt.plusMinutes(1))
                                .status(MessageSendStatus.SENT)
                                .externalMessageId("MSG-DUMMY")
                                .build()
                );

                if (buffer.size() >= BATCH) flush(buffer);
            }
        }

        if (!buffer.isEmpty()) flush(buffer);
    }

    /**
     * 더미 데이터로 허용 가능한 시간인지 검증한다 (오늘 이전 + 허용 기간).
     */
    private boolean isValidDummyTime(LocalDateTime t) {
        if (t == null) return false;
        if (t.isAfter(LocalDateTime.now())) return false;

        return (t.isAfter(JAN_START) && t.isBefore(JAN_END))
                || (t.isAfter(DEC_START) && t.isBefore(DEC_END));
    }

    /**
     * 배치 단위로 저장 후 영속성 컨텍스트를 초기화한다.
     */
    private void flush(List<MessageSendHistory> buffer) {
        historyRepository.saveAll(buffer);
        em.flush();
        em.clear();
        buffer.clear();
    }
}
