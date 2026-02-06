package com.gaekdam.gaekdambe.analytics_service.report.dataset.query.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.YearMonth;
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

import com.gaekdam.gaekdambe.analytics_service.report.dashboard.query.dto.ChartWidgetDto;
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
                // KPI ADR 계산과 동일한 방식으로: 기간 내 점유박수에 대한 총요금 / 점유박수
                java.math.BigDecimal totalRevenue;
                Integer occupiedNights;
                if (hotelId != null) {
                    totalRevenue = jdbc.queryForObject(
                        "SELECT COALESCE(SUM(r.reservation_room_price * GREATEST(DATEDIFF(LEAST(r.checkout_date, ?), GREATEST(r.checkin_date, ?)),0)),0) FROM reservation r WHERE r.checkin_date < ? AND r.checkout_date > ? AND r.canceled_at IS NULL AND r.property_code = ?",
                        java.math.BigDecimal.class, dEnd, dStart, dEnd, dStart, hotelId);
                    occupiedNights = jdbc.queryForObject(
                        "SELECT COALESCE(SUM(GREATEST(DATEDIFF(LEAST(r.checkout_date, ?), GREATEST(r.checkin_date, ?)),0)),0) FROM reservation r WHERE r.checkin_date < ? AND r.checkout_date > ? AND r.canceled_at IS NULL AND r.property_code = ?",
                        Integer.class, dEnd, dStart, dEnd, dStart, hotelId);
                } else if (hotelGroup != null) {
                    totalRevenue = jdbc.queryForObject(
                        "SELECT COALESCE(SUM(r.reservation_room_price * GREATEST(DATEDIFF(LEAST(r.checkout_date, ?), GREATEST(r.checkin_date, ?)),0)),0) FROM reservation r JOIN property p ON r.property_code = p.property_code WHERE r.checkin_date < ? AND r.checkout_date > ? AND r.canceled_at IS NULL AND p.hotel_group_code = ?",
                        java.math.BigDecimal.class, dEnd, dStart, dEnd, dStart, hotelGroup);
                    occupiedNights = jdbc.queryForObject(
                        "SELECT COALESCE(SUM(GREATEST(DATEDIFF(LEAST(r.checkout_date, ?), GREATEST(r.checkin_date, ?)),0)),0) FROM reservation r JOIN property p ON r.property_code = p.property_code WHERE r.checkin_date < ? AND r.checkout_date > ? AND r.canceled_at IS NULL AND p.hotel_group_code = ?",
                        Integer.class, dEnd, dStart, dEnd, dStart, hotelGroup);
                } else {
                    totalRevenue = jdbc.queryForObject(
                        "SELECT COALESCE(SUM(r.reservation_room_price * GREATEST(DATEDIFF(LEAST(r.checkout_date, ?), GREATEST(r.checkin_date, ?)),0)),0) FROM reservation r WHERE r.checkin_date < ? AND r.checkout_date > ? AND r.canceled_at IS NULL",
                        java.math.BigDecimal.class, dEnd, dStart, dEnd, dStart);
                    occupiedNights = jdbc.queryForObject(
                        "SELECT COALESCE(SUM(GREATEST(DATEDIFF(LEAST(r.checkout_date, ?), GREATEST(r.checkin_date, ?)),0)),0) FROM reservation r WHERE r.checkin_date < ? AND r.checkout_date > ? AND r.canceled_at IS NULL",
                        Integer.class, dEnd, dStart, dEnd, dStart);
                }

                if (totalRevenue == null) totalRevenue = java.math.BigDecimal.ZERO;
                if (occupiedNights == null || occupiedNights == 0) return BigDecimal.ZERO;
                java.math.BigDecimal adrResult = totalRevenue.divide(java.math.BigDecimal.valueOf(occupiedNights), 2, RoundingMode.HALF_UP);
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
                        "SELECT COALESCE(SUM(GREATEST(DATEDIFF(LEAST(r.checkout_date, ?), GREATEST(r.checkin_date, ?)),0)),0) FROM reservation r WHERE r.checkin_date < ? AND r.checkout_date > ? AND r.canceled_at IS NULL AND r.property_code = ?",
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
            // Accept several aliases for non-room revenue (부대시설 매출 비율). Also map FACILITY_REVENUE_RATIO to the same KPI code.
            case "NON_ROOM_REVENUE", "NON_ROOM_SALES", "NONROOM_REVENUE", "NON_ROOM", "NON_ROOM_REVENUE_RATIO", "FACILITY_REVENUE_RATIO" -> "NON_ROOM_REVENUE";
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
            // Non-room revenue (ratio KPI) - include FACILITY_REVENUE_RATIO as an alias so it uses non_room_revenue computation
            case "NON_ROOM_REVENUE", "NON_ROOM_SALES", "NONROOM_REVENUE", "NON_ROOM", "NON_ROOM_REVENUE_RATIO", "FACILITY_REVENUE_RATIO" -> "non_room_revenue";
            // Facility revenue (absolute sales) used by timeseries widget
            case "FACILITY_REVENUE" -> "facility_revenue";
            default -> widgetKey.toLowerCase(Locale.ROOT);
        };
    }

    @Override
    public MetricTimeSeries queryMetricTimeSeries(String metricKey, String period, Map<String, Object> filter) {
        // 로그: 진입
        log.debug("queryMetricTimeSeries called: metricKey={}, period={}, filter={}", metricKey, period, filter);

        // 1) 기간 범위 계산: 연간(월별 bucket) 또는 월간(일별 bucket)
        LocalDate start;
        LocalDate end;
        boolean isYear = false;
        boolean isMonth = false;
        try {
            if (period != null && period.matches("\\d{4}")) {
                // 연간: YYYY
                int y = Integer.parseInt(period);
                start = LocalDate.of(y, 1, 1);
                end = LocalDate.of(y, 12, 31);
                isYear = true;
            } else if (period != null && period.matches("\\d{4}-\\d{1,2}")) {
                // 월간: YYYY-MM
                YearMonth ym = YearMonth.parse(period);
                start = ym.atDay(1);
                end = ym.atEndOfMonth();
                isMonth = true;
            } else {
                throw new IllegalArgumentException("unsupported period: " + period);
            }
            log.debug("Computed date range for timeseries: period={}, start={}, end={}", period, start, end);
        } catch (Exception ex) {
            throw new IllegalArgumentException("invalid period for time series: " + period);
        }

        // 2) 내부 메트릭 키로 정규화
        String internalKey = normalizeToInternalKey(metricKey);

        // 3) JDBC 사용을 위한 날짜 값 준비 (end는 inclusive였으므로 exclusive bound 계산)
        LocalDate endExclusive = end.plusDays(1);
        Date dStart = Date.valueOf(start);
        Date dEndExclusive = Date.valueOf(endExclusive);
        // recorded_at 비교용으로 Timestamp도 준비 (DB의 timestamp 칼럼 비교에 사용)
        Timestamp tsStart = Timestamp.valueOf(start.atStartOfDay());
        Timestamp tsEnd = Timestamp.valueOf(endExclusive.atStartOfDay());

        // 결과용 라벨/값 리스트 초기화
        List<String> labels = new java.util.ArrayList<>();
        List<java.math.BigDecimal> values = new java.util.ArrayList<>();

        // 연간: 12개월 레이블(1월..12월), 월간: 1일..N일 레이블
        if (isYear) {
            for (int m = 1; m <= 12; m++) labels.add(m + "월");
        } else if (isMonth) {
            YearMonth ym = YearMonth.from(start);
            int days = ym.lengthOfMonth();
            for (int d = 1; d <= days; d++) labels.add(d + "일");
        }

        // 공통 필터: hotelGroup / hotelId
        Object hotelGroup = filter != null ? filter.get("hotelGroupCode") : null;
        Object hotelId = filter != null ? filter.get("hotelId") : null; // property_code

        // 4) DB에서 집계 조회: metric별로 적절한 그룹화 SQL 사용 (월별은 DATE_FORMAT(...,'%m'), 일별은 DATE_FORMAT(...,'%d'))
        try {
            String sql = null;

            if (isYear) {
                // 월별 집계: prepare sql/params or compute directly for complex metrics
                boolean needQuery = true;
                Object[] params = null;

                switch (internalKey) {
                    case "checkin":
                        if (hotelId != null) {
                            sql = "SELECT DATE_FORMAT(c.recorded_at, '%m') AS bucket, COUNT(*) AS val "
                                    + "FROM checkinout c JOIN stay s ON c.stay_code = s.stay_code JOIN reservation r ON s.reservation_code = r.reservation_code "
                                    + "WHERE c.record_type='CHECK_IN' AND c.recorded_at >= ? AND c.recorded_at < ? AND r.property_code = ? GROUP BY bucket ORDER BY bucket";
                            params = new Object[]{tsStart, tsEnd, hotelId};
                        } else if (hotelGroup != null) {
                            sql = "SELECT DATE_FORMAT(c.recorded_at, '%m') AS bucket, COUNT(*) AS val "
                                    + "FROM checkinout c JOIN stay s ON c.stay_code = s.stay_code JOIN reservation r ON s.reservation_code = r.reservation_code JOIN property p ON r.property_code = p.property_code "
                                    + "WHERE c.record_type='CHECK_IN' AND c.recorded_at >= ? AND c.recorded_at < ? AND p.hotel_group_code = ? GROUP BY bucket ORDER BY bucket";
                            params = new Object[]{tsStart, tsEnd, hotelGroup};
                        } else {
                            sql = "SELECT DATE_FORMAT(recorded_at, '%m') AS bucket, COUNT(*) AS val "
                                    + "FROM checkinout WHERE record_type='CHECK_IN' AND recorded_at >= ? AND recorded_at < ? GROUP BY bucket ORDER BY bucket";
                            params = new Object[]{tsStart, tsEnd};
                        }
                        break;
                    case "checkout":
                        if (hotelId != null) {
                            sql = "SELECT DATE_FORMAT(c.recorded_at, '%m') AS bucket, COUNT(*) AS val "
                                    + "FROM checkinout c JOIN stay s ON c.stay_code = s.stay_code JOIN reservation r ON s.reservation_code = r.reservation_code "
                                    + "WHERE c.record_type='CHECK_OUT' AND c.recorded_at >= ? AND c.recorded_at < ? AND r.property_code = ? GROUP BY bucket ORDER BY bucket";
                            params = new Object[]{tsStart, tsEnd, hotelId};
                        } else if (hotelGroup != null) {
                            sql = "SELECT DATE_FORMAT(c.recorded_at, '%m') AS bucket, COUNT(*) AS val "
                                    + "FROM checkinout c JOIN stay s ON c.stay_code = s.stay_code JOIN reservation r ON s.reservation_code = r.reservation_code JOIN property p ON r.property_code = p.property_code "
                                    + "WHERE c.record_type='CHECK_OUT' AND c.recorded_at >= ? AND c.recorded_at < ? AND p.hotel_group_code = ? GROUP BY bucket ORDER BY bucket";
                            params = new Object[]{tsStart, tsEnd, hotelGroup};
                        } else {
                            sql = "SELECT DATE_FORMAT(recorded_at, '%m') AS bucket, COUNT(*) AS val "
                                    + "FROM checkinout WHERE record_type='CHECK_OUT' AND recorded_at >= ? AND recorded_at < ? GROUP BY bucket ORDER BY bucket";
                            params = new Object[]{tsStart, tsEnd};
                        }
                        break;
                    case "avg_daily_rate":
                        // ADR monthly - compute per month
                        needQuery = false;
                        int year = start.getYear();
                        for (int m = 1; m <= 12; m++) {
                            LocalDate ms = LocalDate.of(year, m, 1);
                            LocalDate meExclusive = ms.plusMonths(1);
                            Date msDate = Date.valueOf(ms);
                            Date meExDate = Date.valueOf(meExclusive);

                            java.math.BigDecimal numerator = jdbc.queryForObject(
                                    "SELECT COALESCE(SUM(r.reservation_room_price * GREATEST(DATEDIFF(LEAST(r.checkout_date, ?), GREATEST(r.checkin_date, ?)),0)),0) FROM reservation r" +
                                            (hotelGroup != null ? " JOIN property p ON r.property_code = p.property_code" : "") +
                                            " WHERE r.checkin_date < ? AND r.checkout_date > ? AND r.canceled_at IS NULL" +
                                            (hotelId != null ? " AND r.property_code = ?" : (hotelGroup != null ? " AND p.hotel_group_code = ?" : "")),
                                    java.math.BigDecimal.class,
                                    hotelId != null ? new Object[]{meExDate, msDate, meExDate, msDate, hotelId} : (hotelGroup != null ? new Object[]{meExDate, msDate, meExDate, msDate, hotelGroup} : new Object[]{meExDate, msDate, meExDate, msDate})
                            );

                            Integer denomNights = jdbc.queryForObject(
                                    "SELECT COALESCE(SUM(GREATEST(DATEDIFF(LEAST(r.checkout_date, ?), GREATEST(r.checkin_date, ?)),0)),0) FROM reservation r" +
                                            (hotelGroup != null ? " JOIN property p ON r.property_code = p.property_code" : "") +
                                            " WHERE r.checkin_date < ? AND r.checkout_date > ? AND r.canceled_at IS NULL" +
                                            (hotelId != null ? " AND r.property_code = ?" : (hotelGroup != null ? " AND p.hotel_group_code = ?" : "")),
                                    Integer.class,
                                    hotelId != null ? new Object[]{meExDate, msDate, meExDate, msDate, hotelId} : (hotelGroup != null ? new Object[]{meExDate, msDate, meExDate, msDate, hotelGroup} : new Object[]{meExDate, msDate, meExDate, msDate})
                            );

                            log.debug("ADR timeseries (monthly) bucket={} numerator={} denom={} hotelId={} hotelGroup={}", ms, numerator, denomNights, hotelId, hotelGroup);

                            if (numerator == null) numerator = java.math.BigDecimal.ZERO;
                            if (denomNights == null || denomNights == 0) {
                                values.add(null);
                            } else {
                                java.math.BigDecimal adrValue = numerator.divide(java.math.BigDecimal.valueOf(denomNights), 2, RoundingMode.HALF_UP);
                                values.add(adrValue);
                            }
                        }
                        break;
                    case "occ_rate":
                        needQuery = false;
                        int y = start.getYear();
                        for (int m = 1; m <= 12; m++) {
                            LocalDate ms = LocalDate.of(y, m, 1);
                            LocalDate meExclusive = ms.plusMonths(1);
                            Date msDate = Date.valueOf(ms);
                            Date meExDate = Date.valueOf(meExclusive);

                            Integer occupiedNights = null;
                            if (hotelId != null) {
                                occupiedNights = jdbc.queryForObject(
                                        "SELECT COALESCE(SUM(GREATEST(DATEDIFF(LEAST(r.checkout_date, ?), GREATEST(r.checkin_date, ?)),0)),0) FROM reservation r WHERE r.checkin_date < ? AND r.checkout_date > ? AND r.canceled_at IS NULL AND r.property_code = ?",
                                        Integer.class, meExDate, msDate, meExDate, msDate, hotelId);
                            } else if (hotelGroup != null) {
                                occupiedNights = jdbc.queryForObject(
                                        "SELECT COALESCE(SUM(GREATEST(DATEDIFF(LEAST(r.checkout_date, ?), GREATEST(r.checkin_date, ?)),0)),0) FROM reservation r JOIN property p ON r.property_code = p.property_code WHERE r.checkin_date < ? AND r.checkout_date > ? AND r.canceled_at IS NULL AND p.hotel_group_code = ?",
                                        Integer.class, meExDate, msDate, meExDate, msDate, hotelGroup);
                            } else {
                                occupiedNights = jdbc.queryForObject(
                                        "SELECT COALESCE(SUM(GREATEST(DATEDIFF(LEAST(r.checkout_date, ?), GREATEST(r.checkin_date, ?)),0)),0) FROM reservation r WHERE r.checkin_date < ? AND r.checkout_date > ? AND r.canceled_at IS NULL",
                                        Integer.class, meExDate, msDate, meExDate, msDate);
                            }

                            Integer totalRooms;
                            if (hotelId != null) {
                                totalRooms = jdbc.queryForObject(
                                        "SELECT COALESCE(COUNT(rm.room_code),0) FROM room rm JOIN room_type rt ON rm.room_type_code = rt.room_type_code WHERE rt.property_code = ? AND rm.room_status = 'ACTIVE'",
                                        Integer.class, hotelId);
                            } else if (hotelGroup != null) {
                                totalRooms = jdbc.queryForObject(
                                        "SELECT COALESCE(COUNT(rm.room_code),0) FROM room rm JOIN room_type rt ON rm.room_type_code = rt.room_type_code JOIN property p ON rt.property_code = p.property_code WHERE p.hotel_group_code = ? AND rm.room_status = 'ACTIVE'",
                                        Integer.class, hotelGroup);
                            } else {
                                totalRooms = jdbc.queryForObject(
                                        "SELECT COALESCE(COUNT(rm.room_code),0) FROM room rm WHERE rm.room_status = 'ACTIVE'",
                                        Integer.class);
                            }

                            int daysInMonth = ms.lengthOfMonth();
                            java.math.BigDecimal occValue = java.math.BigDecimal.ZERO;
                            if (totalRooms != null && totalRooms > 0) {
                                java.math.BigDecimal availableRoomNights = java.math.BigDecimal.valueOf((long) totalRooms * daysInMonth);
                                occValue = java.math.BigDecimal.valueOf(occupiedNights != null ? occupiedNights : 0)
                                        .divide(availableRoomNights, 4, RoundingMode.HALF_UP)
                                        .multiply(java.math.BigDecimal.valueOf(100));
                            }
                            values.add(occValue);
                        }
                        break;
                    case "inquiry_count":
                        if (hotelId != null) {
                            sql = "SELECT DATE_FORMAT(i.created_at, '%m') AS bucket, COUNT(i.inquiry_code) AS val "
                                    + "FROM inquiry i WHERE i.created_at >= ? AND i.created_at < ? AND i.property_code = ? GROUP BY bucket ORDER BY bucket";
                            params = new Object[]{tsStart, tsEnd, hotelId};
                        } else if (hotelGroup != null) {
                            sql = "SELECT DATE_FORMAT(i.created_at, '%m') AS bucket, COUNT(i.inquiry_code) AS val "
                                    + "FROM inquiry i JOIN property p ON i.property_code = p.property_code WHERE i.created_at >= ? AND i.created_at < ? AND p.hotel_group_code = ? GROUP BY bucket ORDER BY bucket";
                            params = new Object[]{tsStart, tsEnd, hotelGroup};
                        } else {
                            sql = "SELECT DATE_FORMAT(i.created_at, '%m') AS bucket, COUNT(i.inquiry_code) AS val "
                                    + "FROM inquiry i WHERE i.created_at >= ? AND i.created_at < ? GROUP BY bucket ORDER BY bucket";
                            params = new Object[]{tsStart, tsEnd};
                        }
                        break;
                    case "claim_count":
                        if (hotelId != null) {
                            sql = "SELECT DATE_FORMAT(i.created_at, '%m') AS bucket, COUNT(i.inquiry_code) AS val "
                                    + "FROM inquiry i WHERE i.inquiry_category_code = 2 AND i.created_at >= ? AND i.created_at < ? AND i.property_code = ? GROUP BY bucket ORDER BY bucket";
                            params = new Object[]{tsStart, tsEnd, hotelId};
                        } else if (hotelGroup != null) {
                            sql = "SELECT DATE_FORMAT(i.created_at, '%m') AS bucket, COUNT(i.inquiry_code) AS val "
                                    + "FROM inquiry i JOIN property p ON i.property_code = p.property_code WHERE i.inquiry_category_code = 2 AND i.created_at >= ? AND i.created_at < ? AND p.hotel_group_code = ? GROUP BY bucket ORDER BY bucket";
                            params = new Object[]{tsStart, tsEnd, hotelGroup};
                        } else {
                            sql = "SELECT DATE_FORMAT(i.created_at, '%m') AS bucket, COUNT(i.inquiry_code) AS val "
                                    + "FROM inquiry i WHERE i.inquiry_category_code = 2 AND i.created_at >= ? AND i.created_at < ? GROUP BY bucket ORDER BY bucket";
                            params = new Object[]{tsStart, tsEnd};
                        }
                        break;
                    // 예약 수 (created_at 기준 월별 집계)
                    case "reservation_count":
                        if (hotelId != null) {
                            sql = "SELECT DATE_FORMAT(r.created_at, '%m') AS bucket, COUNT(r.reservation_code) AS val FROM reservation r WHERE r.created_at >= ? AND r.created_at < ? AND r.property_code = ? GROUP BY bucket ORDER BY bucket";
                            params = new Object[]{tsStart, tsEnd, hotelId};
                        } else if (hotelGroup != null) {
                            sql = "SELECT DATE_FORMAT(r.created_at, '%m') AS bucket, COUNT(r.reservation_code) AS val FROM reservation r JOIN property p ON r.property_code = p.property_code WHERE r.created_at >= ? AND r.created_at < ? AND p.hotel_group_code = ? GROUP BY bucket ORDER BY bucket";
                            params = new Object[]{tsStart, tsEnd, hotelGroup};
                        } else {
                            sql = "SELECT DATE_FORMAT(r.created_at, '%m') AS bucket, COUNT(r.reservation_code) AS val FROM reservation r WHERE r.created_at >= ? AND r.created_at < ? GROUP BY bucket ORDER BY bucket";
                            params = new Object[]{tsStart, tsEnd};
                        }
                        break;
                    // 예약 취소수 (canceled_at 기준 월별 집계)
                    case "cancel_count":
                        if (hotelId != null) {
                            sql = "SELECT DATE_FORMAT(r.canceled_at, '%m') AS bucket, COUNT(r.reservation_code) AS val FROM reservation r WHERE r.canceled_at >= ? AND r.canceled_at < ? AND r.canceled_at IS NOT NULL AND r.property_code = ? GROUP BY bucket ORDER BY bucket";
                            params = new Object[]{tsStart, tsEnd, hotelId};
                        } else if (hotelGroup != null) {
                            sql = "SELECT DATE_FORMAT(r.canceled_at, '%m') AS bucket, COUNT(r.reservation_code) AS val FROM reservation r JOIN property p ON r.property_code = p.property_code WHERE r.canceled_at >= ? AND r.canceled_at < ? AND r.canceled_at IS NOT NULL AND p.hotel_group_code = ? GROUP BY bucket ORDER BY bucket";
                            params = new Object[]{tsStart, tsEnd, hotelGroup};
                        } else {
                            sql = "SELECT DATE_FORMAT(r.canceled_at, '%m') AS bucket, COUNT(r.reservation_code) AS val FROM reservation r WHERE r.canceled_at >= ? AND r.canceled_at < ? AND r.canceled_at IS NOT NULL GROUP BY bucket ORDER BY bucket";
                            params = new Object[]{tsStart, tsEnd};
                        }
                        break;
                    // 노쇼 수 (checkin_date 기준 월별 집계, reservation_status='NO_SHOW')
                    case "no_show_count":
                        if (hotelId != null) {
                            sql = "SELECT DATE_FORMAT(r.checkin_date, '%m') AS bucket, COUNT(r.reservation_code) AS val FROM reservation r WHERE r.checkin_date >= ? AND r.checkin_date < ? AND r.reservation_status = 'NO_SHOW' AND r.property_code = ? GROUP BY bucket ORDER BY bucket";
                            params = new Object[]{dStart, dEndExclusive, hotelId};
                        } else if (hotelGroup != null) {
                            sql = "SELECT DATE_FORMAT(r.checkin_date, '%m') AS bucket, COUNT(r.reservation_code) AS val FROM reservation r JOIN property p ON r.property_code = p.property_code WHERE r.checkin_date >= ? AND r.checkin_date < ? AND r.reservation_status = 'NO_SHOW' AND p.hotel_group_code = ? GROUP BY bucket ORDER BY bucket";
                            params = new Object[]{dStart, dEndExclusive, hotelGroup};
                        } else {
                            sql = "SELECT DATE_FORMAT(r.checkin_date, '%m') AS bucket, COUNT(r.reservation_code) AS val FROM reservation r WHERE r.checkin_date >= ? AND r.checkin_date < ? AND r.reservation_status = 'NO_SHOW' GROUP BY bucket ORDER BY bucket";
                            params = new Object[]{dStart, dEndExclusive};
                        }
                        break;
                    // 부대시설 매출 (checkin_date 기준, reservation_package_price 합계)
                    case "facility_revenue":
                        if (hotelId != null) {
                            sql = "SELECT DATE_FORMAT(r.checkin_date, '%m') AS bucket, COALESCE(SUM(COALESCE(r.reservation_package_price,0)),0) AS val FROM reservation r WHERE r.checkin_date >= ? AND r.checkin_date < ? AND r.canceled_at IS NULL AND r.property_code = ? GROUP BY bucket ORDER BY bucket";
                            params = new Object[]{dStart, dEndExclusive, hotelId};
                        } else if (hotelGroup != null) {
                            sql = "SELECT DATE_FORMAT(r.checkin_date, '%m') AS bucket, COALESCE(SUM(COALESCE(r.reservation_package_price,0)),0) AS val FROM reservation r JOIN property p ON r.property_code = p.property_code WHERE r.checkin_date >= ? AND r.checkin_date < ? AND r.canceled_at IS NULL AND p.hotel_group_code = ? GROUP BY bucket ORDER BY bucket";
                            params = new Object[]{dStart, dEndExclusive, hotelGroup};
                        } else {
                            sql = "SELECT DATE_FORMAT(r.checkin_date, '%m') AS bucket, COALESCE(SUM(COALESCE(r.reservation_package_price,0)),0) AS val FROM reservation r WHERE r.checkin_date >= ? AND r.checkin_date < ? AND r.canceled_at IS NULL GROUP BY bucket ORDER BY bucket";
                            params = new Object[]{dStart, dEndExclusive};
                        }
                        break;
                }

                java.util.List<java.util.Map<String, Object>> rowsLocal = java.util.Collections.emptyList();
                if (needQuery && sql != null) {
                    log.debug("Timeseries SQL (internalKey={}) sql={} paramsPresent={}", internalKey, sql, params != null);
                    rowsLocal = params != null ? jdbc.queryForList(sql, params) : jdbc.queryForList(sql);
                }

                java.util.Map<String, java.math.BigDecimal> bucketMap = new java.util.HashMap<>();
                for (java.util.Map<String, Object> row : rowsLocal) {
                    String bucket = String.valueOf(row.get("bucket"));
                    Object valObj = row.get("val");
                    java.math.BigDecimal v = null;
                    if (valObj instanceof java.math.BigDecimal) v = (java.math.BigDecimal) valObj;
                    else if (valObj instanceof Number) v = java.math.BigDecimal.valueOf(((Number) valObj).doubleValue());
                    bucketMap.put(bucket, v);
                }

                // fill values for months 01..12 (skip if values already filled by complex metric computation)
                for (int m = 1; m <= 12; m++) {
                    String mm = String.format("%02d", m);
                    if (("occ_rate".equals(internalKey) || "avg_daily_rate".equals(internalKey) || "adr".equals(internalKey)) && values.size() == 12) continue;
                    values.add(bucketMap.getOrDefault(mm, null));
                }

            } else if (isMonth) {
                // 일별 집계
                YearMonth ym = YearMonth.from(start);
                int days = ym.lengthOfMonth();

                boolean needQuery = true;
                Object[] params = null;

                switch (internalKey) {
                    case "checkin":
                        if (hotelId != null) {
                            sql = "SELECT DATE_FORMAT(c.recorded_at, '%d') AS bucket, COUNT(*) AS val "
                                    + "FROM checkinout c JOIN stay s ON c.stay_code = s.stay_code JOIN reservation r ON s.reservation_code = r.reservation_code "
                                    + "WHERE c.record_type='CHECK_IN' AND c.recorded_at >= ? AND c.recorded_at < ? AND r.property_code = ? GROUP BY bucket ORDER BY bucket";
                            params = new Object[]{tsStart, tsEnd, hotelId};
                        } else if (hotelGroup != null) {
                            sql = "SELECT DATE_FORMAT(c.recorded_at, '%d') AS bucket, COUNT(*) AS val "
                                    + "FROM checkinout c JOIN stay s ON c.stay_code = s.stay_code JOIN reservation r ON s.reservation_code = r.reservation_code JOIN property p ON r.property_code = p.property_code "
                                    + "WHERE c.record_type='CHECK_IN' AND c.recorded_at >= ? AND c.recorded_at < ? AND p.hotel_group_code = ? GROUP BY bucket ORDER BY bucket";
                            params = new Object[]{tsStart, tsEnd, hotelGroup};
                        } else {
                            sql = "SELECT DATE_FORMAT(recorded_at, '%d') AS bucket, COUNT(*) AS val "
                                    + "FROM checkinout WHERE record_type='CHECK_IN' AND recorded_at >= ? AND recorded_at < ? GROUP BY bucket ORDER BY bucket";
                            params = new Object[]{tsStart, tsEnd};
                        }
                        break;
                    case "checkout":
                        if (hotelId != null) {
                            sql = "SELECT DATE_FORMAT(c.recorded_at, '%d') AS bucket, COUNT(*) AS val "
                                    + "FROM checkinout c JOIN stay s ON c.stay_code = s.stay_code JOIN reservation r ON s.reservation_code = r.reservation_code "
                                    + "WHERE c.record_type='CHECK_OUT' AND c.recorded_at >= ? AND c.recorded_at < ? AND r.property_code = ? GROUP BY bucket ORDER BY bucket";
                            params = new Object[]{tsStart, tsEnd, hotelId};
                        } else if (hotelGroup != null) {
                            sql = "SELECT DATE_FORMAT(c.recorded_at, '%d') AS bucket, COUNT(*) AS val "
                                    + "FROM checkinout c JOIN stay s ON c.stay_code = s.stay_code JOIN reservation r ON s.reservation_code = r.reservation_code JOIN property p ON r.property_code = p.property_code "
                                    + "WHERE c.record_type='CHECK_OUT' AND c.recorded_at >= ? AND c.recorded_at < ? AND p.hotel_group_code = ? GROUP BY bucket ORDER BY bucket";
                            params = new Object[]{tsStart, tsEnd, hotelGroup};
                        } else {
                            sql = "SELECT DATE_FORMAT(recorded_at, '%d') AS bucket, COUNT(*) AS val "
                                    + "FROM checkinout WHERE record_type='CHECK_OUT' AND recorded_at >= ? AND recorded_at < ? GROUP BY bucket ORDER BY bucket";
                            params = new Object[]{tsStart, tsEnd};
                        }
                        break;
                    case "avg_daily_rate":
                        needQuery = false;
                        for (int d = 1; d <= days; d++) {
                            LocalDate dayStart = start.withDayOfMonth(d);
                            LocalDate dayEndExclusive = dayStart.plusDays(1);
                            Date dayStartDate = Date.valueOf(dayStart);
                            Date dayEndExDate = Date.valueOf(dayEndExclusive);

                            java.math.BigDecimal numerator;
                            Integer denomNights;
                            if (hotelId != null) {
                                numerator = jdbc.queryForObject(
                                        "SELECT COALESCE(SUM(r.reservation_room_price * GREATEST(DATEDIFF(LEAST(r.checkout_date, ?), GREATEST(r.checkin_date, ?)),0)),0) FROM reservation r WHERE r.checkin_date < ? AND r.checkout_date > ? AND r.canceled_at IS NULL AND r.property_code = ?",
                                        java.math.BigDecimal.class, dayEndExDate, dayStartDate, dayEndExDate, dayStartDate, hotelId);
                                denomNights = jdbc.queryForObject(
                                        "SELECT COALESCE(SUM(GREATEST(DATEDIFF(LEAST(r.checkout_date, ?), GREATEST(r.checkin_date, ?)),0)),0) FROM reservation r WHERE r.checkin_date < ? AND r.checkout_date > ? AND r.canceled_at IS NULL AND r.property_code = ?",
                                        Integer.class, dayEndExDate, dayStartDate, dayEndExDate, dayStartDate, hotelId);
                            } else if (hotelGroup != null) {
                                numerator = jdbc.queryForObject(
                                        "SELECT COALESCE(SUM(r.reservation_room_price * GREATEST(DATEDIFF(LEAST(r.checkout_date, ?), GREATEST(r.checkin_date, ?)),0)),0) FROM reservation r JOIN property p ON r.property_code = p.property_code WHERE r.checkin_date < ? AND r.checkout_date > ? AND r.canceled_at IS NULL AND p.hotel_group_code = ?",
                                        java.math.BigDecimal.class, dayEndExDate, dayStartDate, dayEndExDate, dayStartDate, hotelGroup);
                                denomNights = jdbc.queryForObject(
                                        "SELECT COALESCE(SUM(GREATEST(DATEDIFF(LEAST(r.checkout_date, ?), GREATEST(r.checkin_date, ?)),0)),0) FROM reservation r JOIN property p ON r.property_code = p.property_code WHERE r.checkin_date < ? AND r.checkout_date > ? AND r.canceled_at IS NULL AND p.hotel_group_code = ?",
                                        Integer.class, dayEndExDate, dayStartDate, dayEndExDate, dayStartDate, hotelGroup);
                            } else {
                                numerator = jdbc.queryForObject(
                                        "SELECT COALESCE(SUM(r.reservation_room_price * GREATEST(DATEDIFF(LEAST(r.checkout_date, ?), GREATEST(r.checkin_date, ?)),0)),0) FROM reservation r WHERE r.checkin_date < ? AND r.checkout_date > ? AND r.canceled_at IS NULL",
                                        java.math.BigDecimal.class, dayEndExDate, dayStartDate, dayEndExDate, dayStartDate);
                                denomNights = jdbc.queryForObject(
                                        "SELECT COALESCE(SUM(GREATEST(DATEDIFF(LEAST(r.checkout_date, ?), GREATEST(r.checkin_date, ?)),0)),0) FROM reservation r WHERE r.checkin_date < ? AND r.checkout_date > ? AND r.canceled_at IS NULL",
                                        Integer.class, dayEndExDate, dayStartDate, dayEndExDate, dayStartDate);
                            }

                            log.debug("ADR timeseries (daily) bucket={} numerator={} denom={} hotelId={} hotelGroup={}", dayStart, numerator, denomNights, hotelId, hotelGroup);

                            if (numerator == null) numerator = java.math.BigDecimal.ZERO;
                            if (denomNights == null || denomNights == 0) {
                                values.add(null);
                            } else {
                                java.math.BigDecimal adrValue = numerator.divide(java.math.BigDecimal.valueOf(denomNights), 2, RoundingMode.HALF_UP);
                                values.add(adrValue);
                            }
                        }
                        break;
                    case "occ_rate":
                        needQuery = false;
                        for (int d = 1; d <= days; d++) {
                            LocalDate dayStart = start.withDayOfMonth(d);
                            LocalDate dayEndExclusive = dayStart.plusDays(1);
                            Date dayStartDate = Date.valueOf(dayStart);
                            Date dayEndExDate = Date.valueOf(dayEndExclusive);

                            Integer occupiedNights;
                            if (hotelId != null) {
                                occupiedNights = jdbc.queryForObject(
                                        "SELECT COALESCE(SUM(GREATEST(DATEDIFF(LEAST(r.checkout_date, ?), GREATEST(r.checkin_date, ?)),0)),0) FROM reservation r WHERE r.checkin_date < ? AND r.checkout_date > ? AND r.canceled_at IS NULL AND r.property_code = ?",
                                        Integer.class, dayEndExDate, dayStartDate, dayEndExDate, dayStartDate, hotelId);
                            } else if (hotelGroup != null) {
                                occupiedNights = jdbc.queryForObject(
                                        "SELECT COALESCE(SUM(GREATEST(DATEDIFF(LEAST(r.checkout_date, ?), GREATEST(r.checkin_date, ?)),0)),0) FROM reservation r JOIN property p ON r.property_code = p.property_code WHERE r.checkin_date < ? AND r.checkout_date > ? AND r.canceled_at IS NULL AND p.hotel_group_code = ?",
                                        Integer.class, dayEndExDate, dayStartDate, dayEndExDate, dayStartDate, hotelGroup);
                            } else {
                                occupiedNights = jdbc.queryForObject(
                                        "SELECT COALESCE(SUM(GREATEST(DATEDIFF(LEAST(r.checkout_date, ?), GREATEST(r.checkin_date, ?)),0)),0) FROM reservation r WHERE r.checkin_date < ? AND r.checkout_date > ? AND r.canceled_at IS NULL",
                                        Integer.class, dayEndExDate, dayStartDate, dayEndExDate, dayStartDate);
                            }

                            Integer totalRooms;
                            if (hotelId != null) {
                                totalRooms = jdbc.queryForObject(
                                        "SELECT COALESCE(COUNT(rm.room_code),0) FROM room rm JOIN room_type rt ON rm.room_type_code = rt.room_type_code WHERE rt.property_code = ? AND rm.room_status = 'ACTIVE'",
                                        Integer.class, hotelId);
                            } else if (hotelGroup != null) {
                                totalRooms = jdbc.queryForObject(
                                        "SELECT COALESCE(COUNT(rm.room_code),0) FROM room rm JOIN room_type rt ON rm.room_type_code = rt.room_type_code JOIN property p ON rt.property_code = p.property_code WHERE p.hotel_group_code = ? AND rm.room_status = 'ACTIVE'",
                                        Integer.class, hotelGroup);
                            } else {
                                totalRooms = jdbc.queryForObject(
                                        "SELECT COALESCE(COUNT(rm.room_code),0) FROM room rm WHERE rm.room_status = 'ACTIVE'",
                                        Integer.class);
                            }

                            java.math.BigDecimal occValue = java.math.BigDecimal.ZERO;
                            if (totalRooms != null && totalRooms > 0) {
                                java.math.BigDecimal available = java.math.BigDecimal.valueOf(totalRooms);
                                occValue = java.math.BigDecimal.valueOf(occupiedNights != null ? occupiedNights : 0)
                                        .divide(available, 4, RoundingMode.HALF_UP)
                                        .multiply(java.math.BigDecimal.valueOf(100));
                            }
                            values.add(occValue);
                        }
                        break;
                    case "inquiry_count":
                        if (hotelId != null) {
                            sql = "SELECT DATE_FORMAT(i.created_at, '%d') AS bucket, COUNT(i.inquiry_code) AS val "
                                    + "FROM inquiry i WHERE i.created_at >= ? AND i.created_at < ? AND i.property_code = ? GROUP BY bucket ORDER BY bucket";
                            params = new Object[]{tsStart, tsEnd, hotelId};
                        } else if (hotelGroup != null) {
                            sql = "SELECT DATE_FORMAT(i.created_at, '%d') AS bucket, COUNT(i.inquiry_code) AS val "
                                    + "FROM inquiry i JOIN property p ON i.property_code = p.property_code WHERE i.created_at >= ? AND i.created_at < ? AND p.hotel_group_code = ? GROUP BY bucket ORDER BY bucket";
                            params = new Object[]{tsStart, tsEnd, hotelGroup};
                        } else {
                            sql = "SELECT DATE_FORMAT(i.created_at, '%d') AS bucket, COUNT(i.inquiry_code) AS val "
                                    + "FROM inquiry i WHERE i.created_at >= ? AND i.created_at < ? GROUP BY bucket ORDER BY bucket";
                            params = new Object[]{tsStart, tsEnd};
                        }
                        break;
                    case "claim_count":
                        if (hotelId != null) {
                            sql = "SELECT DATE_FORMAT(i.created_at, '%d') AS bucket, COUNT(i.inquiry_code) AS val "
                                    + "FROM inquiry i WHERE i.inquiry_category_code = 2 AND i.created_at >= ? AND i.created_at < ? AND i.property_code = ? GROUP BY bucket ORDER BY bucket";
                            params = new Object[]{tsStart, tsEnd, hotelId};
                        } else if (hotelGroup != null) {
                            sql = "SELECT DATE_FORMAT(i.created_at, '%d') AS bucket, COUNT(i.inquiry_code) AS val "
                                    + "FROM inquiry i JOIN property p ON i.property_code = p.property_code WHERE i.inquiry_category_code = 2 AND i.created_at >= ? AND i.created_at < ? AND p.hotel_group_code = ? GROUP BY bucket ORDER BY bucket";
                            params = new Object[]{tsStart, tsEnd, hotelGroup};
                        } else {
                            sql = "SELECT DATE_FORMAT(i.created_at, '%d') AS bucket, COUNT(i.inquiry_code) AS val "
                                    + "FROM inquiry i WHERE i.inquiry_category_code = 2 AND i.created_at >= ? AND i.created_at < ? GROUP BY bucket ORDER BY bucket";
                            params = new Object[]{tsStart, tsEnd};
                        }
                        break;
                    // 예약 수 (created_at 기준 일별 집계)
                    case "reservation_count":
                        if (hotelId != null) {
                            sql = "SELECT DATE_FORMAT(r.created_at, '%d') AS bucket, COUNT(r.reservation_code) AS val FROM reservation r WHERE r.created_at >= ? AND r.created_at < ? AND r.property_code = ? GROUP BY bucket ORDER BY bucket";
                            params = new Object[]{tsStart, tsEnd, hotelId};
                        } else if (hotelGroup != null) {
                            sql = "SELECT DATE_FORMAT(r.created_at, '%d') AS bucket, COUNT(r.reservation_code) AS val FROM reservation r JOIN property p ON r.property_code = p.property_code WHERE r.created_at >= ? AND r.created_at < ? AND p.hotel_group_code = ? GROUP BY bucket ORDER BY bucket";
                            params = new Object[]{tsStart, tsEnd, hotelGroup};
                        } else {
                            sql = "SELECT DATE_FORMAT(r.created_at, '%d') AS bucket, COUNT(r.reservation_code) AS val FROM reservation r WHERE r.created_at >= ? AND r.created_at < ? GROUP BY bucket ORDER BY bucket";
                            params = new Object[]{tsStart, tsEnd};
                        }
                        break;
                    case "cancel_count":
                        if (hotelId != null) {
                            sql = "SELECT DATE_FORMAT(r.canceled_at, '%d') AS bucket, COUNT(r.reservation_code) AS val FROM reservation r WHERE r.canceled_at >= ? AND r.canceled_at < ? AND r.canceled_at IS NOT NULL AND r.property_code = ? GROUP BY bucket ORDER BY bucket";
                            params = new Object[]{tsStart, tsEnd, hotelId};
                        } else if (hotelGroup != null) {
                            sql = "SELECT DATE_FORMAT(r.canceled_at, '%d') AS bucket, COUNT(r.reservation_code) AS val FROM reservation r JOIN property p ON r.property_code = p.property_code WHERE r.canceled_at >= ? AND r.canceled_at < ? AND r.canceled_at IS NOT NULL AND p.hotel_group_code = ? GROUP BY bucket ORDER BY bucket";
                            params = new Object[]{tsStart, tsEnd, hotelGroup};
                        } else {
                            sql = "SELECT DATE_FORMAT(r.canceled_at, '%d') AS bucket, COUNT(r.reservation_code) AS val FROM reservation r WHERE r.canceled_at >= ? AND r.canceled_at < ? AND r.canceled_at IS NOT NULL GROUP BY bucket ORDER BY bucket";
                            params = new Object[]{tsStart, tsEnd};
                        }
                        break;
                    case "no_show_count":
                        if (hotelId != null) {
                            sql = "SELECT DATE_FORMAT(r.checkin_date, '%d') AS bucket, COUNT(r.reservation_code) AS val FROM reservation r WHERE r.checkin_date >= ? AND r.checkin_date < ? AND r.reservation_status = 'NO_SHOW' AND r.property_code = ? GROUP BY bucket ORDER BY bucket";
                            params = new Object[]{dStart, dEndExclusive, hotelId};
                        } else if (hotelGroup != null) {
                            sql = "SELECT DATE_FORMAT(r.checkin_date, '%d') AS bucket, COUNT(r.reservation_code) AS val FROM reservation r JOIN property p ON r.property_code = p.property_code WHERE r.checkin_date >= ? AND r.checkin_date < ? AND r.reservation_status = 'NO_SHOW' AND p.hotel_group_code = ? GROUP BY bucket ORDER BY bucket";
                            params = new Object[]{dStart, dEndExclusive, hotelGroup};
                        } else {
                            sql = "SELECT DATE_FORMAT(r.checkin_date, '%d') AS bucket, COUNT(r.reservation_code) AS val FROM reservation r WHERE r.checkin_date >= ? AND r.checkin_date < ? AND r.reservation_status = 'NO_SHOW' GROUP BY bucket ORDER BY bucket";
                            params = new Object[]{dStart, dEndExclusive};
                        }
                        break;
                    case "facility_revenue":
                        if (hotelId != null) {
                            sql = "SELECT DATE_FORMAT(r.checkin_date, '%d') AS bucket, COALESCE(SUM(COALESCE(r.reservation_package_price,0)),0) AS val FROM reservation r WHERE r.checkin_date >= ? AND r.checkin_date < ? AND r.canceled_at IS NULL AND r.property_code = ? GROUP BY bucket ORDER BY bucket";
                            params = new Object[]{dStart, dEndExclusive, hotelId};
                        } else if (hotelGroup != null) {
                            sql = "SELECT DATE_FORMAT(r.checkin_date, '%d') AS bucket, COALESCE(SUM(COALESCE(r.reservation_package_price,0)),0) AS val FROM reservation r JOIN property p ON r.property_code = p.property_code WHERE r.checkin_date >= ? AND r.checkin_date < ? AND r.canceled_at IS NULL AND p.hotel_group_code = ? GROUP BY bucket ORDER BY bucket";
                            params = new Object[]{dStart, dEndExclusive, hotelGroup};
                        } else {
                            sql = "SELECT DATE_FORMAT(r.checkin_date, '%d') AS bucket, COALESCE(SUM(COALESCE(r.reservation_package_price,0)),0) AS val FROM reservation r WHERE r.checkin_date >= ? AND r.checkin_date < ? AND r.canceled_at IS NULL GROUP BY bucket ORDER BY bucket";
                            params = new Object[]{dStart, dEndExclusive};
                        }
                        break;
                }

                java.util.List<java.util.Map<String, Object>> rowsLocal = java.util.Collections.emptyList();
                if (needQuery && sql != null) {
                    log.debug("Timeseries SQL (internalKey={}) sql={} paramsPresent={}", internalKey, sql, params != null);
                    rowsLocal = params != null ? jdbc.queryForList(sql, params) : jdbc.queryForList(sql);
                }

                java.util.Map<String, java.math.BigDecimal> bucketMap = new java.util.HashMap<>();
                for (java.util.Map<String, Object> row : rowsLocal) {
                    String bucket = String.valueOf(row.get("bucket"));
                    Object valObj = row.get("val");
                    java.math.BigDecimal v = null;
                    if (valObj instanceof java.math.BigDecimal) v = (java.math.BigDecimal) valObj;
                    else if (valObj instanceof Number) v = java.math.BigDecimal.valueOf(((Number) valObj).doubleValue());
                    bucketMap.put(bucket, v);
                }

                // fill values for days 01..days
                for (int d = 1; d <= days; d++) {
                    String dd = String.format("%02d", d);
                    // occ_rate 또는 ADR(점유박수 기반 계산) 일별은 이미 values가 채워졌으므로 bucketMap 무시
                    if (("occ_rate".equals(internalKey) || "avg_daily_rate".equals(internalKey) || "adr".equals(internalKey)) && values.size() == days) continue;
                     values.add(bucketMap.getOrDefault(dd, null));
                }
            }
        } catch (Exception ex) {
            log.warn("Failed to build timeseries for metricKey={}", metricKey, ex);
        }

        // 5) MetricTimeSeries 생성 및 반환
        MetricTimeSeries mts = new MetricTimeSeries();
        mts.setLabels(labels);
        MetricTimeSeries.Series s = new MetricTimeSeries.Series();
        s.setName("actual");
        s.setData(values);
        mts.setSeries(java.util.Collections.singletonList(s));

        return mts;
    }

    // --- 고객유형(개인/법인) 분포 집계 ---
    /**
     * 고객 테이블에서 contract_type별 건수를 집계하여 ChartWidgetDto 형태로 반환합니다.
     * 필터에서 hotelGroupCode가 주어지면 해당 호텔그룹으로 제한합니다.
     * 반환 형식: widgetType="GAUGE", labels=["INDIVIDUAL","CORPORATE"], series=[{name:"actual", data:[..]}], meta.total
     */
    public ChartWidgetDto queryCustomerContractDistribution(Map<String, Object> filter) {
        Object hotelGroup = filter != null ? filter.get("hotelGroupCode") : null;
        Object periodObj = filter != null ? filter.get("period") : null;
        String period = periodObj == null ? null : String.valueOf(periodObj);

        // period가 제공되면 customer.created_at을 기준으로 기간 필터를 적용합니다.
        boolean hasPeriod = false;
        java.sql.Date dStart = null;
        java.sql.Date dEndExclusive = null;
        if (period != null) {
            try {
                java.time.LocalDate[] range = computeRangeFromPeriod(period);
                java.time.LocalDate s = range[0];
                java.time.LocalDate e = range[1];
                dStart = java.sql.Date.valueOf(s);
                dEndExclusive = java.sql.Date.valueOf(e.plusDays(1)); // exclusive bound
                hasPeriod = true;
            } catch (Exception ex) {
                log.warn("Invalid period passed to queryCustomerContractDistribution: {}", period);
                hasPeriod = false;
            }
        }

        String sql;
        java.util.List<java.util.Map<String, Object>> rows;

        if (hotelGroup != null) {
            if (hasPeriod) {
                sql = "SELECT c.contract_type AS label, COALESCE(COUNT(*),0) AS cnt FROM customer c WHERE c.hotel_group_code = ? AND c.created_at >= ? AND c.created_at < ? GROUP BY c.contract_type";
                rows = jdbc.queryForList(sql, hotelGroup, dStart, dEndExclusive);
            } else {
                sql = "SELECT c.contract_type AS label, COALESCE(COUNT(*),0) AS cnt FROM customer c WHERE c.hotel_group_code = ? GROUP BY c.contract_type";
                rows = jdbc.queryForList(sql, hotelGroup);
            }
        } else {
            if (hasPeriod) {
                sql = "SELECT c.contract_type AS label, COALESCE(COUNT(*),0) AS cnt FROM customer c WHERE c.created_at >= ? AND c.created_at < ? GROUP BY c.contract_type";
                rows = jdbc.queryForList(sql, dStart, dEndExclusive);
            } else {
                sql = "SELECT c.contract_type AS label, COALESCE(COUNT(*),0) AS cnt FROM customer c GROUP BY c.contract_type";
                rows = jdbc.queryForList(sql);
            }
        }

        long individual = 0L;
        long corporate = 0L;
        long other = 0L;

        for (java.util.Map<String, Object> r : rows) {
            String label = r.get("label") == null ? "OTHER" : String.valueOf(r.get("label"));
            Number cntNum = (Number) r.get("cnt");
            long cnt = cntNum == null ? 0L : cntNum.longValue();
            if ("INDIVIDUAL".equalsIgnoreCase(label)) individual = cnt;
            else if ("CORPORATE".equalsIgnoreCase(label)) corporate = cnt;
            else other += cnt;
        }

        java.util.List<String> labels = java.util.Arrays.asList("INDIVIDUAL", "CORPORATE");
        java.util.List<java.math.BigDecimal> data = new java.util.ArrayList<>();
        data.add(java.math.BigDecimal.valueOf(individual));
        data.add(java.math.BigDecimal.valueOf(corporate));

        java.util.Map<String, Object> meta = new java.util.HashMap<>();
        meta.put("total", individual + corporate + other);
        meta.put("other", other);

        // 프론트에서 기대하는 widgetType이 "GAUGE" 이므로, ChartWidgetDto의 widgetType을 "gauge"로 명시합니다.
        // series는 이름을 "actual"로 하고 숫자 배열을 전달합니다.
        java.util.List<ChartWidgetDto.Series> series = new java.util.ArrayList<>();
        series.add(new ChartWidgetDto.Series("actual", data));

        ChartWidgetDto dto = new ChartWidgetDto("gauge", labels, series, meta);
        return dto;
    }

    // --- 외국인 TOP3 국가 집계 ---
    /**
     * 상위 3개 외국인 고객 국가별 건수를 조회하여 Bar 차트용 ChartWidgetDto로 반환합니다.
     * 필터에서 hotelGroupCode가 주어지면 해당 호텔그룹으로 제한합니다.
     * period가 주어지면 customer.created_at 기준으로 기간 필터를 적용합니다.
     */
    public ChartWidgetDto queryForeignTop3(Map<String, Object> filter) {
        Object hotelGroup = filter != null ? filter.get("hotelGroupCode") : null;
        Object periodObj = filter != null ? filter.get("period") : null;
        String period = periodObj == null ? null : String.valueOf(periodObj);

        // period가 제공되면 customer.created_at을 기준으로 기간 필터를 적용합니다.
        boolean hasPeriod = false;
        java.sql.Date dStart = null;
        java.sql.Date dEndExclusive = null;
        if (period != null) {
            try {
                java.time.LocalDate[] range = computeRangeFromPeriod(period);
                java.time.LocalDate s = range[0];
                java.time.LocalDate e = range[1];
                dStart = java.sql.Date.valueOf(s);
                dEndExclusive = java.sql.Date.valueOf(e.plusDays(1)); // exclusive bound
                hasPeriod = true;
            } catch (Exception ex) {
                log.warn("Invalid period passed to queryForeignTop3: {}", period);
                hasPeriod = false;
            }
        }

        String sql;
        java.util.List<java.util.Map<String,Object>> rows;

        if (hotelGroup != null) {
            if (hasPeriod) {
                sql = "SELECT c.nationality_code AS code, COALESCE(COUNT(*),0) AS cnt FROM customer c WHERE c.nationality_type = 'FOREIGN' AND c.hotel_group_code = ? AND c.created_at >= ? AND c.created_at < ? GROUP BY c.nationality_code ORDER BY cnt DESC LIMIT 3";
                rows = jdbc.queryForList(sql, hotelGroup, dStart, dEndExclusive);
            } else {
                sql = "SELECT c.nationality_code AS code, COALESCE(COUNT(*),0) AS cnt FROM customer c WHERE c.nationality_type = 'FOREIGN' AND c.hotel_group_code = ? GROUP BY c.nationality_code ORDER BY cnt DESC LIMIT 3";
                rows = jdbc.queryForList(sql, hotelGroup);
            }
        } else {
            if (hasPeriod) {
                sql = "SELECT c.nationality_code AS code, COALESCE(COUNT(*),0) AS cnt FROM customer c WHERE c.nationality_type = 'FOREIGN' AND c.created_at >= ? AND c.created_at < ? GROUP BY c.nationality_code ORDER BY cnt DESC LIMIT 3";
                rows = jdbc.queryForList(sql, dStart, dEndExclusive);
            } else {
                sql = "SELECT c.nationality_code AS code, COALESCE(COUNT(*),0) AS cnt FROM customer c WHERE c.nationality_type = 'FOREIGN' GROUP BY c.nationality_code ORDER BY cnt DESC LIMIT 3";
                rows = jdbc.queryForList(sql);
            }
        }
        
        java.util.List<String> labels = new java.util.ArrayList<>();
        java.util.List<java.math.BigDecimal> data = new java.util.ArrayList<>();

        for (java.util.Map<String,Object> r : rows) {
            String code = r.get("code") == null ? "OTHER" : String.valueOf(r.get("code"));
            Number cntNum = (Number) r.get("cnt");
            long cnt = cntNum == null ? 0L : cntNum.longValue();
            String label;
            switch(code.toUpperCase(Locale.ROOT)) {
                case "CN": label = "중국"; break;
                case "JP": label = "일본"; break;
                case "TW": label = "대만"; break;
                case "US": label = "미국"; break;
                case "VN": label = "베트남"; break;
                case "TH": label = "태국"; break;
                case "PH": label = "필리핀"; break;
                case "ID": label = "인도네시아"; break;
                case "IN": label = "인도"; break;
                default: label = code; break;
            }
            labels.add(label);
            data.add(java.math.BigDecimal.valueOf(cnt));
        }

        java.util.Map<String,Object> meta = new java.util.HashMap<>();
        meta.put("chartKind","bar");
        meta.put("topN", rows.size());

        // series 생성
        java.util.List<ChartWidgetDto.Series> series = new java.util.ArrayList<>();
        series.add(new ChartWidgetDto.Series("actual", data));

        // widgetType은 'bar'로 반환
        return new ChartWidgetDto("bar", labels, series, meta);
    }
}