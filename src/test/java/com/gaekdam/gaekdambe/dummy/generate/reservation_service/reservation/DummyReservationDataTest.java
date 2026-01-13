package com.gaekdam.gaekdambe.dummy.generate.reservation_service.reservation;

import com.gaekdam.gaekdambe.reservation_service.reservation.command.domain.entity.Reservation;
import com.gaekdam.gaekdambe.reservation_service.reservation.command.domain.enums.*;
import com.gaekdam.gaekdambe.reservation_service.reservation.command.infrastructure.repository.ReservationRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Random;

@Component
public class DummyReservationDataTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @Transactional
    public void generate() {

        if (reservationRepository.count() > 0) {
            return;
        }

        Random random = new Random();
        int totalCount = 10_000;

        for (int i = 1; i <= totalCount; i++) {

            ReservationStatus reservationStatus;
            if (i <= 1_200) {
                reservationStatus = ReservationStatus.NO_SHOW;
            } else if (i <= 2_000) {
                reservationStatus = ReservationStatus.CANCELED;
            } else {
                reservationStatus = ReservationStatus.RESERVED;
            }

            long propertyCode = (i % 20) + 1;
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

            GuestType guestType =
                    random.nextBoolean() ? GuestType.INDIVIDUAL : GuestType.FAMILY;

            ReservationChannel channel =
                    random.nextBoolean() ? ReservationChannel.WEB : ReservationChannel.OTA;

            Reservation reservation = Reservation.createReservation(
                    checkin,
                    checkout,
                    1 + random.nextInt(4),
                    guestType,
                    channel,
                    roomPrice,
                    packagePrice,
                    propertyCode,
                    roomCode,
                    customerCode,
                    packageCode,
                    reservationStatus
            );

            // 취소 예약이면 취소 시각 세팅
            if (reservationStatus == ReservationStatus.CANCELED) {
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
                        .canceledAt(
                                reservation.getReservedAt()
                                        .plusHours(1 + random.nextInt(72))
                        )
                        .createdAt(reservation.getCreatedAt())
                        .propertyCode(reservation.getPropertyCode())
                        .roomCode(reservation.getRoomCode())
                        .customerCode(reservation.getCustomerCode())
                        .packageCode(reservation.getPackageCode())
                        .build();
            }

            reservationRepository.save(reservation);
        }
    }
}
