package com.gaekdam.gaekdambe.hotel_service.property.query.dto.request;

import com.gaekdam.gaekdambe.hotel_service.property.command.domain.PropertyStatus;

public record PropertySearchRequest(
    Long propertyCode,
    String propertyCity,
    String propertyName,
    PropertyStatus propertyStatus
) {

}
