package com.gaekdam.gaekdambe.customer_service.customer.query.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerMemoSearchRequest {
    private Long hotelGroupCode;     // SaaS 스코프
    private Long customerCode;
    private Long customerMemoCode;   // 상세용
}
