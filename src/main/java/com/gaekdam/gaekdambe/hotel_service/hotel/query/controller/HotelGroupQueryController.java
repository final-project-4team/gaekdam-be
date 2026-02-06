package com.gaekdam.gaekdambe.hotel_service.hotel.query.controller;

import com.gaekdam.gaekdambe.global.config.model.ApiResponse;
import com.gaekdam.gaekdambe.global.paging.PageResponse;
import com.gaekdam.gaekdambe.hotel_service.hotel.command.infrastructure.repository.HotelGroupRepository;
import com.gaekdam.gaekdambe.hotel_service.hotel.query.dto.request.HotelGroupQueryRequest;
import com.gaekdam.gaekdambe.hotel_service.hotel.query.dto.response.HotelGroupListResponse;
import com.gaekdam.gaekdambe.hotel_service.hotel.query.service.HotelGroupQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "호텔")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/hotel-group")
public class HotelGroupQueryController {

    private final HotelGroupQueryService hotelGroupQueryService;
    private final HotelGroupRepository hotelGroupRepository;

    @GetMapping("")
    @Operation(summary = "호텔 리스트 조회", description = "호텔 리스트를 조회합니다.")
    public ApiResponse<PageResponse<HotelGroupListResponse>> getHotelGroupList(
            HotelGroupQueryRequest query) {
        return ApiResponse.success(hotelGroupQueryService.getHotelGroupList(query));
    }

    @GetMapping("/{hotelGroupCode}")
    @Operation(summary = "호텔 명칭 조회", description = "특정 호텔의 명칭을 조회합니다.")
    public ApiResponse<String> getHotelNameById(
            @Parameter(description = "호텔 그룹 코드") @PathVariable Long hotelGroupCode) {

        String HotelName = hotelGroupRepository.findById(hotelGroupCode).orElseThrow().getHotelGroupName();

        return ApiResponse.success(HotelName);
    }
}
