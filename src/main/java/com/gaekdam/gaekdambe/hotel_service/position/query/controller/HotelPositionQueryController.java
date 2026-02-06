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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "직급")
@RestController
@RequestMapping("/api/v1/position")
@RequiredArgsConstructor
public class HotelPositionQueryController {

    private final HotelPositionQueryService hotelPositionQueryService;

    @GetMapping("")
    @Operation(summary = "직급 리스트 조회", description = "호텔에 속한 직급 리스트를 조회합니다.")
    public ApiResponse<List<HotelPositionListResponse>> getHotelPositionList(
            @AuthenticationPrincipal CustomUser employee) {
        Long hotelGroupCode = employee.getHotelGroupCode();
        return ApiResponse.success(hotelPositionQueryService.getHotelPositionList(hotelGroupCode));
    }
}
