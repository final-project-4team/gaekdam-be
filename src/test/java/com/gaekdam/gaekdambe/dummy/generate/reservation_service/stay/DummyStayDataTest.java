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

    @Autowired ReservationRepository reservationRepository;
    @Autowired StayRepository stayRepository;

    @Transactional
    public void generate() {

        if (stayRepository.count() > 0) return;

        LocalDate today = LocalDate.now();

        List<Reservation> reservations =
                reservationRepository.findByReservationStatus(ReservationStatus.RESERVED);

        for (Reservation r : reservations) {

            // 미래 예약 → 투숙 없음
            if (r.getCheckinDate().isAfter(today)) continue;


            LocalDateTime checkinAt = r.getCheckinDate().atTime(15, 0);
            LocalDateTime checkoutAt = null;
            StayStatus stayStatus = StayStatus.STAYING;

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
