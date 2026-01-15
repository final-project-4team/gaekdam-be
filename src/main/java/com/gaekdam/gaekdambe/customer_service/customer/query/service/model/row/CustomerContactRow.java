package com.gaekdam.gaekdambe.customer_service.customer.query.service.model.row;

import com.gaekdam.gaekdambe.customer_service.customer.command.domain.ContactType;

import java.time.LocalDateTime;

public record CustomerContactRow(
        Long contactCode,
        ContactType contactType,
        byte[] contactValueEnc,
        Boolean isPrimary,
        Boolean marketingOptIn,
        LocalDateTime consentAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
