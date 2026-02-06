package com.gaekdam.gaekdambe.hotel_service.position.query.dto.response;

public record HotelPositionListResponse(
        Long hotelPositionCode,
        String hotelPositionName,
        Long departmentCode) {
}
