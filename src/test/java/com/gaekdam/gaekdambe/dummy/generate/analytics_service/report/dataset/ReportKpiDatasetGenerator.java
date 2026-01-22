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

import com.gaekdam.gaekdambe.analytics_service.report.dataset.command.domain.entity.ReportKPICodeDim;
import com.gaekdam.gaekdambe.analytics_service.report.dataset.command.domain.entity.ReportKPITarget;
import com.gaekdam.gaekdambe.analytics_service.report.dataset.command.domain.entity.ReportKPITargetId;
import com.gaekdam.gaekdambe.analytics_service.report.dataset.command.domain.infrastructure.repository.ReportKPICodeDimRepository;
import com.gaekdam.gaekdambe.analytics_service.report.dataset.command.domain.infrastructure.repository.ReportKPITargetRepository;

@Component
public class ReportKpiDatasetGenerator {

    @Autowired
    private ReportKPICodeDimRepository codeRepo;

    @Autowired
    private ReportKPITargetRepository targetRepo;

    @Transactional
    public void generate() {

        if (codeRepo.count() > 0) {
            // codes already exist, do not recreate
        } else {
            // If codes don't exist, create them (original list)
            LocalDateTime now = LocalDateTime.now();
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

                // build entity using setters instead of Lombok builder
                ReportKPICodeDim ent = new ReportKPICodeDim();
                ent.setKpiCode(code);
                ent.setKpiName((String) v[0]);
                ent.setDomainType((String) v[1]);
                ent.setUnit((String) v[2]);
                ent.setDescription((String) v[3]);
                ent.setCalcRuleJson((String) v[4]);
                ent.setIsActive(((Integer) v[5]) == 1);
                ent.setCreatedAt(now);
                ent.setUpdatedAt(now);

                codeEntities.add(ent);
            }

            if (!codeEntities.isEmpty()) {
                // saveAll in one batch
                codeRepo.saveAll(codeEntities);
            }
        }

        // existing simple target seeding (kept for backward compatibility)
        if (targetRepo.count() == 0) {
            LocalDateTime now = LocalDateTime.now();
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

                // create embedded id and check existence with correct type
                ReportKPITargetId id = new ReportKPITargetId(targetId, hotelGroup);
                if (targetRepo.existsById(id)) continue;

                // build entity using setters and set EmbeddedId
                ReportKPITarget t = new ReportKPITarget();
                t.setId(id);
                t.setKpiCode(kpi);
                t.setPeriodType("MONTH");
                t.setPeriodValue(periodValue);
                t.setTargetValue(java.math.BigDecimal.valueOf(vals[0]));
                t.setWarningThreshold(java.math.BigDecimal.valueOf(vals[1]));
                t.setDangerThreshold(java.math.BigDecimal.valueOf(vals[2]));
                t.setSeasonType(null);
                t.setEffectiveFrom(effectiveFrom);
                t.setEffectiveTo(effectiveTo);
                t.setCreatedAt(now);
                t.setUpdatedAt(now);

                targetEntities.add(t);
            }

