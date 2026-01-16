package com.gaekdam.gaekdambe.customer_service.loyalty.query.dto.response;

public record LoyaltyGradeListQueryResponse(
    Long loyaltyGradeCode,
    String loyaltyGradeName,
    Long loyaltyTierLevel,
    Long loyaltyCalculationAmount,
    Integer loyaltyCalculationCount,
    Integer loyaltyCalculationTermMonth,
    Integer loyaltyCalculationRenewalDay
){
}
