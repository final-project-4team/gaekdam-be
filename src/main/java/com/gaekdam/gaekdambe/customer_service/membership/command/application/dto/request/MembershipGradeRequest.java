package com.gaekdam.gaekdambe.customer_service.membership.command.application.dto.request;

public record MembershipGradeRequest(
    String gradeName,
    Long tierLevel,
    String tierComment,
    Long calculationAmount,
    Integer calculationCount
) {

}
