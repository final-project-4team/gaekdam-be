package com.gaekdam.gaekdambe.customer_service.customer.query.service.model.row;

import java.time.LocalDateTime;

public record CustomerContactRow(
        Long contactCode,
        String contactType,
        String contactValue,
        Boolean isPrimary,
        Boolean marketingOptIn,
        LocalDateTime consentAt
) {
}
