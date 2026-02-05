package com.gaekdam.gaekdambe.customer_service.insight.query.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Getter
@Builder
public class CustomerInsightResponse {
    private Profile profile;
    private Kpi kpi;
    private MarketingInsight marketingInsight;
    private ChartData chartData;

    @Getter
    @Builder
    public static class Profile {
        private String customerName;
        private String grade;
        private LocalDate joinedAt;
        private LocalDate lastVisitedAt;
    }

    @Getter
    @Builder
    public static class Kpi {
        private BigDecimal totalSpending;
        private Double totalSpendingTrend;
        private Integer totalStayDays;
        private Double avgStayDuration;
    }

    @Getter
    @Builder
    public static class MarketingInsight {
        private String summary;
        private List<String> details;
    }

    @Getter
    @Builder
    public static class ChartData {
        private List<MonthlySpending> monthlySpending;
        private List<SpendingCategory> spendingCategory;
        private Map<String, Integer> stayDayPattern;
    }

    @Getter
    @Builder
    public static class MonthlySpending {
        private String month;
        private BigDecimal amount;
    }

    @Getter
    @Builder
    public static class SpendingCategory {
        private String label;
        private Integer value;
        private BigDecimal amount;
    }
}
