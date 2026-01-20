package com.gaekdam.gaekdambe.customer_service.customer.query.dto.response;

import java.time.LocalDateTime;

public record CustomerMemoResponse(
        Long customerMemoCode,
        Long customerCode,
        Long employeeCode,
        String customerMemoContent,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
