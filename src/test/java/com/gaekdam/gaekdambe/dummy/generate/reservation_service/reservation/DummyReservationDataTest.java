package com.gaekdam.gaekdambe.dummy.generate.reservation_service.reservation;

import com.gaekdam.gaekdambe.operation_service.room.command.domain.entity.Room;
import com.gaekdam.gaekdambe.operation_service.room.command.domain.entity.RoomType;
import com.gaekdam.gaekdambe.operation_service.room.command.infrastructure.repository.RoomRepository;
import com.gaekdam.gaekdambe.operation_service.room.command.infrastructure.repository.RoomTypeRepository;
import com.gaekdam.gaekdambe.reservation_service.reservation.command.domain.entity.Reservation;
import com.gaekdam.gaekdambe.reservation_service.reservation.command.domain.entity.ReservationPackage;
import com.gaekdam.gaekdambe.reservation_service.reservation.command.domain.enums.*;
import com.gaekdam.gaekdambe.reservation_service.reservation.command.infrastructure.repository.ReservationPackageRepository;
import com.gaekdam.gaekdambe.reservation_service.reservation.command.infrastructure.repository.ReservationRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

@Component
public class DummyReservationDataTest {

    @Autowired
    ReservationRepository reservationRepository;
    @Autowired
    RoomRepository roomRepository;
    @Autowired
    RoomTypeRepository roomTypeRepository;
    @Autowired
    ReservationPackageRepository packageRepository;

    @Transactional
    public void generate() {

        if (reservationRepository.count() > 0) return;

        LocalDate today = LocalDate.now();
        Random random = new Random();

        List<Room> rooms = roomRepository.findAll();
        List<RoomType> roomTypes = roomTypeRepository.findAll();

        Map<Long, List<ReservationPackage>> packagesByProperty =
                packageRepository.findAll()
                        .stream()
                        .collect(Collectors.groupingBy(ReservationPackage::getPropertyCode));

        int total = 10_000;

        for (int i = 0; i < total; i++) {

            Room room = rooms.get(random.nextInt(rooms.size()));
            RoomType roomType = roomTypes.stream()
                    .filter(rt -> rt.getRoomTypeCode().equals(room.getRoomTypeCode()))
                    .findFirst()
                    .orElseThrow();

            ReservationStatus status;
            LocalDate checkin;
            LocalDate checkout;
            LocalDateTime canceledAt = null;

            /* =========================
               예약 상태 분기
               ========================= */
            if (i < 800) { // CANCELED (미래 예약)
                status = ReservationStatus.CANCELED;
                checkin = today.plusDays(1 + random.nextInt(30));
                checkout = checkin.plusDays(1 + random.nextInt(5));
                canceledAt = LocalDateTime.now().minusHours(1);

            } else if (i < 1600) { // NO_SHOW (이미 지나감)
                status = ReservationStatus.NO_SHOW;
                checkin = today.minusDays(5 + random.nextInt(20));
                checkout = checkin.plusDays(1 + random.nextInt(3));

            } else { // RESERVED
                status = ReservationStatus.RESERVED;

                int mod = (i - 1600) % 4;

                if (mod == 0) {                // CHECKIN_PLANNED
                    checkin = today;
                    checkout = today.plusDays(1 + random.nextInt(5));

                } else if (mod == 1) {         // STAYING
                    checkin = today.minusDays(1 + random.nextInt(10));
                    checkout = today.plusDays(1 + random.nextInt(10));

                } else if (mod == 2) {         // CHECKOUT_PLANNED (today)
                    checkin = today.minusDays(1 + random.nextInt(10));
                    checkout = today;

                } else {                      // COMPLETED (과거)
                    checkin = today.minusDays(10 + random.nextInt(20));
                    checkout = checkin.plusDays(1 + random.nextInt(5));
                }
            }

            /* =========================
               패키지 선택 (property 기준)
               ========================= */
            List<ReservationPackage> pkgs =
                    packagesByProperty.getOrDefault(roomType.getPropertyCode(), List.of());

            ReservationPackage pkg =
                    pkgs.isEmpty() || random.nextBoolean()
                            ? null
                            : pkgs.get(random.nextInt(pkgs.size()));

            Reservation reservation =
                    Reservation.createReservation(
                            checkin,
                            checkout,
                            1 + random.nextInt(roomType.getMaxCapacity()),
                            GuestType.INDIVIDUAL,
                            ReservationChannel.WEB,
                            roomType.getBasePrice(),
                            pkg != null ? pkg.getPackagePrice() : BigDecimal.ZERO,
                            roomType.getPropertyCode(),
                            room.getRoomCode(),
                            (long) (random.nextInt(5_000) + 1),
                            pkg != null ? pkg.getPackageCode() : null,
                            status
                    );

            /* =========================
               CANCELED 보정 (canceledAt 필요)
               ========================= */
            if (status == ReservationStatus.CANCELED) {
                reservation =
                        Reservation.builder()
                                .reservationStatus(status)
                                .checkinDate(checkin)
                                .checkoutDate(checkout)
                                .guestCount(reservation.getGuestCount())
                                .guestType(reservation.getGuestType())
                                .reservationChannel(reservation.getReservationChannel())
                                .reservationRoomPrice(reservation.getReservationRoomPrice())
                                .reservationPackagePrice(reservation.getReservationPackagePrice())
                                .totalPrice(reservation.getTotalPrice())
                                .reservedAt(reservation.getReservedAt())
                                .canceledAt(canceledAt)
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
