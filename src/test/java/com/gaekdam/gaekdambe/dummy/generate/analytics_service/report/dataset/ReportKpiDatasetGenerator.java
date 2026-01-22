package com.gaekdam.gaekdambe.dummy.generate.analytics_service.report.dataset;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
            kpis.put("OCC_RATE", new Object[]{"객실점유율", "REV", "PERCENT", "Occupancy Rate", null, 1});
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
        
        // 3) Rich dummy generator for years 2021..2031 for hotelGroup=1
        {
            LocalDateTime now = LocalDateTime.now();
            long hotelGroup = 1L;

            // remove existing targets for this hotelGroup to regenerate clean set
            List<ReportKPITarget> allExisting = targetRepo.findAll();
            List<ReportKPITarget> toRemove = allExisting.stream()
                    .filter(t -> t.getId() != null && t.getId().getHotelGroupCode() == hotelGroup)
                    .collect(Collectors.toList());
            if (!toRemove.isEmpty()) {
                targetRepo.deleteAll(toRemove);
            }

            // Select 16 KPI codes to generate (consistent across years/months)
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
                "UNRESOLVED_RATE",
                "AVG_RESPONSE_TIME",
                "RESERVATION_COUNT",
                "CANCELLATION_RATE",
                "NO_SHOW_RATE",
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
            baseAnnual.put("UNRESOLVED_RATE", 0.12);
            baseAnnual.put("AVG_RESPONSE_TIME", 5.0);
            baseAnnual.put("RESERVATION_COUNT", 1200.0);
            baseAnnual.put("CANCELLATION_RATE", 0.15);
            baseAnnual.put("NO_SHOW_RATE", 0.04);
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
