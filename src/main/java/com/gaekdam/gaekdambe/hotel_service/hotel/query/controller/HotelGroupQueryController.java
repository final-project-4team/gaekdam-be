package com.gaekdam.gaekdambe.hotel_service.hotel.query.controller;

import com.gaekdam.gaekdambe.global.config.model.ApiResponse;
import com.gaekdam.gaekdambe.global.paging.PageResponse;
import com.gaekdam.gaekdambe.hotel_service.hotel.query.dto.request.HotelGroupQueryRequest;
import com.gaekdam.gaekdambe.hotel_service.hotel.query.dto.response.HotelGroupListResponse;
import com.gaekdam.gaekdambe.hotel_service.hotel.query.service.HotelGroupQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/hotels")
public class HotelGroupQueryController {

  private final HotelGroupQueryService hotelGroupQueryService;

  @GetMapping("")
  public ApiResponse<PageResponse<HotelGroupListResponse>> getHotelGroupList(
      HotelGroupQueryRequest query) {
    return ApiResponse.success(hotelGroupQueryService.getHotelGroupList(query));
  }
}
