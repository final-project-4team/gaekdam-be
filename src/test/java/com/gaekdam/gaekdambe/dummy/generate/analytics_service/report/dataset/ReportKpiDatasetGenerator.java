package com.gaekdam.gaekdambe.dummy.generate.analytics_service.report.dataset;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.domain.entity.ReportKPICodeDim;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.domain.entity.ReportKPITarget;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.infrastructure.repository.ReportKPICodeDimRepository;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.infrastructure.repository.ReportKPITargetRepository;

@Component
public class ReportKpiDatasetGenerator {

    @Autowired
    private ReportKPICodeDimRepository codeRepo;

    @Autowired
    private ReportKPITargetRepository targetRepo;

    @Transactional
    public void generate() {

        if (codeRepo.count() > 0) {
            return;
        }

        if (targetRepo.count() > 0) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();

        // 1) Insert KPI codes (ReportKPICodeDim)
        Map<String, Object[]> kpis = new LinkedHashMap<>();
        kpis.put("CHECKIN", new Object[]{"체크인", "REV", "COUNT", "체크인 수", null, 1});
        kpis.put("CHECKOUT", new Object[]{"체크아웃", "REV", "COUNT", "체크아웃 수", null, 1});
        kpis.put("ADR", new Object[]{"평균객실단가", "REV", "KRW", "Average Daily Rate", null, 1});
        kpis.put("OCC_RATE", new Object[]{"객실점유율", "REV", "PEqRCENT", "Occupancy Rate", null, 1});
        kpis.put("GUEST_COUNT", new Object[]{"투숙객", "CUST", "COUNT", "Guest Count", null, 1});
        kpis.put("REPEAT_RATE", new Object[]{"재방문율", "CUST", "PERCENT", "Repeat visitor rate", null, 1});
        kpis.put("MEMBERSHIP_RATE", new Object[]{"멤버십 비율", "CUST", "PERCENT", "Membership penetration rate", null, 1});
        kpis.put("FOREIGN_RATE", new Object[]{"외국인 비율", "CUST", "PERCENT", "Foreign guest ratio", null, 1});
        kpis.put("INQUIRY_COUNT", new Object[]{"고객 문의", "CX", "COUNT", "Customer inquiries", null, 1});
        kpis.put("CLAIM_COUNT", new Object[]{"고객 클레임", "CX", "COUNT", "Customer claims", null, 1});
        kpis.put("UNRESOLVED_RATE", new Object[]{"미처리 문의 비율", "CX", "PERCENT", "Unresolved inquiry rate", null, 1});
        kpis.put("AVG_RESPONSE_TIME", new Object[]{"평균응답시간", "CX", "HOURS", "Average response time (hours)", null, 1});
        kpis.put("RESERVATION_COUNT", new Object[]{"예약", "REV", "COUNT", "Reservations", null, 1});
        kpis.put("CANCELLATION_RATE", new Object[]{"예약 취소율", "REV", "PERCENT", "Cancellation rate", null, 1});
        kpis.put("NO_SHOW_RATE", new Object[]{"노쇼율", "REV", "PERCENT", "No-show rate", null, 1});
        kpis.put("NON_ROOM_REVENUE", new Object[]{"객실 외 매출", "REV", "KRW", "Non-room revenue", null, 1});

        List<ReportKPICodeDim> codeEntities = new ArrayList<>();
        for (Map.Entry<String, Object[]> e : kpis.entrySet()) {
            String code = e.getKey();
            Object[] v = e.getValue();
            if (codeRepo.existsById(code)) continue;
            ReportKPICodeDim ent = ReportKPICodeDim.builder()
                    .kpiCode(code)
                    .kpiName((String) v[0])
                    .domainType((String) v[1])
                    .unit((String) v[2])
                    .description((String) v[3])
                    .calcRuleJson((String) v[4])
                    .isActive(((Integer) v[5]) == 1)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();
            codeEntities.add(ent);
        }

        if (!codeEntities.isEmpty()) {
            // saveAll in one batch
            codeRepo.saveAll(codeEntities);
        }

        // 2) Insert KPI targets (ReportKPITarget)
        Map<String, Double[]> targets = new LinkedHashMap<>();
        targets.put("CHECKIN", new Double[]{150.0, 120.0, 90.0});
        targets.put("CHECKOUT", new Double[]{145.0,115.0,85.0});
        targets.put("ADR", new Double[]{180000.0,150000.0,120000.0});
        targets.put("OCC_RATE", new Double[]{0.75,0.6,0.45});
        targets.put("GUEST_COUNT", new Double[]{1200.0,1000.0,800.0});
        targets.put("REPEAT_RATE", new Double[]{0.32,0.25,0.15});
        targets.put("MEMBERSHIP_RATE", new Double[]{0.42,0.3,0.2});
        targets.put("FOREIGN_RATE", new Double[]{0.31,0.2,0.1});
        targets.put("INQUIRY_COUNT", new Double[]{200.0,150.0,100.0});
        targets.put("CLAIM_COUNT", new Double[]{15.0,25.0,40.0});
        targets.put("UNRESOLVED_RATE", new Double[]{0.12,0.2,0.3});
        targets.put("AVG_RESPONSE_TIME", new Double[]{4.0,6.0,8.0});
        targets.put("RESERVATION_COUNT", new Double[]{1400.0,1200.0,900.0});
        targets.put("CANCELLATION_RATE", new Double[]{0.14,0.2,0.3});
        targets.put("NO_SHOW_RATE", new Double[]{0.038,0.06,0.1});
        targets.put("NON_ROOM_REVENUE", new Double[]{3500000.0,2500000.0,1500000.0});

        String periodValue = "2025-12";
        long hotelGroup = 10001L;
        LocalDate effectiveFrom = LocalDate.of(2025,12,1);
        LocalDate effectiveTo = LocalDate.of(2025,12,31);

        List<ReportKPITarget> targetEntities = new ArrayList<>();
        for (Map.Entry<String, Double[]> e : targets.entrySet()) {
            String kpi = e.getKey();
            Double[] vals = e.getValue();
            String targetId = "TGT-" + kpi + "-" + periodValue;
            if (targetRepo.existsById(targetId)) continue;
            ReportKPITarget t = ReportKPITarget.builder()
                    .targetId(targetId)
                    .hotelGroupCode(hotelGroup)
                    .kpiCode(kpi)
                    .periodType("MONTH")
                    .periodValue(periodValue)
                    .targetValue(java.math.BigDecimal.valueOf(vals[0]))
                    .warningThreshold(java.math.BigDecimal.valueOf(vals[1]))
                    .dangerThreshold(java.math.BigDecimal.valueOf(vals[2]))
                    .seasonType(null)
                    .effectiveFrom(effectiveFrom)
                    .effectiveTo(effectiveTo)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();
            targetEntities.add(t);
        }

        if (!targetEntities.isEmpty()) {
            targetRepo.saveAll(targetEntities);
        }

        System.out.println("Report KPI code & target generation completed.");
    }
}
