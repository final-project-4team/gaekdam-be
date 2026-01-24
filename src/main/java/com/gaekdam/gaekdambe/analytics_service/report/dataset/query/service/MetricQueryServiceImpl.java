package com.gaekdam.gaekdambe.analytics_service.report.dataset.query.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Map;

import org.springframework.dao.EmptyResultDataAccessException;
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

        // target 조회: 먼저 필터에 hotelGroupCode가 있으면 해당 그룹의 목표값을 우선 조회하고, 없거나 없을 경우 기존 repo 폴백
        Object hotelGroupFilter = filter != null ? filter.get("hotelGroupCode") : null;
        BigDecimal target = null;
        if (hotelGroupFilter != null) {
            try {
                target = jdbc.queryForObject(
                    "SELECT target_value FROM reportkpitarget WHERE kpi_code = ? AND period_value = ? AND hotel_group_code = ? ORDER BY created_at DESC LIMIT 1",
                    BigDecimal.class, kpiCode, period, hotelGroupFilter);
            } catch (EmptyResultDataAccessException ex) {
                target = null;
            }
        }

        if (target == null) {
            target = targetRepo.findFirstByKpiCodeAndPeriodValue(kpiCode, period)
                    .map(ReportKPITarget::getTargetValue)
                    .orElseGet(() -> targetRepo.findFirstByKpiCodeOrderByCreatedAtDesc(kpiCode)
                            .map(ReportKPITarget::getTargetValue).orElse(BigDecimal.ZERO));
        }

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
            case "stay_guest_count": case "guest_count": case "stay_guests": {
                // Total number of guests who stayed during the period (sum of Stay.guest_count)
                Object hotelId = filter != null ? filter.get("hotelId") : null; // property_code
                Object hotelGroup = filter != null ? filter.get("hotelGroupCode") : null;

                BigDecimal guests = jdbc.queryForObject(
                    "SELECT COALESCE(SUM(s.guest_count),0) FROM Stay s WHERE s.actual_checkin_at < ? AND (s.actual_checkout_at > ? OR s.actual_checkout_at IS NULL)",
                    BigDecimal.class, tsEnd, tsStart);

                if (hotelId != null) {
                    guests = jdbc.queryForObject(
                        "SELECT COALESCE(SUM(s.guest_count),0) FROM Stay s JOIN Reservation r ON s.reservation_code = r.reservation_code WHERE s.actual_checkin_at < ? AND (s.actual_checkout_at > ? OR s.actual_checkout_at IS NULL) AND r.property_code = ?",
                        BigDecimal.class, tsEnd, tsStart, hotelId);
                } else if (hotelGroup != null) {
                    guests = jdbc.queryForObject(
                        "SELECT COALESCE(SUM(s.guest_count),0) FROM Stay s JOIN Reservation r ON s.reservation_code = r.reservation_code JOIN Property p ON r.property_code = p.property_code WHERE s.actual_checkin_at < ? AND (s.actual_checkout_at > ? OR s.actual_checkout_at IS NULL) AND p.hotel_group_code = ?",
                        BigDecimal.class, tsEnd, tsStart, hotelGroup);
                }

                if (guests == null) guests = BigDecimal.ZERO;
                return guests;
            }
            case "repeat_rate": case "REPEAT_RATE": {
                // Repeat rate = distinct customers in period who had prior stay before period start / distinct customers in period * 100
                Object hotelGroup = filter != null ? filter.get("hotelGroupCode") : null;

                String totalSql = "SELECT COALESCE(COUNT(DISTINCT s.customer_code),0) FROM Stay s JOIN Reservation r ON s.reservation_code = r.reservation_code";
                String repeatSql = "SELECT COALESCE(COUNT(DISTINCT s.customer_code),0) FROM Stay s JOIN Reservation r ON s.reservation_code = r.reservation_code";
                if (hotelGroup != null) {
                    totalSql += " JOIN Property p ON r.property_code = p.property_code WHERE s.actual_checkin_at < ? AND (s.actual_checkout_at > ? OR s.actual_checkout_at IS NULL) AND p.hotel_group_code = ?";
                    repeatSql += " JOIN Property p ON r.property_code = p.property_code WHERE s.actual_checkin_at < ? AND (s.actual_checkout_at > ? OR s.actual_checkout_at IS NULL) AND p.hotel_group_code = ? AND EXISTS (SELECT 1 FROM Stay s2 WHERE s2.customer_code = s.customer_code AND s2.actual_checkin_at < ?)";

                    BigDecimal totalCust = jdbc.queryForObject(totalSql, BigDecimal.class, tsEnd, tsStart, hotelGroup);
                    BigDecimal repeatCust = jdbc.queryForObject(repeatSql, BigDecimal.class, tsEnd, tsStart, hotelGroup, tsStart);
                    if (totalCust == null || totalCust.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
                    return repeatCust.divide(totalCust, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
                } else {
                    totalSql += " WHERE s.actual_checkin_at < ? AND (s.actual_checkout_at > ? OR s.actual_checkout_at IS NULL)";
                    repeatSql += " WHERE s.actual_checkin_at < ? AND (s.actual_checkout_at > ? OR s.actual_checkout_at IS NULL) AND EXISTS (SELECT 1 FROM Stay s2 WHERE s2.customer_code = s.customer_code AND s2.actual_checkin_at < ?)";

                    BigDecimal totalCust = jdbc.queryForObject(totalSql, BigDecimal.class, tsEnd, tsStart);
                    BigDecimal repeatCust = jdbc.queryForObject(repeatSql, BigDecimal.class, tsEnd, tsStart, tsStart);
                    if (totalCust == null || totalCust.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
                    return repeatCust.divide(totalCust, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
                }
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
            case "repeat_rate":
                return String.format("%s%%", value.setScale(1, RoundingMode.HALF_UP).toPlainString());
            default:
                // 정수형 수치 표시
                return String.format("%s", value.setScale(0, RoundingMode.HALF_UP).toPlainString());
        }
    }

    private String normalizeToKpiCode(String widgetKey) {
        if (widgetKey == null) return null;
        // Map common widget keys / aliases to the canonical KPI codes stored in reportkpitarget.kpi_code
        return switch(widgetKey.toUpperCase(Locale.ROOT)) {
            case "STAY_GUEST_COUNT", "GUEST_COUNT", "STAY_GUESTS", "GUESTS" -> "GUEST_COUNT";
            case "REPEAT_RATE", "REPEAT" -> "REPEAT_RATE";
            case "ADR", "AVG_DAILY_RATE", "AVERAGE_DAILY_RATE" -> "ADR";
            case "AVG_RESPONSE_TIME", "AVERAGE_RESPONSE_TIME", "RESPONSE_TIME" -> "AVG_RESPONSE_TIME";
            case "CANCELLATION_RATE", "CANCEL_RATE", "CANCELLED_RATE", "CANCELLATION" -> "CANCELLATION_RATE";
            case "CHECKIN", "CHECKIN_COUNT" -> "CHECKIN";
            case "CHECKOUT", "CHECKOUT_COUNT" -> "CHECKOUT";
            case "CLAIM_COUNT", "CLAIMS" -> "CLAIM_COUNT";
            case "FOREIGN_RATE", "FOREIGN" -> "FOREIGN_RATE";
            case "INQUIRY_COUNT", "INQUIRIES", "INQUIRY" -> "INQUIRY_COUNT";
            case "MEMBERSHIP_RATE", "MEMBERSHIP" -> "MEMBERSHIP_RATE";
            case "NON_ROOM_REVENUE", "NON_ROOM_SALES", "NONROOM_REVENUE", "NON_ROOM" -> "NON_ROOM_REVENUE";
            case "NO_SHOW_RATE", "NOSHOW_RATE", "NO_SHOW" -> "NO_SHOW_RATE";
            case "OCC_RATE", "OCCUPANCY", "OCCUPANCY_RATE" -> "OCC_RATE";
            case "RESERVATION_COUNT", "RESERVATIONS", "RESERVATION" -> "RESERVATION_COUNT";
            case "UNRESOLVED_RATE", "UNRESOLVED" -> "UNRESOLVED_RATE";
            default -> widgetKey.toUpperCase(Locale.ROOT);
        };
    }

    private String normalizeToInternalKey(String widgetKey) {
        if (widgetKey == null) return null;
        return switch(widgetKey.toUpperCase(Locale.ROOT)) {
            // CHECKIN / CHECKOUT
            case "CHECKIN_COUNT", "CHECKIN" -> "checkin";
            case "CHECKOUT_COUNT", "CHECKOUT" -> "checkout";
            // ADR
            case "ADR", "AVG_DAILY_RATE", "AVERAGE_DAILY_RATE" -> "avg_daily_rate";
            // Response time
            case "AVG_RESPONSE_TIME", "AVERAGE_RESPONSE_TIME", "RESPONSE_TIME" -> "avg_response_time";
            // Occupancy
            case "OCC_RATE", "OCCUPANCY", "OCCUPANCY_RATE" -> "occ_rate";
            // Guest count
            case "GUEST_COUNT", "STAY_GUEST_COUNT", "STAY_GUESTS", "GUESTS" -> "stay_guest_count";
            // Repeat
            case "REPEAT_RATE", "REPEAT" -> "repeat_rate";
            // Reservation count
            case "RESERVATION_COUNT", "RESERVATIONS", "RESERVATION" -> "reservation_count";
            // Cancellation
            case "CANCELLATION_RATE", "CANCEL_RATE", "CANCELLED_RATE", "CANCELLATION" -> "cancellation_rate";
            // No-show
            case "NO_SHOW_RATE", "NOSHOW_RATE", "NO_SHOW" -> "no_show_rate";
            // Inquiry
            case "INQUIRY_COUNT", "INQUIRIES", "INQUIRY" -> "inquiry_count";
            // Unresolved
            case "UNRESOLVED_RATE", "UNRESOLVED" -> "unresolved_rate";
            // Claim
            case "CLAIM_COUNT", "CLAIMS" -> "claim_count";
            // Foreign rate
            case "FOREIGN_RATE", "FOREIGN" -> "foreign_rate";
            // Membership rate
            case "MEMBERSHIP_RATE", "MEMBERSHIP" -> "membership_rate";
            // Non-room revenue
            case "NON_ROOM_REVENUE", "NON_ROOM_SALES", "NONROOM_REVENUE", "NON_ROOM" -> "non_room_revenue";
            default -> widgetKey.toLowerCase(Locale.ROOT);
        };
    }
}