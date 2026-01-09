package com.gaekdam.gaekdambe.dummy.generate.reservation_service.stay;

import com.gaekdam.gaekdambe.reservation_service.reservation.command.domain.entity.Reservation;
import com.gaekdam.gaekdambe.reservation_service.reservation.command.infrastructure.repository.ReservationRepository;
import com.gaekdam.gaekdambe.reservation_service.stay.command.domain.entity.Stay;
import com.gaekdam.gaekdambe.reservation_service.stay.command.infrastructure.repository.StayRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Component;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

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

        Random random = new Random();

        // RESERVED 상태 예약만 투숙 생성
        List<Reservation> reservations =
                reservationRepository.findByReservationStatus("RESERVED");

        int count = 0;

        for (Reservation reservation : reservations) {
            if (count >= 8_000) break;

            // 실제 체크인 시간 (예약 체크인 날짜, 14~16시)
            LocalDateTime checkinAt =
                    reservation.getCheckinDate().atTime(14 + random.nextInt(3), 0);

            boolean completed = random.nextBoolean();

            // 실제 체크아웃 시간은 예약 checkout_date를 넘어가지 않음
            LocalDateTime checkoutAt =
                    completed
                            ? reservation.getCheckoutDate().atTime(10 + random.nextInt(2), 0)
                            : null;

            String stayStatus = completed ? "COMPLETED" : "STAYING";

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
