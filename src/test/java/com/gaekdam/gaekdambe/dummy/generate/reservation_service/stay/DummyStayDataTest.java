package com.gaekdam.gaekdambe.dummy.generate.reservation_service.stay;

import com.gaekdam.gaekdambe.reservation_service.reservation.command.domain.entity.Reservation;
import com.gaekdam.gaekdambe.reservation_service.reservation.command.domain.enums.ReservationStatus;
import com.gaekdam.gaekdambe.reservation_service.reservation.command.infrastructure.repository.ReservationRepository;
import com.gaekdam.gaekdambe.reservation_service.stay.command.domain.entity.Stay;
import com.gaekdam.gaekdambe.reservation_service.stay.command.domain.enums.StayStatus;
import com.gaekdam.gaekdambe.reservation_service.stay.command.infrastructure.repository.StayRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Component
public class DummyStayDataTest {

    @Autowired
    ReservationRepository reservationRepository;
    @Autowired
    StayRepository stayRepository;

    @Transactional
    public void generate() {

        if (stayRepository.count() > 0) return;

        LocalDate today = LocalDate.now();
        Random random = new Random();

        List<Reservation> reservations =
                reservationRepository.findByReservationStatus(ReservationStatus.RESERVED);

        for (Reservation r : reservations) {

            if (r.getCustomerCode() == null) continue;

            // 날짜 이상
            if (!r.getCheckinDate().isBefore(r.getCheckoutDate())) continue;

            // 미래 예약
            if (r.getCheckinDate().isAfter(today)) continue;

            // 체크인 예정(오늘)은 아직 투숙 아님
            if (r.getCheckinDate().isEqual(today)) continue;

            // NO_SHOW
            if (r.getReservationStatus() == ReservationStatus.NO_SHOW) continue;

            /* =========================
               실제 투숙 생성
               ========================= */

            LocalDateTime checkinAt = r.getCheckinDate().atTime(15, 0);
            LocalDateTime checkoutAt = null;
            StayStatus stayStatus = StayStatus.STAYING;

            // 오늘 체크아웃 대상
            if (r.getCheckoutDate().isEqual(today)) {
                if (random.nextInt(100) < 30) {
                    checkoutAt = today.atTime(10, 0);
                    stayStatus = StayStatus.COMPLETED;
                }
            }

            // 과거 투숙
            if (r.getCheckoutDate().isBefore(today)) {
                checkoutAt = r.getCheckoutDate().atTime(10, 0);
                stayStatus = StayStatus.COMPLETED;
            }

            stayRepository.save(
                    Stay.createStay(
                            r.getReservationCode(),
                            r.getRoomCode(),
                            r.getCustomerCode(),
                            r.getGuestCount(),
                            checkinAt,
                            checkoutAt,
                            stayStatus
                    )
            );
        }
    }
}
