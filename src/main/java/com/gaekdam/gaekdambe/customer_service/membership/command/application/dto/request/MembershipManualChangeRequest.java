package com.gaekdam.gaekdambe.customer_service.membership.command.application.dto.request;

import com.gaekdam.gaekdambe.customer_service.membership.command.domain.MembershipStatus;
import java.time.LocalDateTime;

public record MembershipManualChangeRequest(
        Long membershipGradeCode,
        MembershipStatus membershipStatus,
        LocalDateTime expiredAt,
        String changeReason,
        Long employeeCode
) {
}
