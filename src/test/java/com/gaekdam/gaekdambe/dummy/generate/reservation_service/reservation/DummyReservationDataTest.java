package com.gaekdam.gaekdambe.dummy.generate.reservation_service.reservation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
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
import com.gaekdam.gaekdambe.reservation_service.reservation.command.domain.enums.GuestType;
import com.gaekdam.gaekdambe.reservation_service.reservation.command.domain.enums.ReservationChannel;
import com.gaekdam.gaekdambe.reservation_service.reservation.command.domain.enums.ReservationStatus;
import com.gaekdam.gaekdambe.reservation_service.reservation.command.infrastructure.repository.ReservationPackageRepository;
import com.gaekdam.gaekdambe.reservation_service.reservation.command.infrastructure.repository.ReservationRepository;

@Component
public class DummyReservationDataTest {

    private static final int TOTAL = 100_000; // 전체 예약 목표 건수
    private static final int BATCH = 500;     // JDBC batch + flush 주기

    @Autowired ReservationRepository reservationRepository;
    @Autowired RoomRepository roomRepository;
    @Autowired RoomTypeRepository roomTypeRepository;
    @Autowired ReservationPackageRepository packageRepository;
    @Autowired EntityManager em;

    @Transactional
    public void generate() {

        // 이미 데이터가 있으면 중복 생성 방지
        if (reservationRepository.count() > 0) return;

        Random random = new Random();

        // 더미 생성 중 반복 조회 방지를 위해 고정 데이터 캐싱
        List<Room> rooms = roomRepository.findAll();
        List<RoomType> roomTypes = roomTypeRepository.findAll();

        // propertyCode 기준 패키지 목록 캐싱
        Map<Long, List<ReservationPackage>> packagesByProperty =
                packageRepository.findAll()
                        .stream()
                        .collect(Collectors.groupingBy(
                                ReservationPackage::getPropertyCode
                        ));

        // 생성 기간 (월 단위 분포)
        LocalDate start = LocalDate.of(2024, 1, 1);
        LocalDate end   = LocalDate.of(2026, 12, 31);

        int months = 36;
        int basePerMonth = TOTAL / months;

        // saveAll + flush/clear 를 위한 버퍼
        List<Reservation> buffer = new ArrayList<>(BATCH);

        LocalDate cursor = start;

        while (!cursor.isAfter(end)) {

            // 월별 생성량에 변동을 주어 데이터가 균일하지 않게
            int monthVolume =
                    Math.max(basePerMonth + random.nextInt(600) - 300, 300);

            // === [추가] 최근 운영 데이터 체감 보정 ===
            // 2026년 1~2월은 Today / Operation 화면에서
            // 데이터가 부족해 보이지 않도록 소폭 증량
            if (cursor.getYear() == 2026) {
                if (cursor.getMonthValue() == 1) {
                    monthVolume = (int) (monthVolume * 1.20); // 1월 +20%
                } else if (cursor.getMonthValue() == 2) {
                    monthVolume = (int) (monthVolume * 1.15); // 2월 +15%
                }
            }

            // 월별 상태 비율
            double cancelRate  = 0.05 + random.nextDouble() * 0.10;
            double noShowRate  = 0.03 + random.nextDouble() * 0.07;
            double packageRate = 0.20 + random.nextDouble() * 0.40;

            for (int i = 0; i < monthVolume; i++) {

                // 랜덤 객실 → 해당 객실의 룸타입
                Room room = rooms.get(random.nextInt(rooms.size()));
                RoomType roomType = roomTypes.stream()
                        .filter(rt -> rt.getRoomTypeCode().equals(room.getRoomTypeCode()))
                        .findFirst()
                        .orElseThrow();

                // 체크인/체크아웃 (1~5박)
                int lastDay = cursor.lengthOfMonth();
                LocalDate checkin =
                        cursor.withDayOfMonth(1 + random.nextInt(lastDay));
                LocalDate checkout =
                        checkin.plusDays(1 + random.nextInt(5));

                // 예약 상태 결정
                ReservationStatus status = ReservationStatus.RESERVED;
                double r = random.nextDouble();
                if (r < cancelRate) status = ReservationStatus.CANCELED;
                else if (r < cancelRate + noShowRate) status = ReservationStatus.NO_SHOW;

                // 인원 수 생성
                int guestCount =
                        1 + random.nextInt(roomType.getMaxCapacity());

                // 인원 수에 따른 GuestType 보정
                GuestType guestType;
                if (guestCount == 1) {
                    guestType = GuestType.INDIVIDUAL;
                } else if (guestCount <= 4) {
                    guestType = GuestType.FAMILY;
                } else {
                    guestType = GuestType.GROUP;
                }

                // 예약 채널 분포 (WEB / OTA / PHONE)
                ReservationChannel channel;
                double channelRand = random.nextDouble();
                if (channelRand < 0.50) {
                    channel = ReservationChannel.WEB;
                } else if (channelRand < 0.85) {
                    channel = ReservationChannel.OTA;
                } else {
                    channel = ReservationChannel.PHONE;
                }

                // 패키지 선택
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

                // 예약 생성 시각
                LocalDateTime reservedAt =
                        checkin.minusDays(1 + random.nextInt(30)).atTime(10, 0);

                // 취소 예약만 canceledAt 설정
                LocalDateTime canceledAt =
                        status == ReservationStatus.CANCELED
                                ? reservedAt.plusHours(1 + random.nextInt(48))
                                : null;

                // 예약 엔티티 생성
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

                // 배치 단위 저장
                if (buffer.size() == BATCH) {
                    reservationRepository.saveAll(buffer);
                    em.flush();
                    em.clear();
                    buffer.clear();
                }
            }

            cursor = cursor.plusMonths(1);
        }

        // 남은 데이터 처리
        if (!buffer.isEmpty()) {
            reservationRepository.saveAll(buffer);
            em.flush();
            em.clear();
        }
    }
}
