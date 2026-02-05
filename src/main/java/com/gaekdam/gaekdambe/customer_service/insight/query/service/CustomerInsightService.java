package com.gaekdam.gaekdambe.customer_service.insight.query.service;

import com.gaekdam.gaekdambe.customer_service.customer.command.domain.entity.Customer;
import com.gaekdam.gaekdambe.customer_service.customer.command.infrastructure.repository.CustomerRepository;
import com.gaekdam.gaekdambe.customer_service.insight.query.dto.response.CustomerInsightResponse;
import com.gaekdam.gaekdambe.customer_service.insight.query.mapper.CustomerInsightMapper;
import com.gaekdam.gaekdambe.customer_service.loyalty.command.domain.entity.Loyalty;
import com.gaekdam.gaekdambe.customer_service.loyalty.command.domain.entity.LoyaltyGrade;
import com.gaekdam.gaekdambe.customer_service.loyalty.command.infrastructure.repository.LoyaltyGradeRepository;
import com.gaekdam.gaekdambe.customer_service.loyalty.command.infrastructure.repository.LoyaltyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.gaekdam.gaekdambe.global.crypto.DecryptionService;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CustomerInsightService {

    private final CustomerInsightMapper customerInsightMapper;
    private final CustomerRepository customerRepository;
    private final LoyaltyRepository loyaltyRepository;
    private final LoyaltyGradeRepository loyaltyGradeRepository;
    private final DecryptionService decryptionService;

    public CustomerInsightResponse getInsight(Long customerCode) {
        // 0. 데이터 존재 여부 확인 (없을 경우 데모 데이터 반환)
        List<Map<String, Object>> recentReservations = customerInsightMapper.selectRecentReservations(customerCode);

        // 1. 프로필 데이터 조회
        Customer customer = customerRepository.findById(customerCode)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + customerCode));

        String customerName = "Unknown";
        try {
            Long hotelGroupCode = customer.getHotelGroupCode();
            byte[] dekEnc = customer.getDekEnc();
            byte[] nameEnc = customer.getCustomerNameEnc();
            if (dekEnc != null && nameEnc != null) {
                customerName = decryptionService.decrypt(customer.getCustomerCode(), dekEnc, nameEnc);
            }
        } catch (Exception e) {
            customerName = "Customer-" + customerCode;
        }

        Loyalty loyalty = loyaltyRepository
                .findByHotelGroupCodeAndCustomerCode(customer.getHotelGroupCode(), customerCode)
                .orElse(null);

        String gradeName = "General";
        if (loyalty != null) {
            LoyaltyGrade grade = loyaltyGradeRepository.findById(loyalty.getLoyaltyGradeCode()).orElse(null);
            if (grade != null) {
                gradeName = grade.getLoyaltyGradeName();
            }
        }

        CustomerInsightResponse.Profile profile = CustomerInsightResponse.Profile.builder()
                .customerName(customerName)
                .grade(gradeName)
                .joinedAt(customer.getCreatedAt().toLocalDate())
                .lastVisitedAt(recentReservations.isEmpty() ? null
                        : ((java.sql.Date) recentReservations.get(0).get("checkinDate")).toLocalDate())
                .build();

        // 2. 핵심 성과 지표 계산
        Map<String, Object> kpiMap = customerInsightMapper.selectCustomerKpi(customerCode);
        BigDecimal totalSpending = (BigDecimal) kpiMap.getOrDefault("totalSpending", BigDecimal.ZERO);
        BigDecimal totalStayDaysDec = (BigDecimal) kpiMap.getOrDefault("totalStayDays", BigDecimal.ZERO);
        int totalStayDays = totalStayDaysDec != null ? totalStayDaysDec.intValue() : 0;
        Long resCount = (Long) kpiMap.getOrDefault("reservationCount", 0L);

        Double avgStayDuration = (resCount != null && resCount > 0) ? (double) totalStayDays / resCount : 0.0;

        // 소비 트렌드 계산
        BigDecimal lastYearSpending = customerInsightMapper.selectLastYearTotalSpending(customerCode);
        if (lastYearSpending == null)
            lastYearSpending = BigDecimal.ZERO;

        List<Map<String, Object>> monthlyMap = customerInsightMapper.selectMonthlySpending(customerCode);

        // 월별 데이터로 올해 소비액 추산 (근사치)
        int currentYear = LocalDate.now().getYear();
        BigDecimal thisYearSpending = BigDecimal.ZERO;

        if (monthlyMap != null) {
            thisYearSpending = monthlyMap.stream()
                    .filter(m -> {
                        String monthStr = (String) m.get("month");
                        return monthStr != null && monthStr.startsWith(String.valueOf(currentYear));
                    })
                    .map(m -> (BigDecimal) m.get("amount"))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        Double trend = 0.0;
        if (lastYearSpending.compareTo(BigDecimal.ZERO) > 0) {
            trend = thisYearSpending.subtract(lastYearSpending)
                    .divide(lastYearSpending, 2, java.math.RoundingMode.HALF_UP)
                    .multiply(new BigDecimal(100)).doubleValue();
        } else if (thisYearSpending.compareTo(BigDecimal.ZERO) > 0) {
            trend = 100.0; // New spender
        }

        CustomerInsightResponse.Kpi kpi = CustomerInsightResponse.Kpi.builder()
                .totalSpending(totalSpending)
                .totalSpendingTrend(trend)
                .totalStayDays(totalStayDays)
                .avgStayDuration(Math.round(avgStayDuration * 10.0) / 10.0)
                .build();

        // 3. 마케팅 인사이트 로직 (계절 분석 확장 - 최다 빈도 시즌 선정)
        long springCount = recentReservations.stream().filter(r -> {
            Integer m = (Integer) r.get("checkinMonth");
            return m != null && m >= 3 && m <= 5;
        }).count();
        long summerCount = recentReservations.stream().filter(r -> {
            Integer m = (Integer) r.get("checkinMonth");
            return m != null && m >= 6 && m <= 8;
        }).count();
        long fallCount = recentReservations.stream().filter(r -> {
            Integer m = (Integer) r.get("checkinMonth");
            return m != null && m >= 9 && m <= 11;
        }).count();
        long winterCount = recentReservations.stream().filter(r -> {
            Integer m = (Integer) r.get("checkinMonth");
            return m != null && (m == 12 || m == 1 || m == 2);
        }).count();

        String summary = "일반적인 투숙 패턴을 보입니다.";
        List<String> details = new java.util.ArrayList<>();
        long totalCount = recentReservations.size();

        if (totalCount > 0) {
            // 가장 많이 방문한 계절 찾기
            long maxCount = Math.max(Math.max(springCount, summerCount), Math.max(fallCount, winterCount));

            // 제 의견: 최소한의 비중(30% 이상)은 차지해야 "선호한다"고 말할 수 있습니다.
            // 예: 10번 중 3번 이상은 방문해야 의미있는 패턴으로 간주 (30%)
            // 그렇지 않으면 골고루 방문한 것이므로 "일반적인 패턴"으로 둡니다.
            if (maxCount > 0 && (double) maxCount / totalCount >= 0.3) {
                if (maxCount == springCount) {
                    summary = "봄 시즌(3~5월)을 선호하는 고객입니다.";
                    details.add("봄맞이 프로모션 대상");
                } else if (maxCount == summerCount) {
                    summary = "여름 휴가철(6~8월)을 선호하는 고객입니다.";
                    details.add("여름 시즌 얼리버드 프로모션 반응 가능성 높음");
                } else if (maxCount == fallCount) {
                    summary = "가을 시즌(9~11월)을 선호하는 고객입니다.";
                    details.add("가을 단풍여행/휴식 패키지 추천");
                } else if (maxCount == winterCount) {
                    summary = "겨울 시즌(12~2월)을 선호하는 고객입니다.";
                    details.add("겨울방학 및 연말/연초 호캉스 추천");
                }
            }
        }

        // 실제 데이터 기반 선호 객실 및 부대시설 분석
        List<Map<String, Object>> prefRoom = customerInsightMapper.selectPreferredRoomType(customerCode);
        if (prefRoom != null && !prefRoom.isEmpty()) {
            String roomType = (String) prefRoom.get(0).get("typeName");
            if (roomType != null) {
                details.add("선호 객실 타입: " + roomType);
                if (roomType.contains("Suite") || roomType.contains("스위트")) {
                    summary = "스위트룸을 선호하는 프리미엄 고객입니다.";
                }
            }
        }

        List<Map<String, Object>> prefFacility = customerInsightMapper.selectPreferredFacility(customerCode);
        if (prefFacility != null && !prefFacility.isEmpty()) {
            String facility = (String) prefFacility.get(0).get("facilityName");
            if (facility != null) {
                details.add("자주 이용하는 부대시설: " + facility);
            }
        }

        if (details.isEmpty()) {
            details.add("특이 사항 없음");
        }

        CustomerInsightResponse.MarketingInsight marketingInsight = CustomerInsightResponse.MarketingInsight.builder()
                .summary(summary)
                .details(details)
                .build();

        // 4. 차트 데이터 구성
        List<CustomerInsightResponse.MonthlySpending> monthlySpendings = java.util.List.of();
        if (monthlyMap != null) {
            monthlySpendings = monthlyMap.stream()
                    .map(m -> CustomerInsightResponse.MonthlySpending.builder()
                            .month((String) m.get("month"))
                            .amount((BigDecimal) m.get("amount"))
                            .build())
                    .toList();
        }

        Map<String, BigDecimal> catMap = customerInsightMapper.selectSpendingCategory(customerCode);
        BigDecimal roomAmount = catMap != null ? catMap.getOrDefault("roomAmount", BigDecimal.ZERO) : BigDecimal.ZERO;
        BigDecimal fnbAmount = catMap != null ? catMap.getOrDefault("fnbAmount", BigDecimal.ZERO) : BigDecimal.ZERO;

        // DTO 내부 클래스 빌더 사용 (객실, F&B)
        List<CustomerInsightResponse.SpendingCategory> categories = new java.util.ArrayList<>();
        categories.add(
                CustomerInsightResponse.SpendingCategory.builder().label("객실").value(0).amount(roomAmount).build());
        categories.add(
                CustomerInsightResponse.SpendingCategory.builder().label("F&B").value(0).amount(fnbAmount).build());

        // 부대시설 상세 내역 추가 (시설 타입별 그룹화: 예 - Wellness, Activity)
        List<Map<String, Object>> facDetails = customerInsightMapper.selectFacilitySpendingDetail(customerCode);
        BigDecimal totalFacilityAmount = BigDecimal.ZERO;

        if (facDetails != null) {
            for (Map<String, Object> f : facDetails) {
                String fType = (String) f.get("facilityType");
                BigDecimal fAmount = (BigDecimal) f.get("amount");
                if (fAmount == null)
                    fAmount = BigDecimal.ZERO;

                totalFacilityAmount = totalFacilityAmount.add(fAmount);
                categories.add(CustomerInsightResponse.SpendingCategory.builder()
                        .label(fType) // 예: 식사, 운동, 휴식 등
                        .value(0)
                        .amount(fAmount)
                        .build());
            }
        }

        // 5. 기타 비용 (Total Spending과의 차액 보정)
        // 차트의 모든 상세 항목(객실 + F&B + 집계된 부대시설)의 합이 LTV(totalSpending)보다 작을 경우,
        // 그 차액을 모두 '기타'로 통합하여 표시합니다. (세금, 봉사료, 집계되지 않은 부대시설 등 포함)

        BigDecimal currentChartTotal = roomAmount.add(fnbAmount).add(totalFacilityAmount);

        // totalSpending(LTV)이 더 크면 차액을 '기타'로 추가
        if (totalSpending.compareTo(currentChartTotal) > 0) {
            BigDecimal otherDiff = totalSpending.subtract(currentChartTotal);
            categories.add(CustomerInsightResponse.SpendingCategory.builder()
                    .label("기타")
                    .value(0)
                    .amount(otherDiff)
                    .build());
        }

        // 백분율 재계산
        // 이제 totalSpending(LTV)을 기준으로 퍼센트 계산
        final BigDecimal finalTotal = totalSpending.compareTo(BigDecimal.ZERO) > 0 ? totalSpending : BigDecimal.ONE;

        categories = categories.stream().map(c -> CustomerInsightResponse.SpendingCategory.builder()
                .label(c.getLabel())
                .amount(c.getAmount())
                .value(c.getAmount().divide(finalTotal, 2, java.math.RoundingMode.HALF_UP)
                        .multiply(new BigDecimal(100)).intValue())
                .build()).toList();

        List<Map<String, Object>> patternList = customerInsightMapper.selectStayDayPattern(customerCode);
        Map<String, Integer> stayDayPattern = new LinkedHashMap<>();
        List.of("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")
                .forEach(d -> stayDayPattern.put(d, 0));

        if (patternList != null) {
            for (Map<String, Object> p : patternList) {
                String day = (String) p.get("dayOfWeek");
                Long count = (Long) p.get("count");
                if (day != null && day.length() >= 3) {
                    stayDayPattern.put(day.substring(0, 3), count != null ? count.intValue() : 0);
                }
            }
        }

        CustomerInsightResponse.ChartData chartData = CustomerInsightResponse.ChartData.builder()
                .monthlySpending(monthlySpendings)
                .spendingCategory(categories)
                .stayDayPattern(stayDayPattern)
                .build();

        return CustomerInsightResponse.builder().profile(profile).kpi(kpi).marketingInsight(marketingInsight)
                .chartData(chartData).build();
    }

}