            if (!targetEntities.isEmpty()) {
                targetRepo.saveAll(targetEntities);
            }
        }

        // 3) Rich dummy generator for years 2021..2031 for hotelGroup=1
        {
            LocalDateTime now = LocalDateTime.now();
            long hotelGroup = 1L;

            // Select 11 KPI codes to generate (consistent across years/months)
            String[] genKpis = new String[] {
                "CHECKIN",
                "CHECKOUT",
                "ADR",
                "OCC_RATE",
                "GUEST_COUNT",
                "REPEAT_RATE",
                "MEMBERSHIP_RATE",
                "FOREIGN_RATE",
                "INQUIRY_COUNT",
                "CLAIM_COUNT",
                "NON_ROOM_REVENUE"
            };

            // base values per KPI (annual base)
            Map<String, Double> baseAnnual = new LinkedHashMap<>();
            baseAnnual.put("CHECKIN", 120.0);
            baseAnnual.put("CHECKOUT", 115.0);
            baseAnnual.put("ADR", 100000.0);
            baseAnnual.put("OCC_RATE", 0.65);
            baseAnnual.put("GUEST_COUNT", 900.0);
            baseAnnual.put("REPEAT_RATE", 0.20);
            baseAnnual.put("MEMBERSHIP_RATE", 0.30);
            baseAnnual.put("FOREIGN_RATE", 0.20);
            baseAnnual.put("INQUIRY_COUNT", 160.0);
            baseAnnual.put("CLAIM_COUNT", 20.0);
            baseAnnual.put("NON_ROOM_REVENUE", 2000000.0);

            List<ReportKPITarget> toSave = new ArrayList<>();

            for (int year = 2021; year <= 2031; year++) {
                for (String kpi : genKpis) {
                    double base = baseAnnual.getOrDefault(kpi, 100.0);

                    // annual growth factor: small yearly increase or decrease
                    double yearFactor = 1.0 + (year - 2021) * 0.02; // +2% per year
                    double annualValue = base * yearFactor;

                    // thresholds as proportions
                    double warning = annualValue * 0.85;
                    double danger = annualValue * 0.7;

                    String periodYear = String.format("%04d", year);
                    String targetIdYear = kpi + "_" + periodYear;
                    ReportKPITargetId idYear = new ReportKPITargetId(targetIdYear, hotelGroup);
                    if (!targetRepo.existsById(idYear)) {
                        ReportKPITarget ty = new ReportKPITarget();
                        ty.setId(idYear);
                        ty.setKpiCode(kpi);
                        ty.setPeriodType("YEAR");
                        ty.setPeriodValue(periodYear);
                        // For percent KPIs (OCC_RATE, REPEAT_RATE, etc.) keep as ratio
                        ty.setTargetValue(java.math.BigDecimal.valueOf(annualValue));
                        ty.setWarningThreshold(java.math.BigDecimal.valueOf(warning));
                        ty.setDangerThreshold(java.math.BigDecimal.valueOf(danger));
                        ty.setSeasonType(null);
                        ty.setEffectiveFrom(LocalDate.of(year, 1, 1));
                        ty.setEffectiveTo(LocalDate.of(year, 12, 31));
                        ty.setCreatedAt(now);
                        ty.setUpdatedAt(now);
                        toSave.add(ty);
                    }

                    // monthly entries for each month
                    for (int m = 1; m <= 12; m++) {
                        // month variation factor to create different values across months
                        double monthFactor = 0.8 + 0.4 * ((m - 1) / 11.0); // ranges 0.8..1.2
                        double monthlyValue = annualValue * (monthFactor / 12.0) * 1.0; // scaled to month
                        double monthWarning = monthlyValue * 0.85;
                        double monthDanger = monthlyValue * 0.7;

                        String periodMonth = String.format("%04d-%02d", year, m);
                        String targetIdMonth = kpi + "_" + periodMonth;
                        ReportKPITargetId idMonth = new ReportKPITargetId(targetIdMonth, hotelGroup);
                        if (!targetRepo.existsById(idMonth)) {
                            ReportKPITarget tm = new ReportKPITarget();
                            tm.setId(idMonth);
                            tm.setKpiCode(kpi);
                            tm.setPeriodType("MONTH");
                            tm.setPeriodValue(periodMonth);
                            tm.setTargetValue(java.math.BigDecimal.valueOf(monthlyValue));
                            tm.setWarningThreshold(java.math.BigDecimal.valueOf(monthWarning));
                            tm.setDangerThreshold(java.math.BigDecimal.valueOf(monthDanger));
                            tm.setSeasonType(null);
                            LocalDate efFrom = LocalDate.of(year, m, 1);
                            LocalDate efTo = efFrom.withDayOfMonth(efFrom.lengthOfMonth());
                            tm.setEffectiveFrom(efFrom);
                            tm.setEffectiveTo(efTo);
                            tm.setCreatedAt(now);
                            tm.setUpdatedAt(now);
                            toSave.add(tm);
                        }
                    }

                    // batch save periodically to avoid huge memory usage
                    if (toSave.size() > 500) {
                        targetRepo.saveAll(toSave);
                        toSave.clear();
                    }
                }
            }

            if (!toSave.isEmpty()) {
                targetRepo.saveAll(toSave);
            }
        }

        System.out.println("Report KPI code & target generation completed.");
    }
}
