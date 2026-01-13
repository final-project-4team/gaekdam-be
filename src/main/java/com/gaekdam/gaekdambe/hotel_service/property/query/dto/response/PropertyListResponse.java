package com.gaekdam.gaekdambe.hotel_service.property.query.dto.response;

import com.gaekdam.gaekdambe.hotel_service.property.command.domain.PropertyStatus;

public record PropertyListResponse(
    Long propertyCode,
    String propertyCity,
    String propertyName,
    PropertyStatus propertyStatus,
    String hotelGroupName

) {

}
