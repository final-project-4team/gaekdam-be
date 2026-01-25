package com.gaekdam.gaekdambe.communication_service.inquiry.query.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class InquiryListResponse {

    private Long inquiryCode;
    private LocalDateTime createdAt;

    private String inquiryTitle;
    private String inquiryStatus;

    private Long customerCode;

    private Long employeeCode;

    private String employeeLoginId; // 담당자 로그인ID
    private String employeeName; // 담당자명(복호화된 값)

    private Long propertyCode;

    private Long inquiryCategoryCode;
    private String inquiryCategoryName;

    private Long linkedIncidentCode;

    private String customerName;
}
