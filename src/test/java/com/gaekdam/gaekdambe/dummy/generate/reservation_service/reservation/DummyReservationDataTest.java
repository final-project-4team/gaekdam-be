package com.gaekdam.gaekdambe.dummy.generate.reservation_service.reservation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.gaekdam.gaekdambe.operation_service.room.command.domain.entity.Room;
import com.gaekdam.gaekdambe.operation_service.room.command.domain.entity.RoomType;
import com.gaekdam.gaekdambe.operation_service.room.command.infrastructure.repository.RoomRepository;
import com.gaekdam.gaekdambe.operation_service.room.command.infrastructure.repository.RoomTypeRepository;
import com.gaekdam.gaekdambe.reservation_service.reservation.command.domain.entity.Reservation;
import com.gaekdam.gaekdambe.reservation_service.reservation.command.domain.entity.ReservationPackage;
import com.gaekdam.gaekdambe.reservation_service.reservation.command.domain.enums.*;
import com.gaekdam.gaekdambe.reservation_service.reservation.command.infrastructure.repository.ReservationPackageRepository;
import com.gaekdam.gaekdambe.reservation_service.reservation.command.infrastructure.repository.ReservationRepository;

@Component
public class DummyReservationDataTest {

    private static final int TOTAL = 100_000;
    private static final int BATCH = 500;

    /** TODAY 운영 화면 보정용 */
    private static final int TODAY_TOTAL_COUNT = 30;

    @Autowired ReservationRepository reservationRepository;
    @Autowired RoomRepository roomRepository;
    @Autowired RoomTypeRepository roomTypeRepository;
    @Autowired ReservationPackageRepository packageRepository;
    @Autowired EntityManager em;

