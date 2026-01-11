package com.gaekdam.gaekdambe.customer_service.customer.query.dto.response;

import com.gaekdam.gaekdambe.customer_service.customer.command.domain.CustomerStatus;

import java.time.LocalDateTime;

public record CustomerStatusResponse(
        Long customerCode,
        Long hotelGroupCode,
        CustomerStatus status,
        LocalDateTime cautionAt,
        LocalDateTime inactiveAt,
        LocalDateTime updatedAt
) {}
