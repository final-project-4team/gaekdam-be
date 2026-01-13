package com.gaekdam.gaekdambe.hotel_service.hotel.query.dto.request;

public record HotelGroupQueryRequest(
        // Page
        Integer page,
        Integer size,
        // Search
        Long hotelGroupCode,
        String hotelGroupName,
        // Sort
        String sortBy,
        String direction) {
    public HotelGroupQueryRequest {
        if (page == null)
            page = 1;
        if (size == null)
            size = 20;
        if (sortBy == null)
            sortBy = "hotel_expired_at";
        if (direction == null)
            direction = "DESC";
    }
}
