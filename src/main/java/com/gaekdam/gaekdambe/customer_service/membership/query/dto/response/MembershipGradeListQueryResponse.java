package com.gaekdam.gaekdambe.customer_service.membership.query.dto.response;

public record MembershipGradeListQueryResponse (
    Long membershipGradeCode,
    String gradeName,
    Long tierLevel,
    Long calculationAmount,
    Integer calculationCount,
    Integer calculationTermMonth,
    Integer calculationRenewalDay
){
}
