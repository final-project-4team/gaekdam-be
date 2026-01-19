package com.gaekdam.gaekdambe.hotel_service.property.query.dto.request;

import com.gaekdam.gaekdambe.hotel_service.property.command.domain.PropertyStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Builder
public record PropertySearchRequest(
    Long propertyCode,
    String propertyCity,
    String propertyName,
    PropertyStatus propertyStatus
) {

}
