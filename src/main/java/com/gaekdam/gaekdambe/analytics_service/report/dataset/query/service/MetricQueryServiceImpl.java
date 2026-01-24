package com.gaekdam.gaekdambe.analytics_service.report.dataset.query.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gaekdam.gaekdambe.analytics_service.report.dataset.command.domain.entity.ReportKPITarget;
import com.gaekdam.gaekdambe.analytics_service.report.dataset.query.repository.ReportKpiTargetRepository; // 엔티티 경로 맞출 것

@Service
@Transactional(readOnly = true)
public class MetricQueryServiceImpl implements MetricQueryService {

    private final JdbcTemplate jdbc;
    private final ReportKpiTargetRepository targetRepo;

    public MetricQueryServiceImpl(JdbcTemplate jdbc, ReportKpiTargetRepository targetRepo) {
        this.jdbc = jdbc;
        this.targetRepo = targetRepo;
    }

    @Override
    public MetricResult queryMetric(String metricKey, String period, Map<String, Object> filter) {
        // 1) 날짜 범위 계산 (period like "2025" or "2025-12")
        LocalDate start;
        LocalDate end;
        try {
            LocalDate[] range = computeRangeFromPeriod(period);
            start = range[0];
            end = range[1];
        } catch (IllegalArgumentException ex) {
            // invalid period -> default to current month
            LocalDate now = LocalDate.now();
            start = now.withDayOfMonth(1);
            end = start.plusMonths(1);
        }

        // 2) actual 집계
        BigDecimal actual = queryActualFromDb(metricKey, start, end, filter);

        // 3) target 조회
        String kpiCode = normalizeToKpiCode(metricKey);         // target lookup
        String internalKey = normalizeToInternalKey(metricKey); // DB aggregation key

        // target 조회: repository 메서드명이 변경된 것을 사용
        BigDecimal target = targetRepo.findFirstByKpiCodeAndPeriodValue(kpiCode, period)
                .map(ReportKPITarget::getTargetValue)
                .orElseGet(() -> targetRepo.findFirstByKpiCodeOrderByCreatedAtDesc(kpiCode)
                        .map(ReportKPITarget::getTargetValue).orElse(BigDecimal.ZERO));

        // 4) changePct & trend 계산
        Double changePct = null;
        String trend = "neutral";
        if (target != null && target.compareTo(BigDecimal.ZERO) != 0) {
            BigDecimal diff = actual.subtract(target);
            changePct = diff.divide(target, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100)).doubleValue();
            trend = changePct > 0 ? "up" : (changePct < 0 ? "down" : "neutral");
        }

        // 5) 포맷
        String formattedActual = formatByMetric(internalKey, actual);
        String formattedTarget = formatByMetric(internalKey, target);

