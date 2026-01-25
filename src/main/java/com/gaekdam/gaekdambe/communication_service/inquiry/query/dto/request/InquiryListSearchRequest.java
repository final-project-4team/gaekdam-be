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
    private String keyword;  // 제목/내용 검색

    private LocalDate fromDate; // created_at >= fromDate 00:00:00
    private LocalDate toDate;   // created_at < (toDate+1) 00:00:00
}
