package com.gaekdam.gaekdambe.customer_service.loyalty.query.service;

import com.gaekdam.gaekdambe.customer_service.loyalty.query.dto.response.LoyaltyGradeDetailQueryResponse;
import com.gaekdam.gaekdambe.customer_service.loyalty.query.dto.response.LoyaltyGradeListQueryResponse;
import com.gaekdam.gaekdambe.customer_service.loyalty.query.mapper.LoyaltyGradeMapper;
import com.gaekdam.gaekdambe.global.exception.CustomException;
import com.gaekdam.gaekdambe.global.exception.ErrorCode;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoyaltyGradeQueryService {

    private final LoyaltyGradeMapper loyaltyGradeMapper;

    public List<LoyaltyGradeListQueryResponse> getLoyaltyGradeList(
            Long hotelGroupCode, String sortBy, String direction, String status) {
        SortRequest sortReq = new SortRequest();
        sortReq.setSortBy(sortBy);
        sortReq.setDirection(direction);

        return loyaltyGradeMapper.findLoyaltyGradeList(hotelGroupCode, sortReq, status);

    }

    public LoyaltyGradeDetailQueryResponse getLoyaltyGradeDetail(Long hotelGroupCode, Long loyaltyGradeCode) {
        LoyaltyGradeDetailQueryResponse loyaltyGradeDetail = loyaltyGradeMapper.findLoyaltyGradeDetail(hotelGroupCode,
                loyaltyGradeCode);
        if (loyaltyGradeDetail == null) {
            throw new CustomException(ErrorCode.LOYALTY_GRADE_NOT_FOUND);
        }
        return loyaltyGradeDetail;
    }

    private final com.gaekdam.gaekdambe.customer_service.loyalty.command.infrastructure.repository.LoyaltyGradeRepository loyaltyGradeRepository;
    private final com.gaekdam.gaekdambe.customer_service.membership.query.mapper.MembershipBatchMapper membershipBatchMapper;

    public com.gaekdam.gaekdambe.customer_service.loyalty.command.domain.entity.LoyaltyGrade calculateNextLoyaltyGrade(
            Long customerCode, Long hotelGroupCode) {

        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate startDate = today.minusYears(1);
        java.time.LocalDate endDate = today.minusDays(1);

        // 1. 실적 조회
        java.util.Map<String, Object> stats = membershipBatchMapper.selectCustomerStatistics(customerCode, startDate,
                endDate);

        java.math.BigDecimal totalAmount = java.math.BigDecimal.ZERO;
        if (stats.get("totalAmount") != null) {
            totalAmount = (java.math.BigDecimal) stats.get("totalAmount");
        }

        Long visitCount = 0L;
        if (stats.get("visitCount") != null) {
            visitCount = ((Number) stats.get("visitCount")).longValue();
        }

        // 2. 등급 정책 조회
        List<com.gaekdam.gaekdambe.customer_service.loyalty.command.domain.entity.LoyaltyGrade> allGrades = loyaltyGradeRepository
                .findAllByHotelGroup_HotelGroupCode(hotelGroupCode);

        allGrades.sort(java.util.Comparator
                .comparing(
                        com.gaekdam.gaekdambe.customer_service.loyalty.command.domain.entity.LoyaltyGrade::getLoyaltyTierLevel)
                .reversed());

        return findBestGrade(allGrades, totalAmount, visitCount);
    }

    private com.gaekdam.gaekdambe.customer_service.loyalty.command.domain.entity.LoyaltyGrade findBestGrade(
            List<com.gaekdam.gaekdambe.customer_service.loyalty.command.domain.entity.LoyaltyGrade> allGrades,
            java.math.BigDecimal amount, Long count) {
        for (com.gaekdam.gaekdambe.customer_service.loyalty.command.domain.entity.LoyaltyGrade grade : allGrades) {
            boolean amountMet = (grade.getLoyaltyCalculationAmount() == null)
                    || (amount.compareTo(java.math.BigDecimal.valueOf(grade.getLoyaltyCalculationAmount())) >= 0);

            boolean countMet = (grade.getLoyaltyCalculationCount() == null)
                    || (count >= grade.getLoyaltyCalculationCount());

            if (amountMet && countMet) {
                return grade;
            }
        }
        return !allGrades.isEmpty() ? allGrades.get(allGrades.size() - 1) : null;
    }
}
