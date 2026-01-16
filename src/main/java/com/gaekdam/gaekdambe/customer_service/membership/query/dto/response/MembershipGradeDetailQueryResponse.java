package com.gaekdam.gaekdambe.customer_service.membership.query.dto.response;

import com.gaekdam.gaekdambe.customer_service.membership.command.domain.MembershipGradeStatus;
import java.time.LocalDateTime;

public record MembershipGradeDetailQueryResponse(
    Long membershipGradeCode,
    String gradeName,
    Long tierLevel,
    String tierComment,
    Long calculationAmount,
    Integer calculationCount,
    Integer calculationTermMonth,
    Integer calculationRenewalDay,
    MembershipGradeStatus membershipGradeStatus,
    LocalDateTime updatedAt
) {

}
