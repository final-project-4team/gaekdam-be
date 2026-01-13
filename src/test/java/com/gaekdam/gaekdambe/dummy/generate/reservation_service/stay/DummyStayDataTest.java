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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Component
public class DummyStayDataTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private StayRepository stayRepository;

    @Transactional
    public void generate() {

        if (stayRepository.count() > 0) {
            return;
        }

        Random random = new Random();

        // RESERVED 상태 예약만 투숙 생성
        List<Reservation> reservations =
                reservationRepository.findByReservationStatus(ReservationStatus.RESERVED);

        int count = 0;

        for (Reservation reservation : reservations) {
            if (count >= 8_000) break;

            // 실제 체크인 시간 (14~16시)
            LocalDateTime checkinAt =
                    reservation.getCheckinDate().atTime(14 + random.nextInt(3), 0);

            boolean completed = random.nextBoolean();

            LocalDateTime checkoutAt =
                    completed
                            ? reservation.getCheckoutDate().atTime(10 + random.nextInt(2), 0)
                            : null;

            StayStatus stayStatus =
                    completed ? StayStatus.COMPLETED : StayStatus.STAYING;

            Stay stay = Stay.createStay(
                    reservation.getReservationCode(),
                    reservation.getRoomCode(),
                    reservation.getCustomerCode(),
                    reservation.getGuestCount(),
                    checkinAt,
                    checkoutAt,
                    stayStatus
            );

            stayRepository.save(stay);
            count++;
        }
    }
}
