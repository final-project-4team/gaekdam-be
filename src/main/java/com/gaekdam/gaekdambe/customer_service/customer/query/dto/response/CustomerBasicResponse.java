package com.gaekdam.gaekdambe.customer_service.customer.query.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CustomerBasicResponse {

    private Long customerCode;
    private String customerName;
    private String phoneNumber;
}