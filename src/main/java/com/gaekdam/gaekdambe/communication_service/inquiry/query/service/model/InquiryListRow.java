package com.gaekdam.gaekdambe.communication_service.inquiry.query.service.model.row;

import java.time.LocalDateTime;

public record InquiryListRow(
        Long inquiryCode,
        LocalDateTime createdAt,
        String inquiryTitle,
        String inquiryStatus,
        Long customerCode,
        Long employeeCode,
        Long propertyCode,
        Long inquiryCategoryCode,
        String inquiryCategoryName,
        Long linkedIncidentCode,
        byte[] customerNameEnc,
        byte[] dekEnc
) {}
