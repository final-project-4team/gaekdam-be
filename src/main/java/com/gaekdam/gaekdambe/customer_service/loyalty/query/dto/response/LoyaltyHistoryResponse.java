package com.gaekdam.gaekdambe.customer_service.loyalty.query.dto.response;

import java.time.LocalDateTime;

public record LoyaltyHistoryResponse(
        LocalDateTime changedAt,
        String changeType,          // 가입/등급변경/상태변경
        String content,
        String changeSource,        // SYSTEM/MANUAL (변경 출처)
        Long changedByEmployeeCode
) {}


