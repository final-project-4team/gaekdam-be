package com.gaekdam.gaekdambe.communication_service.inquiry.query.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InquiryListSearchRequest {

    private Long customerCode;
    private Long hotelGroupCode; // 컨트롤러에서 세팅

    private Long propertyCode;
    private Long inquiryCategoryCode;

    private String status;   // IN_PROGRESS / ANSWERED

    // 검색
    private String searchType;
    private String keyword;

    private String customerNameHash;
    private byte[] employeeNameHash;
    private String employeeLoginId;

    private LocalDate fromDate;
    private LocalDate toDate;
}
