package com.gaekdam.gaekdambe.hotel_service.property.query.controller;

import com.gaekdam.gaekdambe.global.config.model.ApiResponse;
import com.gaekdam.gaekdambe.global.config.security.CustomUser;
import com.gaekdam.gaekdambe.global.paging.PageResponse;
import com.gaekdam.gaekdambe.hotel_service.hotel.command.domain.entity.HotelGroup;
import com.gaekdam.gaekdambe.hotel_service.hotel.command.infrastructure.repository.HotelGroupRepository;
import com.gaekdam.gaekdambe.hotel_service.property.command.domain.entity.Property;
import com.gaekdam.gaekdambe.hotel_service.property.command.infrastructure.PropertyRepository;
import com.gaekdam.gaekdambe.hotel_service.property.query.dto.request.PropertyQueryRequest;
import com.gaekdam.gaekdambe.hotel_service.property.query.dto.response.MyPropertyResponse;
import com.gaekdam.gaekdambe.hotel_service.property.query.dto.response.PropertyListResponse;
import com.gaekdam.gaekdambe.hotel_service.property.query.service.PropertyQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "지점 조회")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/property")
public class PropertyQueryController {
        private final PropertyQueryService propertyQueryService;
        private final PropertyRepository propertyRepository;
        private final HotelGroupRepository hotelGroupRepository;

        @GetMapping("")
        @Operation(summary = "지점 목록 조회", description = "지점 목록을 페이징하여 조회합니다.")
        public ApiResponse<PageResponse<PropertyListResponse>> getPropertyList(
                        PropertyQueryRequest query) {
                return ApiResponse.success(propertyQueryService.getPropertyList(query));
        }

        @GetMapping("/me")
        @Operation(summary = "내 지점 정보 조회", description = "로그인한 사용자의 지점 및 호텔 그룹 정보를 조회합니다.")
        public ApiResponse<MyPropertyResponse> getMyProperty(
                        @Parameter(hidden = true) @AuthenticationPrincipal CustomUser customUser) {
                Long propertyCode = customUser.getPropertyCode();
                Long hotelGroupCode = customUser.getHotelGroupCode();

                Property property = propertyRepository.findById(propertyCode)
                                .orElseThrow();

                HotelGroup hotelGroup = hotelGroupRepository.findById(hotelGroupCode)
                                .orElseThrow();

                return ApiResponse.success(
                                MyPropertyResponse.builder()
                                                .propertyCode(property.getPropertyCode())
                                                .propertyName(property.getPropertyName())
                                                .hotelGroupCode(hotelGroup.getHotelGroupCode())
                                                .hotelGroupName(hotelGroup.getHotelGroupName())
                                                .build());
        }

        @GetMapping("/by-hotel-group")
        @Operation(summary = "호텔 그룹별 지점 목록 조회", description = "로그인한 사용자의 호텔 그룹에 속한 모든 지점 목록을 조회합니다.")
        public ApiResponse<List<PropertyListResponse>> getPropertyByHotelGroup(
                        @Parameter(hidden = true) @AuthenticationPrincipal CustomUser customUser) {
                return ApiResponse.success(
                                propertyQueryService.getPropertiesByHotelGroup(
                                                customUser.getHotelGroupCode()));
        }

}
