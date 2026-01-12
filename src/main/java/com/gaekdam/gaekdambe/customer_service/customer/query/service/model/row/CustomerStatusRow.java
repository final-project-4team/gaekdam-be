package com.gaekdam.gaekdambe.customer_service.customer.query.service.model.row;

import com.gaekdam.gaekdambe.customer_service.customer.command.domain.CustomerStatus;

import java.time.LocalDateTime;

public record CustomerStatusRow(
        Long customerCode,
        CustomerStatus status,
        LocalDateTime cautionAt,
        LocalDateTime inactiveAt,
        LocalDateTime updatedAt
) {
}
