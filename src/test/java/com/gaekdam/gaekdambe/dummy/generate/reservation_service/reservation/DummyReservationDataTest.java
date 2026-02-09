package com.gaekdam.gaekdambe.dummy.generate.reservation_service.reservation;

import com.gaekdam.gaekdambe.customer_service.customer.command.domain.entity.Customer;
import com.gaekdam.gaekdambe.customer_service.customer.command.infrastructure.repository.CustomerRepository;
import com.gaekdam.gaekdambe.hotel_service.property.command.domain.entity.Property;
import com.gaekdam.gaekdambe.hotel_service.property.command.infrastructure.PropertyRepository;
import com.gaekdam.gaekdambe.operation_service.room.command.domain.entity.Room;
import com.gaekdam.gaekdambe.operation_service.room.command.domain.entity.RoomType;
import com.gaekdam.gaekdambe.operation_service.room.command.infrastructure.repository.RoomRepository;
import com.gaekdam.gaekdambe.operation_service.room.command.infrastructure.repository.RoomTypeRepository;
import com.gaekdam.gaekdambe.reservation_service.reservation.command.domain.entity.Reservation;
import com.gaekdam.gaekdambe.reservation_service.reservation.command.domain.entity.ReservationPackage;
import com.gaekdam.gaekdambe.reservation_service.reservation.command.domain.enums.*;
import com.gaekdam.gaekdambe.reservation_service.reservation.command.infrastructure.repository.ReservationPackageRepository;
import com.gaekdam.gaekdambe.reservation_service.reservation.command.infrastructure.repository.ReservationRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class DummyReservationDataTest {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private static final int TOTAL = 100_000;
    private static final int BATCH = 500;

    /** TODAY 운영 화면 보정용 */
    private static final int TODAY_TOTAL_COUNT = 30;

    @Autowired ReservationRepository reservationRepository;
    @Autowired RoomRepository roomRepository;
    @Autowired RoomTypeRepository roomTypeRepository;
    @Autowired ReservationPackageRepository packageRepository;

    // 추가: customer/hotelGroup 매칭용
    @Autowired CustomerRepository customerRepository;
    @Autowired
    PropertyRepository propertyRepository;

    @Autowired EntityManager em;

    @Transactional
    public void generate() {

        // 중복 생성 방지
        if (reservationRepository.count() > 0) return;

        Random random = new Random();
        LocalDate today = LocalDate.now(KST);

        //  property_code -> hotel_group_code 맵 구성 (스코프 정합성 보장)
        Map<Long, Long> hotelGroupByProperty = buildHotelGroupByProperty();

        //  hotel_group_code -> customer_code 리스트 맵 구성 (예약 생성 시 매칭)
        Map<Long, List<Long>> customerCodesByHotelGroup = buildCustomerCodesByHotelGroup();

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
           1) TODAY 예약 분산 생성
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

            //  핵심: propertyCode에 맞는 hotelGroupCode 추출
            Long hotelGroupCode = hotelGroupByProperty.get(propertyCode);
            if (hotelGroupCode == null) continue;

            //  핵심: 해당 hotelGroupCode의 customer 중에서 뽑기 (스코프 정합성 보장)
            Long customerCode = pickCustomerCode(hotelGroupCode, customerCodesByHotelGroup, random);
            if (customerCode == null) continue;

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
                            .customerCode(customerCode) //  변경: 랜덤(1~50000) 제거
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
           2) 기존 월 단위 예약 생성 (기존 코드 유지 + customer 매칭만 수정)
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

                Long propertyCode = roomType.getPropertyCode();

                //  핵심: propertyCode -> hotelGroupCode -> customerCode
                Long hotelGroupCode = hotelGroupByProperty.get(propertyCode);
                if (hotelGroupCode == null) continue;

                Long customerCode = pickCustomerCode(hotelGroupCode, customerCodesByHotelGroup, random);
                if (customerCode == null) continue;

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
                                .propertyCode(propertyCode)
                                .roomCode(room.getRoomCode())
                                .packageCode(packageCode)
                                .customerCode(customerCode)
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

    private Map<Long, Long> buildHotelGroupByProperty() {
        // property_code -> hotel_group_code 매핑 (예약 생성 스코프 기준)
        List<Property> properties = propertyRepository.findAll();
        Map<Long, Long> map = new HashMap<>();
        for (Property p : properties) {
            if (p == null) continue;
            if (p.getPropertyCode() == null) continue;
            if (p.getHotelGroup() == null || p.getHotelGroup().getHotelGroupCode() == null) continue;
            map.put(p.getPropertyCode(), p.getHotelGroup().getHotelGroupCode());
        }
        return map;
    }

    private Map<Long, List<Long>> buildCustomerCodesByHotelGroup() {
        // hotel_group_code -> customer_code 리스트 구성 (예약 생성 시 반드시 이 풀에서 뽑아야 정합성 유지)
        List<Customer> customers = customerRepository.findAll();
        Map<Long, List<Long>> map = new HashMap<>();
        for (Customer c : customers) {
            if (c == null) continue;
            if (c.getCustomerCode() == null) continue;
            // Customer 엔티티에 getHotelGroupCode()가 없으면 필드/게터에 맞게 변경
            Long hotelGroupCode = c.getHotelGroupCode();
            if (hotelGroupCode == null) continue;
            map.computeIfAbsent(hotelGroupCode, k -> new ArrayList<>()).add(c.getCustomerCode());
        }
        // 랜덤 접근 성능 위해 정렬 (선택 사항)
        map.values().forEach(list -> list.sort(Comparator.naturalOrder()));
        return map;
    }

    private Long pickCustomerCode(
            Long hotelGroupCode,
            Map<Long, List<Long>> customerCodesByHotelGroup,
            Random random
    ) {
        List<Long> list = customerCodesByHotelGroup.get(hotelGroupCode);
        if (list == null || list.isEmpty()) return null;
        return list.get(random.nextInt(list.size()));
        //
    }
}
