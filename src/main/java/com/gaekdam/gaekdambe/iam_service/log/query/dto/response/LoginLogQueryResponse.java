package com.gaekdam.gaekdambe.iam_service.log.query.dto.response;

import java.time.LocalDateTime;

public record LoginLogQueryResponse(
        Long loginLogCode,
        String action,
        Long employeeCode,
        String employeeName,
        String loginId,
        String userIp,
        LocalDateTime occurredAt,
        Boolean result,
        String failedReason,
        Long hotelGroupCode) {
}
