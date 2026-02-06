package com.gaekdam.gaekdambe.customer_service.customer.query.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDetailRequest {

    private Long hotelGroupCode;
    private Long customerCode;
}
