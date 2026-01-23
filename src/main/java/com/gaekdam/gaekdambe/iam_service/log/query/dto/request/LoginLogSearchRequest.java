package com.gaekdam.gaekdambe.iam_service.log.query.dto.request;

import java.time.LocalDateTime;

public record LoginLogSearchRequest(
        Long hotelGroupCode,
        Long employeeCode,
        String loginId,
        String action,
        Boolean result,
        String userIp,
        LocalDateTime fromDate,
        LocalDateTime toDate) {
}
