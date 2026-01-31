package com.gaekdam.gaekdambe.hotel_service.position.query.mapper;

import com.gaekdam.gaekdambe.hotel_service.position.query.dto.response.HotelPositionListResponse;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface HotelPositionQueryMapper {
    List<HotelPositionListResponse> findByHotelGroupCode(Long hotelGroupCode);
}
