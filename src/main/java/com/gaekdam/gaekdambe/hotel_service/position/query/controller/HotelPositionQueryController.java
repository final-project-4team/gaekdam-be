package com.gaekdam.gaekdambe.hotel_service.position.query.controller;

import com.gaekdam.gaekdambe.global.config.model.ApiResponse;
import com.gaekdam.gaekdambe.global.config.security.CustomUser;
import com.gaekdam.gaekdambe.hotel_service.position.query.dto.response.HotelPositionListResponse;
import com.gaekdam.gaekdambe.hotel_service.position.query.service.HotelPositionQueryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/position")
@RequiredArgsConstructor
public class HotelPositionQueryController {

    private final HotelPositionQueryService hotelPositionQueryService;

    @GetMapping("")
    public ApiResponse<List<HotelPositionListResponse>> getHotelPositionList(
            @AuthenticationPrincipal CustomUser employee) {
        Long hotelGroupCode = employee.getHotelGroupCode();
        return ApiResponse.success(hotelPositionQueryService.getHotelPositionList(hotelGroupCode));
    }
}
