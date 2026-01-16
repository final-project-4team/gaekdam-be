package com.gaekdam.gaekdambe.customer_service.loyalty.query.dto.response;

import com.gaekdam.gaekdambe.customer_service.loyalty.command.domain.LoyaltyGradeStatus;
import java.time.LocalDateTime;

public record LoyaltyGradeDetailQueryResponse(
    Long loyaltyGradeCode,
    String loyaltyGradeName,
    Long loyaltyTierLevel,
    String loyaltyTierComment,
    Long loyaltyCalculationAmount,
    Integer loyaltyCalculationCount,
    Integer loyaltyCalculationTermMonth,
    Integer loyaltyCalculationRenewalDay,
    LoyaltyGradeStatus loyaltyGradeStatus,
    LocalDateTime updatedAt
) {

}
