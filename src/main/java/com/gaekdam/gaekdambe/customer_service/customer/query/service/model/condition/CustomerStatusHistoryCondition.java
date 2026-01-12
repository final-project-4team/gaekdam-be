package com.gaekdam.gaekdambe.customer_service.customer.query.service.model.condition;

public record CustomerStatusHistoryCondition(
        Long hotelGroupCode,
        Long customerCode,
        Integer offset,
        Integer limit,
        String sortBy,
        String direction
) {
}
