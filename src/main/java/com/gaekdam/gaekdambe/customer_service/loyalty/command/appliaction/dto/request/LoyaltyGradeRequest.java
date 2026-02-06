package com.gaekdam.gaekdambe.customer_service.loyalty.command.appliaction.dto.request;

public record LoyaltyGradeRequest(
        String loyaltyGradeName,
        Long loyaltyTierLevel,
        String loyaltyTierComment,
        Long loyaltyCalculationAmount,
        Integer loyaltyCalculationCount) {

}
