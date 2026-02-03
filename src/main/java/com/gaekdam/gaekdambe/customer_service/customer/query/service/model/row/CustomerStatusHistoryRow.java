package com.gaekdam.gaekdambe.customer_service.customer.query.service.model.row;

import com.gaekdam.gaekdambe.customer_service.customer.command.domain.ChangeSource;
import com.gaekdam.gaekdambe.customer_service.customer.command.domain.CustomerStatus;

import java.time.LocalDateTime;

public record CustomerStatusHistoryRow(
        Long customerStatusHistoryCode,
        CustomerStatus beforeStatus,
        CustomerStatus afterStatus,
        ChangeSource changeSource,
        String changeReason,
        LocalDateTime changedAt,
        Long employeeCode,

        byte[] employeeNameEnc,
        byte[] employeeDekEnc
) {}

