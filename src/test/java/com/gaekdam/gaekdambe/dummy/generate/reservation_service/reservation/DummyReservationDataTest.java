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
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

@Component
public class DummyReservationDataTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private RoomTypeRepository roomTypeRepository;

    @Autowired
    private ReservationPackageRepository reservationPackageRepository;

    @Transactional
    public void generate() {

        if (reservationRepository.count() > 0) {
            return;
        }

        Random random = new Random();

        // 실제 존재 데이터 로딩
        List<Room> rooms = roomRepository.findAll();
        List<RoomType> roomTypes = roomTypeRepository.findAll();
        List<ReservationPackage> packages = reservationPackageRepository.findAll();

        Map<Long, RoomType> roomTypeMap =
                roomTypes.stream()
                        .collect(Collectors.toMap(
                                RoomType::getRoomTypeCode,
                                rt -> rt
                        ));

        int totalCount = 10_000;

        for (int i = 1; i <= totalCount; i++) {

            // ===== 예약 상태 =====
            ReservationStatus reservationStatus;
            if (i <= 1_200) {
                reservationStatus = ReservationStatus.NO_SHOW;
            } else if (i <= 2_000) {
                reservationStatus = ReservationStatus.CANCELED;
            } else {
                reservationStatus = ReservationStatus.RESERVED;
            }

            // ===== Room 선택 =====
            Room room = rooms.get(random.nextInt(rooms.size()));
            RoomType roomType = roomTypeMap.get(room.getRoomTypeCode());

            BigDecimal roomPrice = roomType.getBasePrice();

            // ===== Package 선택 =====
            boolean hasPackage = random.nextBoolean();

            ReservationPackage reservationPackage = null;
            BigDecimal packagePrice = BigDecimal.ZERO;

            if (hasPackage && !packages.isEmpty()) {
                reservationPackage =
                        packages.get(random.nextInt(packages.size()));
                packagePrice = reservationPackage.getPackagePrice();
            }

            // ===== 날짜 =====
            LocalDate checkin =
                    LocalDate.now().minusDays(random.nextInt(60));
            LocalDate checkout =
                    checkin.plusDays(1 + random.nextInt(3));

            // ===== 기타 =====
            GuestType guestType =
                    random.nextBoolean()
                            ? GuestType.INDIVIDUAL
                            : GuestType.FAMILY;

            ReservationChannel channel =
                    random.nextBoolean()
                            ? ReservationChannel.WEB
                            : ReservationChannel.OTA;

            long customerCode = random.nextInt(5_000) + 1;
            long propertyCode = roomType.getPropertyCode();

            // ===== 생성 =====
            Reservation reservation = Reservation.createReservation(
                    checkin,
                    checkout,
                    1 + random.nextInt(roomType.getMaxCapacity()),
                    guestType,
                    channel,
                    roomPrice,
                    packagePrice,
                    propertyCode,
                    room.getRoomCode(),
                    customerCode,
                    reservationPackage != null
                            ? reservationPackage.getPackageCode()
                            : null,
                    reservationStatus
            );

            // ===== 취소 예약 보정 =====
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