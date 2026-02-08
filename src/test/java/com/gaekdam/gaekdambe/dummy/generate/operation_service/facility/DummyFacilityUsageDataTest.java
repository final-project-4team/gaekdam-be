package com.gaekdam.gaekdambe.dummy.generate.operation_service.facility;

import com.gaekdam.gaekdambe.operation_service.facility.command.domain.enums.FacilityUsageType;
import com.gaekdam.gaekdambe.operation_service.facility.command.domain.enums.PriceSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Component
public class DummyFacilityUsageDataTest {

    private static final int BATCH_SIZE = 500;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Transactional
    public void generate() {

        // 이미 생성돼 있으면 스킵
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM facility_usage",
                Long.class
        );
        if (count != null && count > 0) return;

        Random random = new Random();

        // -------------------------------
        // 1. Stay 조회 (실제 체크인만)
        // -------------------------------
        List<StayRow> stays = jdbcTemplate.query("""
            SELECT stay_code, reservation_code, actual_checkin_at, actual_checkout_at
            FROM stay
            WHERE actual_checkin_at IS NOT NULL
        """, (rs, i) -> new StayRow(
                rs.getLong("stay_code"),
                rs.getLong("reservation_code"),
                rs.getTimestamp("actual_checkin_at").toLocalDateTime(),
                rs.getTimestamp("actual_checkout_at") != null
                        ? rs.getTimestamp("actual_checkout_at").toLocalDateTime()
                        : null
        ));

        if (stays.isEmpty()) return;

        // -------------------------------
        // 2. reservation_code → property / package
        // -------------------------------
        Map<Long, ReservationInfo> reservationMap = jdbcTemplate.query("""
            SELECT reservation_code, property_code, package_code
            FROM reservation
        """, rs -> {
            Map<Long, ReservationInfo> map = new HashMap<>();
            while (rs.next()) {
                map.put(
                        rs.getLong("reservation_code"),
                        new ReservationInfo(
                                rs.getLong("property_code"),
                                rs.getObject("package_code") != null
                                        ? rs.getLong("package_code")
                                        : null
                        )
                );
            }
            return map;
        });

        // -------------------------------
        // 3. property_code → facilities
        // -------------------------------
        Map<Long, List<Long>> facilityMap = jdbcTemplate.query("""
            SELECT facility_code, property_code
            FROM facility
        """, rs -> {
            Map<Long, List<Long>> map = new HashMap<>();
            while (rs.next()) {
                map.computeIfAbsent(
                        rs.getLong("property_code"),
                        k -> new ArrayList<>()
                ).add(rs.getLong("facility_code"));
            }
            return map;
        });

        // -------------------------------
        // 4. package_code → facility_code
        // -------------------------------
        Map<Long, Set<Long>> packageFacilityMap = jdbcTemplate.query("""
            SELECT package_code, facility_code
            FROM package_facility
        """, rs -> {
            Map<Long, Set<Long>> map = new HashMap<>();
            while (rs.next()) {
                map.computeIfAbsent(
                        rs.getLong("package_code"),
                        k -> new HashSet<>()
                ).add(rs.getLong("facility_code"));
            }
            return map;
        });

        // -------------------------------
        // 5. batch insert 준비
        // -------------------------------
        List<Object[]> batch = new ArrayList<>(BATCH_SIZE);

        for (StayRow stay : stays) {

            ReservationInfo reservation = reservationMap.get(stay.reservationCode());
            if (reservation == null) continue;

            List<Long> facilities =
                    facilityMap.getOrDefault(reservation.propertyCode(), List.of());
            if (facilities.isEmpty()) continue;

            Set<Long> packageFacilities =
                    reservation.packageCode() != null
                            ? packageFacilityMap.getOrDefault(reservation.packageCode(), Set.of())
                            : Set.of();

            int usageCount = random.nextInt(3) + 1;
            boolean staying = stay.actualCheckoutAt() == null;

            // ------------------------------------
            // ★ 핵심 수정 포인트
            // ------------------------------------
            LocalDateTime usageEnd =
                    staying
                            // 투숙중: 체크인 기준 + 2일로 고정
                            ? stay.actualCheckinAt()
                            .toLocalDate()
                            .plusDays(2)
                            .atTime(23, 59, 59)
                            // 체크아웃 완료: 실제 체크아웃 시점
                            : stay.actualCheckoutAt();

            for (int i = 0; i < usageCount; i++) {

                Long facilityCode =
                        facilities.get(random.nextInt(facilities.size()));

                boolean personBased = random.nextBoolean();

                LocalDateTime usageAt =
                        randomBetween(stay.actualCheckinAt(), usageEnd, random);

                Integer usedPerson =
                        personBased ? random.nextInt(4) + 1 : null;
                Integer quantity =
                        personBased ? null : random.nextInt(3) + 1;

                FacilityUsageType usageType =
                        (usedPerson != null && usedPerson >= 2)
                                ? FacilityUsageType.WITH_GUEST
                                : FacilityUsageType.PERSONAL;

                boolean usePackage =
                        reservation.packageCode() != null
                                && packageFacilities.contains(facilityCode)
                                && random.nextInt(100) < 70;

                BigDecimal price;
                PriceSource priceSource;

                if (usePackage) {
                    price = BigDecimal.ZERO;
                    priceSource = PriceSource.PACKAGE;
                } else {
                    int qty = quantity != null ? quantity : 1;
                    BigDecimal unit =
                            BigDecimal.valueOf((random.nextInt(5) + 1) * 10_000);
                    price = unit.multiply(BigDecimal.valueOf(qty));
                    priceSource = PriceSource.EXTRA;
                }

                batch.add(new Object[]{
                        stay.stayCode(),
                        facilityCode,
                        Timestamp.valueOf(usageAt),
                        usageType.name(),
                        usedPerson,
                        quantity,
                        price,
                        priceSource.name(),
                        Timestamp.valueOf(LocalDateTime.now())
                });

                if (batch.size() == BATCH_SIZE) {
                    flush(batch);
                    batch.clear();
                }
            }
        }

        if (!batch.isEmpty()) {
            flush(batch);
        }
    }

    private void flush(List<Object[]> batch) {
        jdbcTemplate.batchUpdate("""
            INSERT INTO facility_usage (
                stay_code,
                facility_code,
                usage_at,
                usage_type,
                used_person_count,
                usage_quantity,
                usage_price,
                price_source,
                created_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """, batch);
    }

    private LocalDateTime randomBetween(
            LocalDateTime start,
            LocalDateTime end,
            Random random
    ) {
        long seconds = Duration.between(start, end).getSeconds();
        if (seconds <= 0) return start;
        return start.plusSeconds(random.nextLong(seconds));
    }

    record StayRow(
            Long stayCode,
            Long reservationCode,
            LocalDateTime actualCheckinAt,
            LocalDateTime actualCheckoutAt
    ) {}

    record ReservationInfo(
            Long propertyCode,
            Long packageCode
    ) {}
}
