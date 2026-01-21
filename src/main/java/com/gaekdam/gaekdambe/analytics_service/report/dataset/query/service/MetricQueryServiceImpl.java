package com.gaekdam.gaekdambe.analytics_service.report.dataset.query.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.time.LocalDate;
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
        BigDecimal target = targetRepo.findFirstByKpiCodeAndPeriodValue(metricKey, period)
                .map(ReportKPITarget::getTargetValue)
                .orElseGet(() -> targetRepo.findFirstByKpiCodeOrderByCreatedAtDesc(metricKey)
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
        String formattedActual = formatByMetric(metricKey, actual);
        String formattedTarget = formatByMetric(metricKey, target);

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

        switch (metricKey) {
            case "checkin": {
                Integer cnt = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM bookings WHERE checkin_date >= ? AND checkin_date < ?",
                    Integer.class, dStart, dEnd);
                return BigDecimal.valueOf(cnt != null ? cnt : 0);
            }
            case "checkout": {
                Integer cnt = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM bookings WHERE checkout_date >= ? AND checkout_date < ?",
                    Integer.class, dStart, dEnd);
                return BigDecimal.valueOf(cnt != null ? cnt : 0);
            }
            case "avg_daily_rate": {
                BigDecimal avg = jdbc.queryForObject(
                    "SELECT COALESCE(AVG(room_rate),0) FROM bookings WHERE stay_date >= ? AND stay_date < ?",
                    BigDecimal.class, dStart, dEnd);
                return avg == null ? BigDecimal.ZERO : avg;
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
            default:
                // 정수형 수치 표시
                return String.format("%s", value.setScale(0, RoundingMode.HALF_UP).toPlainString());
        }
    }
}