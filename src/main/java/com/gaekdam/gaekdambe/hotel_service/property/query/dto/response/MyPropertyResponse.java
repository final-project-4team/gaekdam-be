package com.gaekdam.gaekdambe.hotel_service.property.query.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MyPropertyResponse {

    private Long propertyCode;
    private String propertyName;

    private Long hotelGroupCode;
    private String hotelGroupName;
}
