package com.gaekdam.gaekdambe.dummy.generate.reservation_service.reservation;

import com.gaekdam.gaekdambe.reservation_service.reservation.command.domain.entity.Reservation;
import com.gaekdam.gaekdambe.reservation_service.reservation.command.infrastructure.repository.ReservationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Component;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Random;

@Component
public class DummyReservationDataTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @Transactional
    public void generate(){

        if (reservationRepository.count() > 0) {
            return;
        }

        Random random = new Random();
        int totalCount = 10_000;

        for (int i = 1; i <= totalCount; i++) {

            // 상태 결정
            String reservationStatus;
            if (i <= 1_200) {
                reservationStatus = "NO_SHOW";
            } else if (i <= 2_000) {
                reservationStatus = "CANCELED";
            } else {
                reservationStatus = "RESERVED";
            }

            long tenantCode = (i % 10) + 1;
            long roomCode = random.nextInt(500) + 1;
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

            // 예약 생성
            Reservation reservation = Reservation.createReservation(
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
                    packageCode,
                    reservationStatus
            );

            // 취소 상태면 취소 시간 세팅
            if ("CANCELED".equals(reservationStatus)) {
                reservation = Reservation.builder()
                        .reservationCode(reservation.getReservationCode())
                        .reservationStatus(reservation.getReservationStatus())
                        .checkinDate(reservation.getCheckinDate())
                        .checkoutDate(reservation.getCheckoutDate())
                        .guestCount(reservation.getGuestCount())
                        .guestType(reservation.getGuestType())
                        .reservationChannel(reservation.getReservationChannel())
                        .reservationRoomPrice(reservation.getReservationRoomPrice())
                        .reservationPackagePrice(reservation.getReservationPackagePrice())
                        .totalPrice(reservation.getTotalPrice())
                        .reservedAt(reservation.getReservedAt())
                        .canceledAt(reservation.getReservedAt().plusHours(1 + random.nextInt(72)))
                        .createdAt(reservation.getCreatedAt())
                        .tenantCode(reservation.getTenantCode())
                        .roomCode(reservation.getRoomCode())
                        .customerCode(reservation.getCustomerCode())
                        .packageCode(reservation.getPackageCode())
                        .build();
            }

            reservationRepository.save(reservation);
        }

    }


}