        MetricResult r = new MetricResult();
        r.setActual(actual);
        r.setTarget(target);
        r.setFormattedActual(formattedActual);
        r.setFormattedTarget(formattedTarget);
        r.setChangePct(changePct);
        r.setTrend(trend);
        return r;
    }

    // --- helper: compute date range from period string ---
    private LocalDate[] computeRangeFromPeriod(String period) {
        if (period == null) throw new IllegalArgumentException("period null");
        // "2025" -> year range, "2025-12" -> month range
        if (period.matches("\\d{4}")) {
            int y = Integer.parseInt(period);
            LocalDate s = LocalDate.of(y, 1, 1);
            LocalDate e = s.plusYears(1);
            return new LocalDate[]{s, e};
        } else if (period.matches("\\d{4}-\\d{1,2}")) {
            String[] parts = period.split("-");
            int y = Integer.parseInt(parts[0]);
            int m = Integer.parseInt(parts[1]);
            LocalDate s = LocalDate.of(y, m, 1);
            LocalDate e = s.plusMonths(1);
            return new LocalDate[]{s, e};
        } else {
            throw new IllegalArgumentException("unsupported period: " + period);
        }
    }

    // --- helper: query actual metric from DB (simple switch) ---
    private BigDecimal queryActualFromDb(String metricKey, LocalDate start, LocalDate end, Map<String, Object> filter) {
        Date dStart = Date.valueOf(start);
        Date dEnd = Date.valueOf(end);
        Timestamp tsStart = Timestamp.valueOf(start.atStartOfDay());
        Timestamp tsEnd = Timestamp.valueOf(end.atStartOfDay());

        switch (metricKey) {
            case "checkin": {
                // Use checkinout joined with stay -> reservation -> property to respect property.hotel_group_code
                Object hotelGroup = filter != null ? filter.get("hotelGroupCode") : null;
                Integer cnt;
                if (hotelGroup != null) {
                    cnt = jdbc.queryForObject(
                        "SELECT COUNT(*) FROM checkinout c JOIN stay s ON c.stay_code = s.stay_code JOIN reservation r ON s.reservation_code = r.reservation_code JOIN property p ON r.property_code = p.property_code WHERE c.record_type = 'CHECK_IN' AND c.recorded_at >= ? AND c.recorded_at < ? AND p.hotel_group_code = ?",
                        Integer.class, tsStart, tsEnd, hotelGroup);
                } else {
                    cnt = jdbc.queryForObject(
                        "SELECT COUNT(*) FROM checkinout WHERE record_type = 'CHECK_IN' AND recorded_at >= ? AND recorded_at < ?",
                        Integer.class, tsStart, tsEnd);
                }
                return BigDecimal.valueOf(cnt != null ? cnt : 0);
            }
            case "checkout": {
                // Use same join pattern for checkout
                Object hotelGroup2 = filter != null ? filter.get("hotelGroupCode") : null;
                Integer cnt2;
                if (hotelGroup2 != null) {
                    cnt2 = jdbc.queryForObject(
                        "SELECT COUNT(*) FROM checkinout c JOIN stay s ON c.stay_code = s.stay_code JOIN reservation r ON s.reservation_code = r.reservation_code JOIN property p ON r.property_code = p.property_code WHERE c.record_type = 'CHECK_OUT' AND c.recorded_at >= ? AND c.recorded_at < ? AND p.hotel_group_code = ?",
                        Integer.class, tsStart, tsEnd, hotelGroup2);
                } else {
                    cnt2 = jdbc.queryForObject(
                        "SELECT COUNT(*) FROM checkinout WHERE record_type = 'CHECK_OUT' AND recorded_at >= ? AND recorded_at < ?",
                        Integer.class, tsStart, tsEnd);
                }
                return BigDecimal.valueOf(cnt2 != null ? cnt2 : 0);
            }
            case "adr": {
                // ADR: weighted average = total room revenue during overlapping nights / occupied_room_nights
                // Use reservation.reservation_room_price (assumed total room charge per reservation) and overlap logic
                BigDecimal totalRevenue = jdbc.queryForObject(
                    "SELECT COALESCE(SUM(reservation_room_price),0) FROM reservation WHERE checkin_date < ? AND checkout_date > ? AND canceled_at IS NULL",
                    BigDecimal.class, dEnd, dStart);

                Integer occupiedNights = jdbc.queryForObject(
                    "SELECT COALESCE(SUM(GREATEST(DATEDIFF(LEAST(checkout_date, ?), GREATEST(checkin_date, ?)),0)),0) FROM reservation WHERE checkin_date < ? AND checkout_date > ? AND canceled_at IS NULL",
                    Integer.class, dEnd, dStart, dEnd, dStart);

                if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;
                if (occupiedNights == null || occupiedNights == 0) return BigDecimal.ZERO;

                return totalRevenue.divide(BigDecimal.valueOf(occupiedNights.longValue()), 2, RoundingMode.HALF_UP);
            }
            case "occ_rate": {
                // Occupancy = occupied_room_nights / (total_rooms * days_in_period) * 100
                Object hotelId = filter != null ? filter.get("hotelId") : null; // treated as property_code
                Object hotelGroup = filter != null ? filter.get("hotelGroupCode") : null;

                long periodDays = ChronoUnit.DAYS.between(start, end);
                if (periodDays <= 0) return BigDecimal.ZERO;

                Integer occupiedNights;
                if (hotelId != null) {
                    // count occupied nights for reservations belonging to the specific property (property_code)
                    occupiedNights = jdbc.queryForObject(
                        "SELECT COALESCE(SUM(GREATEST(DATEDIFF(LEAST(r.checkout_date, ?), GREATEST(r.checkin_date, ?)),0)),0) " +
                        "FROM reservation r JOIN property p ON r.property_code = p.property_code " +
                        "WHERE r.checkin_date < ? AND r.checkout_date > ? AND r.canceled_at IS NULL AND p.property_code = ?",
                        Integer.class, dEnd, dStart, dEnd, dStart, hotelId);
                } else if (hotelGroup != null) {
                    // count occupied nights for reservations in properties that belong to the hotel group
                    occupiedNights = jdbc.queryForObject(
                        "SELECT COALESCE(SUM(GREATEST(DATEDIFF(LEAST(r.checkout_date, ?), GREATEST(r.checkin_date, ?)),0)),0) " +
                        "FROM reservation r JOIN property p ON r.property_code = p.property_code " +
                        "WHERE r.checkin_date < ? AND r.checkout_date > ? AND r.canceled_at IS NULL AND p.hotel_group_code = ?",
                        Integer.class, dEnd, dStart, dEnd, dStart, hotelGroup);
                } else {
                    // count occupied nights across all reservations (no property filter)
                    occupiedNights = jdbc.queryForObject(
                        "SELECT COALESCE(SUM(GREATEST(DATEDIFF(LEAST(r.checkout_date, ?), GREATEST(r.checkin_date, ?)),0)),0) " +
                        "FROM reservation r WHERE r.checkin_date < ? AND r.checkout_date > ? AND r.canceled_at IS NULL",
                        Integer.class, dEnd, dStart, dEnd, dStart);
                }

                Integer totalRooms = 0;
                if (hotelId != null) {
                    // count active rooms for the property via Room -> RoomType -> property mapping
                    totalRooms = jdbc.queryForObject(
                        "SELECT COALESCE(COUNT(r.room_code),0) FROM Room r JOIN RoomType rt ON r.room_type_code = rt.room_type_code WHERE rt.property_code = ? AND r.room_status = 'ACTIVE'",
                        Integer.class, hotelId);
                } else if (hotelGroup != null) {
                    // count active rooms for all properties in the hotel group
                    totalRooms = jdbc.queryForObject(
                        "SELECT COALESCE(COUNT(r.room_code),0) FROM Room r JOIN RoomType rt ON r.room_type_code = rt.room_type_code JOIN Property p ON rt.property_code = p.property_code WHERE p.hotel_group_code = ? AND r.room_status = 'ACTIVE'",
                        Integer.class, hotelGroup);
                } else {
                    // total active rooms across all properties
                    totalRooms = jdbc.queryForObject(
                        "SELECT COALESCE(COUNT(r.room_code),0) FROM Room r WHERE r.room_status = 'ACTIVE'",
                        Integer.class);
                }

                BigDecimal availableRoomNights = BigDecimal.valueOf((totalRooms != null ? totalRooms : 0) * periodDays);
                if (availableRoomNights.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;

                BigDecimal occ = BigDecimal.valueOf(occupiedNights != null ? occupiedNights : 0)
                        .divide(availableRoomNights, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
                return occ;
            }
            // 필요한 metricKey 추가 구현...
            default:
                return BigDecimal.ZERO;
        }
    }

    // --- helper: simple formatting rule (정수 vs 통화) ---
    private String formatByMetric(String metricKey, BigDecimal value) {
        if (value == null) return "-";
        switch (metricKey) {
            case "avg_daily_rate":
                // 통화(원) 포맷. 간단히 정수에 콤마 + "원" 붙이기
                return String.format("%,d원", value.setScale(0, RoundingMode.HALF_UP).longValue());
            case "occupancy":
            case "occ_rate":
                return String.format("%s%%", value.setScale(1, RoundingMode.HALF_UP).toPlainString());
            default:
                // 정수형 수치 표시
                return String.format("%s", value.setScale(0, RoundingMode.HALF_UP).toPlainString());
        }
    }

    private String normalizeToKpiCode(String widgetKey) {
        if (widgetKey == null) return null;
        return widgetKey.toUpperCase(Locale.ROOT); // 기본: KPI 코드가 대문자 형태라 가정
    }

    private String normalizeToInternalKey(String widgetKey) {
        if (widgetKey == null) return null;
        return switch(widgetKey.toUpperCase(Locale.ROOT)) {
            case "CHECKIN_COUNT", "CHECKIN" -> "checkin";
            case "CHECKOUT_COUNT", "CHECKOUT" -> "checkout";
            case "ADR", "AVG_DAILY_RATE" -> "avg_daily_rate";
            case "OCC_RATE", "OCCUPANCY" -> "occupancy";
            // ...필요한 KPI 매핑을 추가(16개 KPI에 맞춰)
            default -> widgetKey.toLowerCase(Locale.ROOT);
        };
    }
}