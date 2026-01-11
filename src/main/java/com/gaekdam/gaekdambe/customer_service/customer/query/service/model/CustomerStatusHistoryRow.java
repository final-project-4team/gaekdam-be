package com.gaekdam.gaekdambe.customer_service.customer.query.service.model;

import com.gaekdam.gaekdambe.customer_service.customer.command.domain.ChangeSource;
import com.gaekdam.gaekdambe.customer_service.customer.command.domain.CustomerStatus;

import java.time.LocalDateTime;

public record CustomerStatusHistoryRow(
        Long customerStatusHistoryCode,
        CustomerStatus beforeStatus,
        CustomerStatus afterStatus,
        ChangeSource changeSource,
        Long changedByMemberCode,
        String reason,
        LocalDateTime changedAt
) {}
