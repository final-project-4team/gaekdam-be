package com.gaekdam.gaekdambe.dummy.reservation_service.reservation;

import com.gaekdam.gaekdambe.reservation_service.reservation.command.domain.entity.Reservation;
import com.gaekdam.gaekdambe.reservation_service.reservation.command.infrastructure.repository.ReservationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Random;

@SpringBootTest
@Transactional
@Rollback(false)
public class DummyReservationDataTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @Test
    @DisplayName("예약 내역 1만건 생성")
    void createReservationDummy(){

        Random random = new Random();

        int totalCount = 10_000;

        for (int i = 1; i <= totalCount; i++) {

            long tenantCode = (i % 10) + 1; // 호텔 1~10
            long roomCode = random.nextInt(500) + 1; // 객실 1~500
            long customerCode = random.nextInt(5_000) + 1;

            boolean hasPackage = random.nextBoolean();

            BigDecimal roomPrice =
                    BigDecimal.valueOf(100_000 + random.nextInt(200_000));

            BigDecimal packagePrice =
                    hasPackage ? BigDecimal.valueOf(30_000 + random.nextInt(70_000)) : null;

            Long packageCode =
                    hasPackage ? (long) (random.nextInt(50) + 1) : null;

            LocalDate checkin = LocalDate.now().minusDays(random.nextInt(60));
            LocalDate checkout = checkin.plusDays(1 + random.nextInt(3));

            Reservation reservation = Reservation.create(
                    checkin,
                    checkout,
                    1 + random.nextInt(4),
                    random.nextBoolean() ? "INDIVIDUAL" : "FAMILY",
                    random.nextBoolean() ? "WEB" : "OTA",
                    roomPrice,
                    packagePrice,
                    tenantCode,
                    roomCode,
                    customerCode,
                    packageCode
            );

            reservationRepository.save(reservation);
        }

    }


}
