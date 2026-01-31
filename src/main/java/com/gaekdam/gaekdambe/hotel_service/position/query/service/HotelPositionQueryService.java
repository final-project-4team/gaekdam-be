package com.gaekdam.gaekdambe.hotel_service.position.query.service;

import com.gaekdam.gaekdambe.hotel_service.position.query.dto.response.HotelPositionListResponse;
import com.gaekdam.gaekdambe.hotel_service.position.query.mapper.HotelPositionQueryMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HotelPositionQueryService {
    private final HotelPositionQueryMapper hotelPositionQueryMapper;

    public List<HotelPositionListResponse> getHotelPositionList(Long hotelGroupCode) {
        return hotelPositionQueryMapper.findByHotelGroupCode(hotelGroupCode);
    }
}
