package com.gaekdam.gaekdambe.communication_service.inquiry.query.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class InquiryDetailResponse {

    private Long inquiryCode;
    private String inquiryStatus;

    private String inquiryTitle;
    private String inquiryContent;
    private String answerContent;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Long customerCode;
    private Long employeeCode;
    private String employeeLoginId;
    private String employeeName;

    private Long propertyCode;

    private Long inquiryCategoryCode;
    private String inquiryCategoryName;

    private Long linkedIncidentCode;

    private String customerName;
}
