package com.gaekdam.gaekdambe.hotel_service.hotel.query.dto.response;

import java.time.LocalDateTime;

public record HotelGroupListResponse(
    Long hotelGroupCode,
    LocalDateTime hotelExpiredAt,
    String hotelGroupName
) {

}
