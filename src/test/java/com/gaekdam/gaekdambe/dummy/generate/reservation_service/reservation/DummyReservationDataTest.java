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
import java.util.*;
import java.util.stream.Collectors;

@Component
public class DummyReservationDataTest {

    @Autowired ReservationRepository reservationRepository;
    @Autowired RoomRepository roomRepository;
    @Autowired RoomTypeRepository roomTypeRepository;
    @Autowired ReservationPackageRepository packageRepository;

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

            if (i < 800) {
                // 취소
                status = ReservationStatus.CANCELED;
                checkin = today.plusDays(1 + random.nextInt(30));
                checkout = checkin.plusDays(1 + random.nextInt(5));
            } else {
                // 전부 RESERVED
                status = ReservationStatus.RESERVED;

                int mod = (i - 800) % 4;

                if (mod == 0) {                 // 체크인 예정
                    checkin = today.plusDays(1);
                    checkout = checkin.plusDays(1 + random.nextInt(5));
                } else if (mod == 1) {          // 투숙중
                    checkin = today.minusDays(1 + random.nextInt(3));
                    checkout = today.plusDays(1 + random.nextInt(3));
                } else if (mod == 2) {          // 정상 완료
                    checkin = today.minusDays(10 + random.nextInt(10));
                    checkout = checkin.plusDays(1 + random.nextInt(5));
                } else {                       // 노쇼 후보 (과거 + Stay 없음)
                    checkin = today.minusDays(5 + random.nextInt(5));
                    checkout = checkin.plusDays(1 + random.nextInt(3));
                }
            }

            LocalDateTime reservedAt =
                    checkin.minusDays(1 + random.nextInt(14)).atTime(10, 0);

            Reservation reservation =
                    Reservation.builder()
                            .reservationStatus(status)
                            .checkinDate(checkin)
                            .checkoutDate(checkout)
                            .guestCount(1 + random.nextInt(roomType.getMaxCapacity()))
                            .guestType(GuestType.INDIVIDUAL)
                            .reservationChannel(ReservationChannel.WEB)
                            .reservationRoomPrice(roomType.getBasePrice())
                            .reservationPackagePrice(BigDecimal.ZERO)
                            .totalPrice(roomType.getBasePrice())
                            .reservedAt(reservedAt)
                            .createdAt(reservedAt)
                            .propertyCode(roomType.getPropertyCode())
                            .roomCode(room.getRoomCode())
                            .customerCode((long) (random.nextInt(5_000) + 1))
                            .build();

            reservationRepository.save(reservation);
        }
    }
}
