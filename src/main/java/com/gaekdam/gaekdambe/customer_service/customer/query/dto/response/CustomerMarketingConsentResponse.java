package com.gaekdam.gaekdambe.customer_service.customer.query.dto.response;

import com.gaekdam.gaekdambe.customer_service.customer.command.domain.ContactType;

import java.time.LocalDateTime;
import java.util.List;

public record CustomerMarketingConsentResponse(
        Long customerCode,
        Long hotelGroupCode,
        List<Item> items
) {
    public static CustomerMarketingConsentResponse of(Long customerCode, Long hotelGroupCode, List<Item> items) {
        return new CustomerMarketingConsentResponse(customerCode, hotelGroupCode, items);
    }

    public record Item(
            Long contactCode,
            ContactType contactType,
            Boolean marketingOptIn,
            LocalDateTime consentAt,
            Boolean isPrimary
    ) {}
}
