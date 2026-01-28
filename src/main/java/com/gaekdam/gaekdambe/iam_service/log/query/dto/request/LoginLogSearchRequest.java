package com.gaekdam.gaekdambe.iam_service.log.query.dto.request;

import com.gaekdam.gaekdambe.iam_service.log.command.domain.LoginResult;
import java.time.LocalDateTime;

public record LoginLogSearchRequest(
                Long hotelGroupCode,
                Long employeeCode,
                String loginId,
                String action,
                LoginResult result,
                String userIp,
                LocalDateTime fromDate,
                LocalDateTime toDate) {
}
