package com.gaekdam.gaekdambe.communication_service.inquiry.query.service.model;

import java.time.LocalDateTime;

public record InquiryListRow(
        Long inquiryCode,
        LocalDateTime createdAt,
        String inquiryTitle,
        String inquiryStatus,
        Long customerCode,
        Long employeeCode,

        String employeeLoginId,
        byte[] employeeNameEnc,
        byte[] employeeDekEnc,

        Long propertyCode,
        Long inquiryCategoryCode,
        String inquiryCategoryName,
        Long linkedIncidentCode,

        byte[] customerNameEnc,
        byte[] dekEnc
) {}
