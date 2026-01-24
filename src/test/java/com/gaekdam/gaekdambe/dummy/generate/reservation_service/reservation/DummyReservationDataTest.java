package com.gaekdam.gaekdambe.dummy.generate.reservation_service.reservation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.gaekdam.gaekdambe.operation_service.room.command.domain.entity.Room;
import com.gaekdam.gaekdambe.operation_service.room.command.domain.entity.RoomType;
import com.gaekdam.gaekdambe.operation_service.room.command.infrastructure.repository.RoomRepository;
import com.gaekdam.gaekdambe.operation_service.room.command.infrastructure.repository.RoomTypeRepository;
import com.gaekdam.gaekdambe.reservation_service.reservation.command.domain.entity.Reservation;
import com.gaekdam.gaekdambe.reservation_service.reservation.command.domain.entity.ReservationPackage;
import com.gaekdam.gaekdambe.reservation_service.reservation.command.domain.enums.GuestType;
import com.gaekdam.gaekdambe.reservation_service.reservation.command.domain.enums.ReservationChannel;
import com.gaekdam.gaekdambe.reservation_service.reservation.command.domain.enums.ReservationStatus;
import com.gaekdam.gaekdambe.reservation_service.reservation.command.infrastructure.repository.ReservationPackageRepository;
import com.gaekdam.gaekdambe.reservation_service.reservation.command.infrastructure.repository.ReservationRepository;

import jakarta.transaction.Transactional;

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

            // choose a package for this property's reservation if available
            Long chosenPackageCode = null;
            BigDecimal chosenPackagePrice = BigDecimal.ZERO;
            List<ReservationPackage> pkgs = packagesByProperty.get(roomType.getPropertyCode());


            boolean usePackage = pkgs != null && !pkgs.isEmpty() && random.nextDouble() < 0.4;

            if (usePackage) {
                ReservationPackage pkg = pkgs.get(random.nextInt(pkgs.size()));
                if (pkg != null) {
                    chosenPackageCode = pkg.getPackageCode();
                    chosenPackagePrice = pkg.getPackagePrice() != null
                            ? pkg.getPackagePrice() : BigDecimal.ZERO;
                }
            }


            LocalDateTime canceledAt = null;

            if (i < 800) {
                // 취소
                status = ReservationStatus.CANCELED;
                checkin = today.plusDays(1 + random.nextInt(30));
                checkout = checkin.plusDays(1 + random.nextInt(5));
                // set canceled_at after reservedAt later (reservedAt computed below)
            } else {
                // default to RESERVED; some will be NO_SHOW
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
                    // some of these become NO_SHOW
                    if (random.nextDouble() < 0.6) {
                        status = ReservationStatus.NO_SHOW;
                    }
                }
            }

            LocalDateTime reservedAt =
                    checkin.minusDays(1 + random.nextInt(14)).atTime(10, 0);

            // reservation_status 값이 CANCELED 이면 cancel_at 데이터를 reservedAt 시점으로 72시간 이내에 생성함
            if (status == ReservationStatus.CANCELED) {
                long maxHours = Math.max(1, java.time.Duration.between(reservedAt, checkin.atStartOfDay()).toHours());
                int hours = 1 + random.nextInt((int) Math.max(1, Math.min(72, maxHours)));
                canceledAt = reservedAt.plusHours(hours);
            }

            Reservation reservation =
                    Reservation.builder()
                            .reservationStatus(status)
                            .checkinDate(checkin)
                            .checkoutDate(checkout)
                            .guestCount(1 + random.nextInt(roomType.getMaxCapacity()))
                            .guestType(GuestType.INDIVIDUAL)
                            .reservationChannel(ReservationChannel.WEB)
                            .reservationRoomPrice(roomType.getBasePrice())
                            .reservationPackagePrice(chosenPackagePrice)
                            .totalPrice(roomType.getBasePrice().add(chosenPackagePrice != null ? chosenPackagePrice : BigDecimal.ZERO))
                            .reservedAt(reservedAt)
                            .createdAt(reservedAt)
                            .canceledAt(canceledAt)
                            .propertyCode(roomType.getPropertyCode())
                            .roomCode(room.getRoomCode())
                            .packageCode(chosenPackageCode)
                            .customerCode((long) (random.nextInt(5_000) + 1))
                            .build();

            reservationRepository.save(reservation);
        }
    }
}
