package com.gaekdam.gaekdambe.analytics_service.report.dataset.query.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gaekdam.gaekdambe.analytics_service.report.dataset.command.domain.entity.ReportKPITarget;
import com.gaekdam.gaekdambe.analytics_service.report.dataset.query.repository.ReportKpiTargetRepository;

@Service
@Transactional(readOnly = true)
public class MetricQueryServiceImpl implements MetricQueryService {
    private static final Logger log = LoggerFactory.getLogger(MetricQueryServiceImpl.class);

    private final JdbcTemplate jdbc;
    private final ReportKpiTargetRepository targetRepo;

    public MetricQueryServiceImpl(JdbcTemplate jdbc, ReportKpiTargetRepository targetRepo) {
        this.jdbc = jdbc;
        this.targetRepo = targetRepo;
    }

    @Override
    public MetricResult queryMetric(String metricKey, String period, Map<String, Object> filter) {
        // 로그: 진입점
        log.debug("queryMetric called: metricKey={}, period={}, filter={}", metricKey, period, filter);

        // 1) 날짜 범위 계산 (period like "2025" or "2025-12")
        LocalDate start;
        LocalDate end;
        try {
            LocalDate[] range = computeRangeFromPeriod(period);
            start = range[0];
            end = range[1];
            log.debug("Computed date range from period: period={}, start={}, end={}", period, start, end);
        } catch (IllegalArgumentException ex) {
            // invalid period -> default to current month
            LocalDate now = LocalDate.now();
            start = now.withDayOfMonth(1);
            end = start.plusMonths(1);
            log.debug("Invalid period provided, defaulting to current month: start={}, end={}", start, end);
        }

        // Normalize metric key to the internal aggregation key (so aliases like CHECKIN_COUNT, CHECKIN both work)
        String kpiCode = normalizeToKpiCode(metricKey);         // target lookup
        String internalKey = normalizeToInternalKey(metricKey); // DB aggregation key
        log.debug("Normalized keys: metricKey={}, kpiCode={}, internalKey={}", metricKey, kpiCode, internalKey);

        // 2) actual 집계 (use internalKey so switch-case matches)
        log.debug("Querying actual value: internalKey={}, start={}, end={}, filters={}", internalKey, start, end, filter);
        BigDecimal actual = queryActualFromDb(internalKey, start, end, filter);
        log.debug("Actual value retrieved: {} -> {}", internalKey, actual);

        // 3) target 조회

        // target 조회: 먼저 필터에 hotelGroupCode가 있으면 해당 그룹의 목표값을 우선 조회하고, 없거나 없을 경우 기존 repo 폴백
        Object hotelGroupFilter = filter != null ? filter.get("hotelGroupCode") : null;
        BigDecimal target = null;
        if (hotelGroupFilter != null) {
            log.debug("Looking up target by hotelGroup: kpiCode={}, period={}, hotelGroup={}", kpiCode, period, hotelGroupFilter);
            try {
                target = jdbc.queryForObject(
                    "SELECT target_value FROM reportkpitarget WHERE kpi_code = ? AND period_value = ? AND hotel_group_code = ? ORDER BY created_at DESC LIMIT 1",
                    BigDecimal.class, kpiCode, period, hotelGroupFilter);
                log.debug("Target found by hotelGroup: {}", target);
            } catch (EmptyResultDataAccessException ex) {
                target = null;
                log.debug("No target found for hotelGroup={}, will fallback to repository lookup", hotelGroupFilter);
            }
        }

        if (target == null) {
            log.debug("Falling back to repository lookup for target: kpiCode={}, period={}", kpiCode, period);
            target = targetRepo.findFirstByKpiCodeAndPeriodValue(kpiCode, period)
                    .map(ReportKPITarget::getTargetValue)
                    .orElseGet(() -> targetRepo.findFirstByKpiCodeOrderByCreatedAtDesc(kpiCode)
                            .map(ReportKPITarget::getTargetValue).orElse(BigDecimal.ZERO));
            log.debug("Target resolved from repository or default: {}", target);
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

        log.debug("MetricResult prepared: actual={}, target={}, changePct={}, trend={}", actual, target, changePct, trend);
        return r;
    }

    // --- helper: compute date range from period string ---
    private LocalDate[] computeRangeFromPeriod(String period) {
        if (period == null) throw new IllegalArgumentException("period null");
        // 사용자 요구: period=YYYY -> start=YYYY-01-01, end=YYYY-12-31 (포함)
        //            period=YYYY-MM -> start=YYYY-MM-01, end=해당월의 마지막날 (포함)
        if (period.matches("\\d{4}")) {
            int y = Integer.parseInt(period);
            LocalDate s = LocalDate.of(y, 1, 1);
            // 마지막날 포함(end = 12/31)
            LocalDate e = s.plusYears(1).minusDays(1);
            return new LocalDate[]{s, e};
        } else if (period.matches("\\d{4}-\\d{1,2}")) {
            String[] parts = period.split("-");
            int y = Integer.parseInt(parts[0]);
            int m = Integer.parseInt(parts[1]);
            LocalDate s = LocalDate.of(y, m, 1);
            // 해당월의 마지막날 포함
            LocalDate e = s.plusMonths(1).minusDays(1);
            return new LocalDate[]{s, e};
        } else {
            throw new IllegalArgumentException("unsupported period: " + period);
        }
    }

    // --- helper: query actual metric from DB (simple switch) ---
    private BigDecimal queryActualFromDb(String metricKey, LocalDate start, LocalDate end, Map<String, Object> filter) {
        log.debug("queryActualFromDb called: metricKey={}, start={}, end={}, filter={}", metricKey, start, end, filter);
        Date dStart = Date.valueOf(start);
        // compute exclusive upper bound for SQL queries (end is inclusive in our API contract)
        LocalDate endExclusive = end.plusDays(1);
        Date dEnd = Date.valueOf(endExclusive);
        Timestamp tsStart = Timestamp.valueOf(start.atStartOfDay());
        Timestamp tsEnd = Timestamp.valueOf(endExclusive.atStartOfDay());

        String mk = metricKey == null ? "" : metricKey.toLowerCase(Locale.ROOT);
        // common filters extracted once to avoid repeated declarations in each case
        Object hotelGroup = filter != null ? filter.get("hotelGroupCode") : null;
        Object hotelId = filter != null ? filter.get("hotelId") : null; // treated as property_code
        switch (mk) {
            // 1. 체크인수
            case "checkin" -> {
                // use hotelGroup from outer scope
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
            // 2. 체크아웃수
            case "checkout" -> {
                // use hotelGroup from outer scope
                Integer cnt2;
                if (hotelGroup != null) {
                    cnt2 = jdbc.queryForObject(
                        "SELECT COUNT(*) FROM checkinout c JOIN stay s ON c.stay_code = s.stay_code JOIN reservation r ON s.reservation_code = r.reservation_code JOIN property p ON r.property_code = p.property_code WHERE c.record_type = 'CHECK_OUT' AND c.recorded_at >= ? AND c.recorded_at < ? AND p.hotel_group_code = ?",
                        Integer.class, tsStart, tsEnd, hotelGroup);
                } else {
                    cnt2 = jdbc.queryForObject(
                        "SELECT COUNT(*) FROM checkinout WHERE record_type = 'CHECK_OUT' AND recorded_at >= ? AND recorded_at < ?",
                        Integer.class, tsStart, tsEnd);
                }
                return BigDecimal.valueOf(cnt2 != null ? cnt2 : 0);
            }
            // 3. 평균객실단가
            case "adr", "avg_daily_rate", "average_daily_rate" -> {
                // Use a single-query with two subqueries (total_revenue, occupied_nights) then compute adr safely
                // Respect hotelId (property_code) and hotelGroup (hotel_group_code) filters when provided
                String sql;
                BigDecimal adrResult;
                if (hotelId != null) {
                    sql = "SELECT CASE WHEN occupied_nights = 0 THEN 0 ELSE ROUND(total_revenue / occupied_nights, 2) END FROM ("
                        + " SELECT (SELECT COALESCE(SUM(r.reservation_room_price),0) FROM reservation r WHERE r.checkin_date < ? AND r.checkout_date > ? AND r.canceled_at IS NULL AND r.property_code = ?) AS total_revenue,"
                        + " (SELECT COALESCE(SUM(GREATEST(DATEDIFF(LEAST(r.checkout_date, ?), GREATEST(r.checkin_date, ?)),0)),0) FROM reservation r WHERE r.checkin_date < ? AND r.checkout_date > ? AND r.canceled_at IS NULL AND r.property_code = ?) AS occupied_nights"
                        + " ) t";
                    adrResult = jdbc.queryForObject(sql, BigDecimal.class, dEnd, dStart, hotelId, dEnd, dStart, dEnd, dStart, hotelId);
                } else if (hotelGroup != null) {
                    sql = "SELECT CASE WHEN occupied_nights = 0 THEN 0 ELSE ROUND(total_revenue / occupied_nights, 2) END FROM ("
                        + " SELECT (SELECT COALESCE(SUM(r.reservation_room_price),0) FROM reservation r JOIN property p ON r.property_code = p.property_code WHERE r.checkin_date < ? AND r.checkout_date > ? AND r.canceled_at IS NULL AND p.hotel_group_code = ?) AS total_revenue,"
                        + " (SELECT COALESCE(SUM(GREATEST(DATEDIFF(LEAST(r.checkout_date, ?), GREATEST(r.checkin_date, ?)),0)),0) FROM reservation r JOIN property p ON r.property_code = p.property_code WHERE r.checkin_date < ? AND r.checkout_date > ? AND r.canceled_at IS NULL AND p.hotel_group_code = ?) AS occupied_nights"
                        + " ) t";
                    adrResult = jdbc.queryForObject(sql, BigDecimal.class, dEnd, dStart, hotelGroup, dEnd, dStart, dEnd, dStart, hotelGroup);
                } else {
                    sql = "SELECT CASE WHEN occupied_nights = 0 THEN 0 ELSE ROUND(total_revenue / occupied_nights, 2) END FROM ("
                        + " SELECT (SELECT COALESCE(SUM(reservation_room_price),0) FROM reservation WHERE checkin_date < ? AND checkout_date > ? AND canceled_at IS NULL) AS total_revenue,"
                        + " (SELECT COALESCE(SUM(GREATEST(DATEDIFF(LEAST(checkout_date, ?), GREATEST(checkin_date, ?)),0)),0) FROM reservation WHERE checkin_date < ? AND checkout_date > ? AND canceled_at IS NULL) AS occupied_nights"
                        + " ) t";
                    adrResult = jdbc.queryForObject(sql, BigDecimal.class, dEnd, dStart, dEnd, dStart, dEnd, dStart);
                }

                if (adrResult == null) return BigDecimal.ZERO;
                // ensure scale 2
                return adrResult.setScale(2, RoundingMode.HALF_UP);
            }
            // 4. 객실점유율
            case "occ_rate" -> {
                // Occupancy = occupied_room_nights / (total_rooms * days_in_period) * 100
                // use hotelId and hotelGroup from outer scope
                long periodDays = ChronoUnit.DAYS.between(start, end);
                if (periodDays <= 0) return BigDecimal.ZERO;

                Integer occupiedNights;
                if (hotelId != null) {
                    occupiedNights = jdbc.queryForObject(
                        "SELECT COALESCE(SUM(GREATEST(DATEDIFF(LEAST(r.checkout_date, ?), GREATEST(r.checkin_date, ?)),0)),0) FROM reservation r JOIN property p ON r.property_code = p.property_code WHERE r.checkin_date < ? AND r.checkout_date > ? AND r.canceled_at IS NULL AND p.property_code = ?",
                        Integer.class, dEnd, dStart, dEnd, dStart, hotelId);
                } else if (hotelGroup != null) {
                    occupiedNights = jdbc.queryForObject(
                        "SELECT COALESCE(SUM(GREATEST(DATEDIFF(LEAST(r.checkout_date, ?), GREATEST(r.checkin_date, ?)),0)),0) FROM reservation r JOIN property p ON r.property_code = p.property_code WHERE r.checkin_date < ? AND r.checkout_date > ? AND r.canceled_at IS NULL AND p.hotel_group_code = ?",
                        Integer.class, dEnd, dStart, dEnd, dStart, hotelGroup);
                } else {
                    occupiedNights = jdbc.queryForObject(
                        "SELECT COALESCE(SUM(GREATEST(DATEDIFF(LEAST(r.checkout_date, ?), GREATEST(r.checkin_date, ?)),0)),0) FROM reservation r WHERE r.checkin_date < ? AND r.checkout_date > ? AND r.canceled_at IS NULL",
                        Integer.class, dEnd, dStart, dEnd, dStart);
                }

                Integer totalRooms;
                if (hotelId != null) {
                    totalRooms = jdbc.queryForObject(
                        "SELECT COALESCE(COUNT(r.room_code),0) FROM room r JOIN room_type rt ON r.room_type_code = rt.room_type_code WHERE rt.property_code = ? AND r.room_status = 'ACTIVE'",
                        Integer.class, hotelId);
                } else if (hotelGroup != null) {
                    totalRooms = jdbc.queryForObject(
                        "SELECT COALESCE(COUNT(r.room_code),0) FROM room r JOIN room_type rt ON r.room_type_code = rt.room_type_code JOIN property p ON rt.property_code = p.property_code WHERE p.hotel_group_code = ? AND r.room_status = 'ACTIVE'",
                        Integer.class, hotelGroup);
                } else {
                    totalRooms = jdbc.queryForObject(
                        "SELECT COALESCE(COUNT(r.room_code),0) FROM room r WHERE r.room_status = 'ACTIVE'",
                        Integer.class);
                }

                BigDecimal availableRoomNights = BigDecimal.valueOf((totalRooms != null ? totalRooms : 0) * periodDays);
                if (availableRoomNights.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;

                BigDecimal occ = BigDecimal.valueOf(occupiedNights != null ? occupiedNights : 0)
                        .divide(availableRoomNights, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
                return occ;
            }
            // 5. 투숙객수
            case "stay_guest_count", "guest_count", "stay_guests" -> {
                // Total number of guests who stayed during the period (sum of Stay.guest_count)
                // use hotelId and hotelGroup from outer scope
                BigDecimal guests = jdbc.queryForObject(
                    "SELECT COALESCE(SUM(s.guest_count),0) FROM stay s WHERE s.actual_checkin_at < ? AND (s.actual_checkout_at > ? OR s.actual_checkout_at IS NULL)",
                    BigDecimal.class, tsEnd, tsStart);

                if (hotelId != null) {
                    guests = jdbc.queryForObject(
                        "SELECT COALESCE(SUM(s.guest_count),0) FROM stay s JOIN reservation r ON s.reservation_code = r.reservation_code WHERE s.actual_checkin_at < ? AND (s.actual_checkout_at > ? OR s.actual_checkout_at IS NULL) AND r.property_code = ?",
                        BigDecimal.class, tsEnd, tsStart, hotelId);
                } else if (hotelGroup != null) {
                    guests = jdbc.queryForObject(
                        "SELECT COALESCE(SUM(s.guest_count),0) FROM stay s JOIN reservation r ON s.reservation_code = r.reservation_code JOIN property p ON r.property_code = p.property_code WHERE s.actual_checkin_at < ? AND (s.actual_checkout_at > ? OR s.actual_checkout_at IS NULL) AND p.hotel_group_code = ?",
                        BigDecimal.class, tsEnd, tsStart, hotelGroup);
                }

                if (guests == null) guests = BigDecimal.ZERO;
                return guests;
            }
            // 6. 재방문율
            case "repeat_rate" -> {
                // Repeat rate = distinct customers in period who had prior stay before period start / distinct customers in period * 100
                // use hotelGroup from outer scope
                String totalSql = "SELECT COALESCE(COUNT(DISTINCT s.customer_code),0) FROM stay s JOIN reservation r ON s.reservation_code = r.reservation_code";
                String repeatSql = "SELECT COALESCE(COUNT(DISTINCT s.customer_code),0) FROM stay s JOIN reservation r ON s.reservation_code = r.reservation_code";
                if (hotelGroup != null) {
                    totalSql += " JOIN property p ON r.property_code = p.property_code WHERE s.actual_checkin_at < ? AND (s.actual_checkout_at > ? OR s.actual_checkout_at IS NULL) AND p.hotel_group_code = ?";
                    repeatSql += " JOIN property p ON r.property_code = p.property_code WHERE s.actual_checkin_at < ? AND (s.actual_checkout_at > ? OR s.actual_checkout_at IS NULL) AND p.hotel_group_code = ? AND EXISTS (SELECT 1 FROM stay s2 WHERE s2.customer_code = s.customer_code AND s2.actual_checkin_at < ?)";

                    BigDecimal totalCust = jdbc.queryForObject(totalSql, BigDecimal.class, tsEnd, tsStart, hotelGroup);
                    BigDecimal repeatCust = jdbc.queryForObject(repeatSql, BigDecimal.class, tsEnd, tsStart, hotelGroup, tsStart);
                    if (totalCust == null || totalCust.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
                    return repeatCust.divide(totalCust, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
                } else {
                    totalSql += " WHERE s.actual_checkin_at < ? AND (s.actual_checkout_at > ? OR s.actual_checkout_at IS NULL)";
                    repeatSql += " WHERE s.actual_checkin_at < ? AND (s.actual_checkout_at > ? OR s.actual_checkout_at IS NULL) AND EXISTS (SELECT 1 FROM stay s2 WHERE s2.customer_code = s.customer_code AND s2.actual_checkin_at < ?)";

                    BigDecimal totalCust = jdbc.queryForObject(totalSql, BigDecimal.class, tsEnd, tsStart);
                    BigDecimal repeatCust = jdbc.queryForObject(repeatSql, BigDecimal.class, tsEnd, tsStart, tsStart);
                    if (totalCust == null || totalCust.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
                    return repeatCust.divide(totalCust, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
                }
            }
            // 7. 멤버십비율
            case "membership_rate" -> {
                // use hotelGroup from outer scope
                if (hotelGroup != null) {
                    BigDecimal total = jdbc.queryForObject(
                        "SELECT COALESCE(COUNT(DISTINCT s.customer_code),0) FROM stay s JOIN reservation r ON s.reservation_code = r.reservation_code JOIN property p ON r.property_code = p.property_code WHERE s.actual_checkin_at < ? AND (s.actual_checkout_at > ? OR s.actual_checkout_at IS NULL) AND p.hotel_group_code = ?",
                        BigDecimal.class, tsEnd, tsStart, hotelGroup);
                    BigDecimal member = jdbc.queryForObject(
                        "SELECT COALESCE(COUNT(DISTINCT s.customer_code),0) FROM stay s JOIN reservation r ON s.reservation_code = r.reservation_code JOIN property p ON r.property_code = p.property_code JOIN member m ON s.customer_code = m.customer_code WHERE s.actual_checkin_at < ? AND (s.actual_checkout_at > ? OR s.actual_checkout_at IS NULL) AND p.hotel_group_code = ?",
                        BigDecimal.class, tsEnd, tsStart, hotelGroup);
                    if (total == null || total.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
                    return member.divide(total, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
                } else {
                    BigDecimal total = jdbc.queryForObject(
                        "SELECT COALESCE(COUNT(DISTINCT s.customer_code),0) FROM stay s WHERE s.actual_checkin_at < ? AND (s.actual_checkout_at > ? OR s.actual_checkout_at IS NULL)",
                        BigDecimal.class, tsEnd, tsStart);
                    BigDecimal member = jdbc.queryForObject(
                        "SELECT COALESCE(COUNT(DISTINCT s.customer_code),0) FROM stay s JOIN member m ON s.customer_code = m.customer_code WHERE s.actual_checkin_at < ? AND (s.actual_checkout_at > ? OR s.actual_checkout_at IS NULL)",
                        BigDecimal.class, tsEnd, tsStart);
                    if (total == null || total.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
                    return member.divide(total, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
                }
            }
            // 8. 외국인비율
            case "foreign_rate" -> {
                if (hotelGroup != null) {
                    BigDecimal foreignCnt = jdbc.queryForObject(
                        "SELECT COALESCE(COUNT(DISTINCT s.customer_code),0) FROM stay s JOIN reservation r ON s.reservation_code = r.reservation_code JOIN customer c ON s.customer_code = c.customer_code JOIN property p ON r.property_code = p.property_code WHERE c.nationality_type = 'FOREIGN' AND s.actual_checkin_at < ? AND (s.actual_checkout_at > ? OR s.actual_checkout_at IS NULL) AND p.hotel_group_code = ?",
                        BigDecimal.class, tsEnd, tsStart, hotelGroup);
                    BigDecimal totalCnt = jdbc.queryForObject(
                        "SELECT COALESCE(COUNT(DISTINCT s.customer_code),0) FROM stay s JOIN reservation r ON s.reservation_code = r.reservation_code JOIN property p ON r.property_code = p.property_code WHERE s.actual_checkin_at < ? AND (s.actual_checkout_at > ? OR s.actual_checkout_at IS NULL) AND p.hotel_group_code = ?",
                        BigDecimal.class, tsEnd, tsStart, hotelGroup);
                    if (totalCnt == null || totalCnt.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
                    return foreignCnt.divide(totalCnt, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
                } else {
                    BigDecimal foreignCnt = jdbc.queryForObject(
                        "SELECT COALESCE(COUNT(DISTINCT s.customer_code),0) FROM stay s JOIN customer c ON s.customer_code = c.customer_code WHERE c.nationality_type = 'FOREIGN' AND s.actual_checkin_at < ? AND (s.actual_checkout_at > ? OR s.actual_checkout_at IS NULL)",
                        BigDecimal.class, tsEnd, tsStart);
                    BigDecimal totalCnt = jdbc.queryForObject(
                        "SELECT COALESCE(COUNT(DISTINCT s.customer_code),0) FROM stay s WHERE s.actual_checkin_at < ? AND (s.actual_checkout_at > ? OR s.actual_checkout_at IS NULL)",
                        BigDecimal.class, tsEnd, tsStart);
                    if (totalCnt == null || totalCnt.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
                    return foreignCnt.divide(totalCnt, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
                }
            }
            // 9. 문의수
            case "inquiry_count" -> {
                if (hotelGroup != null) {
                    return jdbc.queryForObject(
                        "SELECT COALESCE(COUNT(i.inquiry_code),0) FROM inquiry i JOIN property p ON i.property_code = p.property_code WHERE i.created_at >= ? AND i.created_at < ? AND p.hotel_group_code = ?",
                        BigDecimal.class, tsStart, tsEnd, hotelGroup);
                } else {
                    return jdbc.queryForObject(
                        "SELECT COALESCE(COUNT(i.inquiry_code),0) FROM inquiry i WHERE i.created_at >= ? AND i.created_at < ?",
                        BigDecimal.class, tsStart, tsEnd);
                }
            }
            // 10. 클레임수
            case "claim_count" -> {
                if (hotelGroup != null) {
                    return jdbc.queryForObject(
                        "SELECT COALESCE(COUNT(i.inquiry_code),0) FROM inquiry i JOIN property p ON i.property_code = p.property_code WHERE i.inquiry_category_code = 2 AND i.created_at >= ? AND i.created_at < ? AND p.hotel_group_code = ?",
                        BigDecimal.class, tsStart, tsEnd, hotelGroup);
                } else {
                    return jdbc.queryForObject(
                        "SELECT COALESCE(COUNT(i.inquiry_code),0) FROM inquiry i WHERE i.inquiry_category_code = 2 AND i.created_at >= ? AND i.created_at < ?",
                        BigDecimal.class, tsStart, tsEnd);
                }
            }
            // 11. 미처리문의비율
            case "unresolved_rate" -> {
                if (hotelGroup != null) {
                    BigDecimal total = jdbc.queryForObject(
                        "SELECT COALESCE(COUNT(i.inquiry_code),0) FROM inquiry i JOIN property p ON i.property_code = p.property_code WHERE i.created_at >= ? AND i.created_at < ? AND p.hotel_group_code = ?",
                        BigDecimal.class, tsStart, tsEnd, hotelGroup);
                    BigDecimal unresolved = jdbc.queryForObject(
                        "SELECT COALESCE(COUNT(i.inquiry_code),0) FROM inquiry i JOIN property p ON i.property_code = p.property_code WHERE i.created_at >= ? AND i.created_at < ? AND p.hotel_group_code = ? AND i.inquiry_status = 'IN_PROGRESS'",
                        BigDecimal.class, tsStart, tsEnd, hotelGroup);
                    if (total == null || total.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
                    return unresolved.divide(total, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
                } else {
                    BigDecimal total = jdbc.queryForObject(
                        "SELECT COALESCE(COUNT(i.inquiry_code),0) FROM inquiry i WHERE i.created_at >= ? AND i.created_at < ?",
                        BigDecimal.class, tsStart, tsEnd);
                    BigDecimal unresolved = jdbc.queryForObject(
                        "SELECT COALESCE(COUNT(i.inquiry_code),0) FROM inquiry i WHERE i.created_at >= ? AND i.created_at < ? AND i.inquiry_status = 'IN_PROGRESS'",
                        BigDecimal.class, tsStart, tsEnd);
                    if (total == null || total.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
                    return unresolved.divide(total, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
                }
            }
            // 12. 평균응답시간
            case "avg_response_time" -> {
                if (hotelGroup != null) {
                    BigDecimal secs = jdbc.queryForObject(
                        "SELECT COALESCE(AVG(TIMESTAMPDIFF(SECOND, i.created_at, i.updated_at)),0) FROM inquiry i JOIN property p ON i.property_code = p.property_code WHERE i.created_at >= ? AND i.created_at < ? AND i.answer_content IS NOT NULL AND p.hotel_group_code = ?",
                        BigDecimal.class, tsStart, tsEnd, hotelGroup);
                    secs = secs == null ? BigDecimal.ZERO : secs;
                    return secs.divide(BigDecimal.valueOf(3600), 2, RoundingMode.HALF_UP);
                } else {
                    BigDecimal secs = jdbc.queryForObject(
                        "SELECT COALESCE(AVG(TIMESTAMPDIFF(SECOND, i.created_at, i.updated_at)),0) FROM inquiry i WHERE i.created_at >= ? AND i.created_at < ? AND i.answer_content IS NOT NULL",
                        BigDecimal.class, tsStart, tsEnd);
                    secs = secs == null ? BigDecimal.ZERO : secs;
                    return secs.divide(BigDecimal.valueOf(3600), 2, RoundingMode.HALF_UP);
                }
            }
            // 13. 예약수
            case "reservation_count" -> {
                if (hotelGroup != null) {
                    return jdbc.queryForObject(
                        "SELECT COALESCE(COUNT(r.reservation_code),0) FROM reservation r JOIN property p ON r.property_code = p.property_code WHERE r.created_at >= ? AND r.created_at < ? AND p.hotel_group_code = ?",
                        BigDecimal.class, tsStart, tsEnd, hotelGroup);
                } else {
                    return jdbc.queryForObject(
                        "SELECT COALESCE(COUNT(r.reservation_code),0) FROM reservation r WHERE r.created_at >= ? AND r.created_at < ?",
                        BigDecimal.class, tsStart, tsEnd);
                }
            }
            // 14. 예약취소율
            case "cancellation_rate" -> {
                String totalSql;
                String canceledSql;
                if (hotelGroup != null) {
                    totalSql = "SELECT COALESCE(COUNT(r.reservation_code),0) FROM reservation r JOIN property p ON r.property_code = p.property_code WHERE r.created_at >= ? AND r.created_at < ? AND p.hotel_group_code = ?";
                    canceledSql = "SELECT COALESCE(COUNT(r.reservation_code),0) FROM reservation r JOIN property p ON r.property_code = p.property_code WHERE r.created_at >= ? AND r.created_at < ? AND p.hotel_group_code = ? AND r.canceled_at IS NOT NULL";

                    BigDecimal total = jdbc.queryForObject(totalSql, BigDecimal.class, tsStart, tsEnd, hotelGroup);
                    BigDecimal canceled = jdbc.queryForObject(canceledSql, BigDecimal.class, tsStart, tsEnd, hotelGroup);
                    if (total == null || total.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
                    return canceled.divide(total, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
                } else {
                    totalSql = "SELECT COALESCE(COUNT(r.reservation_code),0) FROM reservation r WHERE r.created_at >= ? AND r.created_at < ?";
                    canceledSql = "SELECT COALESCE(COUNT(r.reservation_code),0) FROM reservation r WHERE r.created_at >= ? AND r.created_at < ? AND r.canceled_at IS NOT NULL";
                    BigDecimal total = jdbc.queryForObject(totalSql, BigDecimal.class, tsStart, tsEnd);
                    BigDecimal canceled = jdbc.queryForObject(canceledSql, BigDecimal.class, tsStart, tsEnd);
                    if (total == null || total.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
                    return canceled.divide(total, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
                }
            }
            // 15. 노쇼율
            case "no_show_rate" -> {
                String totalSql;
                String noshowSql;
                if (hotelGroup != null) {
                    totalSql = "SELECT COALESCE(COUNT(r.reservation_code),0) FROM reservation r JOIN property p ON r.property_code = p.property_code WHERE r.checkin_date < ? AND r.checkout_date > ? AND r.canceled_at IS NULL AND p.hotel_group_code = ?";
                    noshowSql = "SELECT COALESCE(COUNT(r.reservation_code),0) FROM reservation r JOIN property p ON r.property_code = p.property_code WHERE r.checkin_date < ? AND r.checkout_date > ? AND r.canceled_at IS NULL AND p.hotel_group_code = ? AND r.reservation_status = 'NO_SHOW'";

                    BigDecimal total = jdbc.queryForObject(totalSql, BigDecimal.class, dEnd, dStart, hotelGroup);
                    BigDecimal noshow = jdbc.queryForObject(noshowSql, BigDecimal.class, dEnd, dStart, hotelGroup);
                    if (total == null || total.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
                    return noshow.divide(total, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
                } else {
                    totalSql = "SELECT COALESCE(COUNT(r.reservation_code),0) FROM reservation r WHERE r.checkin_date < ? AND r.checkout_date > ? AND r.canceled_at IS NULL";
                    noshowSql = "SELECT COALESCE(COUNT(r.reservation_code),0) FROM reservation r WHERE r.checkin_date < ? AND r.checkout_date > ? AND r.canceled_at IS NULL AND r.reservation_status = 'NO_SHOW'";

                    BigDecimal total = jdbc.queryForObject(totalSql, BigDecimal.class, dEnd, dStart);
                    BigDecimal noshow = jdbc.queryForObject(noshowSql, BigDecimal.class, dEnd, dStart);
                    if (total == null || total.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
                    return noshow.divide(total, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
                }
            }
            // 16. 객실외매출비율
            case "non_room_revenue" -> {
                if (hotelGroup != null) {
                    BigDecimal packageSum = jdbc.queryForObject(
                        "SELECT COALESCE(SUM(r.reservation_package_price),0) FROM reservation r JOIN property p ON r.property_code = p.property_code WHERE p.hotel_group_code = ? AND r.checkin_date < ? AND r.checkout_date > ? AND r.canceled_at IS NULL",
                        BigDecimal.class, hotelGroup, dEnd, dStart);
                    BigDecimal denomSum = jdbc.queryForObject(
                        "SELECT COALESCE(SUM(COALESCE(r.reservation_room_price,0) + COALESCE(r.reservation_package_price,0)),0) FROM reservation r JOIN property p ON r.property_code = p.property_code WHERE p.hotel_group_code = ? AND r.checkin_date < ? AND r.checkout_date > ? AND r.canceled_at IS NULL",
                        BigDecimal.class, hotelGroup, dEnd, dStart);
                    if (packageSum == null) packageSum = BigDecimal.ZERO;
                    if (denomSum == null) denomSum = BigDecimal.ZERO;
                    if (denomSum.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
                    return packageSum.divide(denomSum, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
                } else {
                    BigDecimal packageSum = jdbc.queryForObject(
                        "SELECT COALESCE(SUM(r.reservation_package_price),0) FROM reservation r WHERE r.checkin_date < ? AND r.checkout_date > ? AND r.canceled_at IS NULL",
                        BigDecimal.class, dEnd, dStart);
                    BigDecimal denomSum = jdbc.queryForObject(
                        "SELECT COALESCE(SUM(COALESCE(r.reservation_room_price,0) + COALESCE(r.reservation_package_price,0)),0) FROM reservation r WHERE r.checkin_date < ? AND r.checkout_date > ? AND r.canceled_at IS NULL",
                        BigDecimal.class, dEnd, dStart);
                    if (packageSum == null) packageSum = BigDecimal.ZERO;
                    if (denomSum == null) denomSum = BigDecimal.ZERO;
                    if (denomSum.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
                    return packageSum.divide(denomSum, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
                }
            }
            default -> {
                return BigDecimal.ZERO;
            }
        }
        // end switch
    }

    // --- helper: simple formatting rule (정수 vs 통화) ---
    private String formatByMetric(String metricKey, BigDecimal value) {
        if (value == null) return "-";
        String key = metricKey == null ? "" : metricKey.toLowerCase(Locale.ROOT);
        return switch (key) {
            // count metrics (display as whole number + '회')
            case "checkin", "checkout", "stay_guest_count", "guest_count", "inquiry_count", "claim_count", "reservation_count" ->
                String.format("%,d회", value.setScale(0, RoundingMode.HALF_UP).longValue());

            // currency (ADR) - keep two decimal places
            case "avg_daily_rate", "adr" ->
                String.format("%,.2f원", value.setScale(2, RoundingMode.HALF_UP).doubleValue());

            // average response time (hours) - two decimals
            case "avg_response_time" ->
                String.format("%,.2f시간", value.setScale(2, RoundingMode.HALF_UP).doubleValue());

            // percentage metrics (one decimal + '%')
            case "occ_rate", "repeat_rate", "unresolved_rate", "membership_rate", "foreign_rate", "cancellation_rate", "no_show_rate", "non_room_revenue" ->
                value.setScale(1, RoundingMode.HALF_UP).toPlainString() + "%";

            // fallback: show 2 decimal places if fractional, otherwise integer with grouping
            default -> {
                int scale = value.stripTrailingZeros().scale();
                if (scale > 0) {
                    yield String.format("%,.2f", value.setScale(2, RoundingMode.HALF_UP).doubleValue());
                } else {
                    yield String.format("%,d", value.setScale(0, RoundingMode.HALF_UP).longValue());
                }
            }
        };
    }

    private String normalizeToKpiCode(String widgetKey) {
        if (widgetKey == null) return null;
        // Map common widget keys / aliases to the canonical KPI codes stored in reportkpitarget.kpi_code
        return switch(widgetKey.toUpperCase(Locale.ROOT)) {
            case "STAY_GUEST_COUNT", "GUEST_COUNT", "STAY_GUESTS", "GUESTS" -> "GUEST_COUNT";
            case "REPEAT_RATE", "REPEAT", "REPEAT_CUSTOMER_RATE" -> "REPEAT_RATE";
            case "ADR", "AVG_DAILY_RATE", "AVERAGE_DAILY_RATE" -> "ADR";
            case "AVG_RESPONSE_TIME", "AVERAGE_RESPONSE_TIME", "RESPONSE_TIME" -> "AVG_RESPONSE_TIME";
            case "CANCELLATION_RATE", "CANCEL_RATE", "CANCELLED_RATE", "CANCELLATION" -> "CANCELLATION_RATE";
            case "CHECKIN", "CHECKIN_COUNT" -> "CHECKIN";
            case "CHECKOUT", "CHECKOUT_COUNT" -> "CHECKOUT";
            case "CLAIM_COUNT", "CLAIMS" -> "CLAIM_COUNT";
            case "FOREIGN_RATE", "FOREIGN", "FOREIGN_CUSTOMER_RATE" -> "FOREIGN_RATE";
            case "INQUIRY_COUNT", "INQUIRIES", "INQUIRY", "TOTAL_INQUIRY_COUNT" -> "INQUIRY_COUNT";
            case "MEMBERSHIP_RATE", "MEMBERSHIP" -> "MEMBERSHIP_RATE";
            case "NON_ROOM_REVENUE", "NON_ROOM_SALES", "NONROOM_REVENUE", "NON_ROOM", "NON_ROOM_REVENUE_RATIO" -> "NON_ROOM_REVENUE";
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
            case "REPEAT_RATE", "REPEAT", "REPEAT_CUSTOMER_RATE" -> "repeat_rate";
            // Reservation count
            case "RESERVATION_COUNT", "RESERVATIONS", "RESERVATION" -> "reservation_count";
            // Cancellation
            case "CANCELLATION_RATE", "CANCEL_RATE", "CANCELLED_RATE", "CANCELLATION" -> "cancellation_rate";
            // No-show
            case "NO_SHOW_RATE", "NOSHOW_RATE", "NO_SHOW" -> "no_show_rate";
            // Inquiry
            case "INQUIRY_COUNT", "INQUIRIES", "INQUIRY", "TOTAL_INQUIRY_COUNT" -> "inquiry_count";
            // Unresolved
            case "UNRESOLVED_RATE", "UNRESOLVED" -> "unresolved_rate";
            // Claim
            case "CLAIM_COUNT", "CLAIMS" -> "claim_count";
            // Foreign rate
            case "FOREIGN_RATE", "FOREIGN", "FOREIGN_CUSTOMER_RATE" -> "foreign_rate";
            // Membership rate
            case "MEMBERSHIP_RATE", "MEMBERSHIP" -> "membership_rate";
            // Non-room revenue
            case "NON_ROOM_REVENUE", "NON_ROOM_SALES", "NONROOM_REVENUE", "NON_ROOM", "NON_ROOM_REVENUE_RATIO" -> "non_room_revenue";
            default -> widgetKey.toLowerCase(Locale.ROOT);
        };
    }

    @Override
    public MetricTimeSeries queryMetricTimeSeries(String metricKey, String period, Map<String, Object> filter) {
        // 로그: 진입
        log.debug("queryMetricTimeSeries called: metricKey={}, period={}, filter={}", metricKey, period, filter);

        // 1) 기간 범위 계산: year 단위이면 월별 레이블 생성, month 단위이면 일별 레이블(간단화 위해 month->일 처리 생략 가능)
        LocalDate start;
        LocalDate end;
        try {
            LocalDate[] range = computeRangeFromPeriod(period);
            start = range[0];
            end = range[1];
            log.debug("Computed date range for timeseries: period={}, start={}, end={}", period, start, end);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("invalid period for time series: " + period);
        }

        // 2) 내부 메트릭 키로 정규화
        String internalKey = normalizeToInternalKey(metricKey);

        // 3) 샘플 SQL: 연간(period=YYYY)인 경우 월별 집계
        // DB가 MySQL이라고 가정하고 DATE_FORMAT/STR_TO_DATE 등을 사용합니다. (다른 DB인 경우 적절히 수정 필요)
        // 주의: end는 computeRangeFromPeriod에서 inclusive 끝날로 반환되므로 SQL에서는 end+1 day(Exclusive) 로 사용
        LocalDate endExclusive = end.plusDays(1);
        Date dStart = Date.valueOf(start);
        Date dEndExclusive = Date.valueOf(endExclusive);

        // labels 생성 (연도 -> 1월..12월)
        List<String> labels = null;
        List<java.math.BigDecimal> values = null;

        if (period != null && period.matches("\\d{4}")) {
            int year = Integer.parseInt(period);
            labels = new java.util.ArrayList<>() ;
            for (int m = 1; m <= 12; m++) labels.add(m + "월");

            // 샘플 쿼리: 월별 합계/평균을 구하는 예시 (metric 별로 집계 방식 달라짐)
            // 예: CHECKIN/COUNT 계열은 월별 COUNT, ADR은 월별 평균
            String sql = "";
            switch(internalKey) {
                case "checkin":
                    // 월별 체크인 수
                    sql = "SELECT DATE_FORMAT(recorded_at, '%m') AS month, COUNT(*) as cnt "
                        + "FROM checkinout WHERE record_type='CHECK_IN' AND recorded_at >= ? AND recorded_at < ? GROUP BY month ORDER BY month";
                    break;
                case "checkout":
                    sql = "SELECT DATE_FORMAT(recorded_at, '%m') AS month, COUNT(*) as cnt "
                        + "FROM checkinout WHERE record_type='CHECK_OUT' AND recorded_at >= ? AND recorded_at < ? GROUP BY month ORDER BY month";
                    break;
                case "adr":
                    // ADR의 경우 예시: 월별 총매출 / 점유박수
                    sql = "SELECT DATE_FORMAT(checkin_date, '%m') AS month, ROUND(COALESCE(SUM(reservation_room_price),0) / NULLIF(COALESCE(SUM(GREATEST(DATEDIFF(LEAST(checkout_date, ?), GREATEST(checkin_date, ?)),0)),0),0),2) as val "
                        + "FROM reservation WHERE checkin_date < ? AND checkout_date > ? GROUP BY month ORDER BY month";
                    break;
                case "occ_rate":
                case "occupancy":
                case "occupancy_rate":
                    // 점유율은 월별 점유박수 / (총객실수 * daysInMonth)
                    sql = "SELECT DATE_FORMAT(checkin_date, '%m') AS month, 0 as val FROM reservation WHERE checkin_date < ? AND checkout_date > ? GROUP BY month ORDER BY month"; // placeholder
                    break;
                default:
                    // 기본: 빈 값 리턴
                    labels = new java.util.ArrayList<>() ;
                    values = new java.util.ArrayList<>() ;
            }

            if (sql != null && !sql.isEmpty()) {
                try {
                    // JDBC 결과를 List<Map>으로 조회하여 월별 값 맵을 구성합니다.
                    java.util.Map<String, java.math.BigDecimal> monthMap = new java.util.HashMap<>();
                    java.util.List<java.util.Map<String,Object>> rows = jdbc.queryForList(sql, dStart, dEndExclusive);
                    for (java.util.Map<String,Object> row : rows) {
                        // SQL에서 첫 칼럼은 month, 두번째 칼럼은 val로 가정합니다.
                        String month = String.valueOf(row.get("month"));
                        Object valObj = row.get("val") != null ? row.get("val") : row.get("cnt");
                        java.math.BigDecimal v = null;
                        if (valObj instanceof java.math.BigDecimal) v = (java.math.BigDecimal) valObj;
                        else if (valObj instanceof Number) v = java.math.BigDecimal.valueOf(((Number) valObj).doubleValue());
                        monthMap.put(month, v);
                    }
                     values = new java.util.ArrayList<>();
                     for (int m = 1; m <= 12; m++) {
                         String mm = String.format("%02d", m);
                         values.add(monthMap.getOrDefault(mm, null));
                     }
                 } catch (Exception ex) {
                     log.warn("Failed to build monthly timeseries for metricKey={}: {}", metricKey, ex.getMessage());
                 }
             }
         }

         // MetricTimeSeries 객체 생성
         MetricTimeSeries mts = new MetricTimeSeries();
         mts.setLabels(labels);
         if (values != null) {
             MetricTimeSeries.Series s = new MetricTimeSeries.Series();
             s.setName("actual");
             s.setData(values);
             mts.setSeries(java.util.Collections.singletonList(s));
         }

         return mts;
     }
}