    @Transactional
    public void generate() {

        // 중복 생성 방지
        if (reservationRepository.count() > 0) return;

        Random random = new Random();
        LocalDate today = LocalDate.now();

        List<Room> rooms = roomRepository.findAll();
        List<RoomType> roomTypes = roomTypeRepository.findAll();

        Map<Long, List<Room>> roomsByRoomType =
                rooms.stream().collect(Collectors.groupingBy(Room::getRoomTypeCode));

        Map<Long, List<RoomType>> roomTypesByProperty =
                roomTypes.stream().collect(Collectors.groupingBy(RoomType::getPropertyCode));

        Map<Long, List<ReservationPackage>> packagesByProperty =
                packageRepository.findAll()
                        .stream()
                        .collect(Collectors.groupingBy(ReservationPackage::getPropertyCode));

        List<Long> propertyCodes = new ArrayList<>(roomTypesByProperty.keySet());
        List<Reservation> buffer = new ArrayList<>(BATCH);

        /* =================================================
           1️ TODAY 예약 분산 생성 (추가)
           ================================================= */
        for (int i = 0; i < TODAY_TOTAL_COUNT; i++) {

            Long propertyCode =
                    propertyCodes.get(random.nextInt(propertyCodes.size()));

            List<RoomType> candidateRoomTypes =
                    roomTypesByProperty.get(propertyCode);

            if (candidateRoomTypes == null || candidateRoomTypes.isEmpty()) continue;

            RoomType roomType =
                    candidateRoomTypes.get(random.nextInt(candidateRoomTypes.size()));

            List<Room> candidateRooms =
                    roomsByRoomType.get(roomType.getRoomTypeCode());

            if (candidateRooms == null || candidateRooms.isEmpty()) continue;

            Room room =
                    candidateRooms.get(random.nextInt(candidateRooms.size()));

            int guestCount =
                    1 + random.nextInt(roomType.getMaxCapacity());

            GuestType guestType =
                    guestCount == 1 ? GuestType.INDIVIDUAL :
                            guestCount <= 4 ? GuestType.FAMILY : GuestType.GROUP;

            ReservationChannel channel =
                    random.nextDouble() < 0.6 ? ReservationChannel.WEB :
                            random.nextDouble() < 0.9 ? ReservationChannel.OTA :
                                    ReservationChannel.PHONE;

            Long packageCode = null;
            BigDecimal packagePrice = BigDecimal.ZERO;

            List<ReservationPackage> pkgs =
                    packagesByProperty.get(propertyCode);

            if (pkgs != null && !pkgs.isEmpty() && random.nextDouble() < 0.3) {
                ReservationPackage pkg =
                        pkgs.get(random.nextInt(pkgs.size()));
                packageCode = pkg.getPackageCode();
                packagePrice = pkg.getPackagePrice();
            }

            LocalDateTime reservedAt =
                    today.minusDays(1 + random.nextInt(7)).atTime(10, 0);

            buffer.add(
                    Reservation.builder()
                            .reservationStatus(ReservationStatus.RESERVED)
                            .checkinDate(today)
                            .checkoutDate(today.plusDays(1 + random.nextInt(3)))
                            .guestCount(guestCount)
                            .guestType(guestType)
                            .reservationChannel(channel)
                            .reservationRoomPrice(roomType.getBasePrice())
                            .reservationPackagePrice(packagePrice)
                            .totalPrice(roomType.getBasePrice().add(packagePrice))
                            .reservedAt(reservedAt)
                            .createdAt(reservedAt)
                            .canceledAt(null)
                            .propertyCode(propertyCode)
                            .roomCode(room.getRoomCode())
                            .packageCode(packageCode)
                            .customerCode((long) (random.nextInt(50_000) + 1))
                            .build()
            );

            if (buffer.size() == BATCH) {
                reservationRepository.saveAll(buffer);
                em.flush();
                em.clear();
                buffer.clear();
            }
        }

        /* =================================================
           2️ 기존 월 단위 예약 생성 (기존 코드 100% 유지)
           ================================================= */
        LocalDate start = LocalDate.of(2024, 1, 1);
        LocalDate end   = LocalDate.of(2026, 12, 31);

        int months = 36;
        int basePerMonth = TOTAL / months;

        LocalDate cursor = start;

        while (!cursor.isAfter(end)) {

            int monthVolume =
                    Math.max(basePerMonth + random.nextInt(600) - 300, 300);

            if (cursor.getYear() == 2026) {
                if (cursor.getMonthValue() == 1) {
                    monthVolume = (int) (monthVolume * 1.20);
                } else if (cursor.getMonthValue() == 2) {
                    monthVolume = (int) (monthVolume * 1.15);
                }
            }

            double cancelRate  = 0.05 + random.nextDouble() * 0.10;
            double noShowRate  = 0.03 + random.nextDouble() * 0.07;
            double packageRate = 0.20 + random.nextDouble() * 0.40;

            for (int i = 0; i < monthVolume; i++) {

                Room room = rooms.get(random.nextInt(rooms.size()));
                RoomType roomType = roomTypes.stream()
                        .filter(rt -> rt.getRoomTypeCode().equals(room.getRoomTypeCode()))
                        .findFirst()
                        .orElseThrow();

                int lastDay = cursor.lengthOfMonth();
                LocalDate checkin =
                        cursor.withDayOfMonth(1 + random.nextInt(lastDay));
                LocalDate checkout =
                        checkin.plusDays(1 + random.nextInt(5));

                ReservationStatus status = ReservationStatus.RESERVED;
                double r = random.nextDouble();
                if (r < cancelRate) status = ReservationStatus.CANCELED;
                else if (r < cancelRate + noShowRate) status = ReservationStatus.NO_SHOW;

                int guestCount =
                        1 + random.nextInt(roomType.getMaxCapacity());

                GuestType guestType =
                        guestCount == 1 ? GuestType.INDIVIDUAL :
                                guestCount <= 4 ? GuestType.FAMILY : GuestType.GROUP;

                ReservationChannel channel =
                        random.nextDouble() < 0.5 ? ReservationChannel.WEB :
                                random.nextDouble() < 0.85 ? ReservationChannel.OTA :
                                        ReservationChannel.PHONE;

                Long packageCode = null;
                BigDecimal packagePrice = BigDecimal.ZERO;

                List<ReservationPackage> pkgs =
                        packagesByProperty.get(roomType.getPropertyCode());

                if (pkgs != null && !pkgs.isEmpty()
                        && random.nextDouble() < packageRate) {

                    ReservationPackage pkg =
                            pkgs.get(random.nextInt(pkgs.size()));
                    packageCode = pkg.getPackageCode();
                    packagePrice = pkg.getPackagePrice();
                }

                LocalDateTime reservedAt =
                        checkin.minusDays(1 + random.nextInt(30)).atTime(10, 0);

                LocalDateTime canceledAt =
                        status == ReservationStatus.CANCELED
                                ? reservedAt.plusHours(1 + random.nextInt(48))
                                : null;

                buffer.add(
                        Reservation.builder()
                                .reservationStatus(status)
                                .checkinDate(checkin)
                                .checkoutDate(checkout)
                                .guestCount(guestCount)
                                .guestType(guestType)
                                .reservationChannel(channel)
                                .reservationRoomPrice(roomType.getBasePrice())
                                .reservationPackagePrice(packagePrice)
                                .totalPrice(roomType.getBasePrice().add(packagePrice))
                                .reservedAt(reservedAt)
                                .createdAt(reservedAt)
                                .canceledAt(canceledAt)
                                .propertyCode(roomType.getPropertyCode())
                                .roomCode(room.getRoomCode())
                                .packageCode(packageCode)
                                .customerCode((long) (random.nextInt(50_000) + 1))
                                .build()
                );

                if (buffer.size() == BATCH) {
                    reservationRepository.saveAll(buffer);
                    em.flush();
                    em.clear();
                    buffer.clear();
                }
            }

            cursor = cursor.plusMonths(1);
        }

        if (!buffer.isEmpty()) {
            reservationRepository.saveAll(buffer);
            em.flush();
            em.clear();
        }
    }
}
