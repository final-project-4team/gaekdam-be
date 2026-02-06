package com.gaekdam.gaekdambe.hotel_service.property.query.dto.request;

import com.gaekdam.gaekdambe.hotel_service.property.command.domain.PropertyStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PropertySearchByHotelGroupRequest {

    // SaaS 스코프 (컨트롤러에서 주입)
    private Long hotelGroupCode;

    // 선택 필터 (추후 확장 대비)
    private PropertyStatus propertyStatus;
}