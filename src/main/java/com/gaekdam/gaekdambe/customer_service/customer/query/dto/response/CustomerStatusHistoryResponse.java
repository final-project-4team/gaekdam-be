package com.gaekdam.gaekdambe.customer_service.customer.query.dto.response;

import java.util.List;

public record CustomerStatusHistoryResponse(
        Long customerCode,
        Long hotelGroupCode,
        List<CustomerDetailResponse.StatusHistory> items
) {
    public static CustomerStatusHistoryResponse of(Long customerCode, Long hotelGroupCode, List<CustomerDetailResponse.StatusHistory> items) {
        return new CustomerStatusHistoryResponse(customerCode, hotelGroupCode, items);
    }
}
