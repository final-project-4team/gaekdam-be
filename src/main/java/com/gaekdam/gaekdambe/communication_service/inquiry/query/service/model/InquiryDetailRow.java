package com.gaekdam.gaekdambe.communication_service.inquiry.query.service.model.row;

import java.time.LocalDateTime;

public record InquiryDetailRow(
        Long inquiryCode,
        String inquiryStatus,
        String inquiryTitle,
        String inquiryContent,
        String answerContent,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Long customerCode,
        Long employeeCode,
        Long propertyCode,
        Long inquiryCategoryCode,
        String inquiryCategoryName,
        Long linkedIncidentCode,
        byte[] customerNameEnc,
        byte[] dekEnc
) {}
