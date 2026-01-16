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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/property")
public class PropertyQueryController {
    private final PropertyQueryService propertyQueryService;
    private final PropertyRepository propertyRepository;
    private final HotelGroupRepository hotelGroupRepository;

    @GetMapping("")
    public ApiResponse<PageResponse<PropertyListResponse>> getPropertyList(
            PropertyQueryRequest query
    ) {
        return ApiResponse.success(propertyQueryService.getPropertyList(query));
    }

    @GetMapping("/me")
    public ApiResponse<MyPropertyResponse> getMyProperty(
            @AuthenticationPrincipal CustomUser customUser
    ) {
        Long propertyCode = customUser.getPropertyCode();
        Long hotelGroupCode = customUser.getHotelGroupCode();

        Property property = propertyRepository.findById(propertyCode)
                .orElseThrow();

        HotelGroup hotelGroup =  hotelGroupRepository.findById(hotelGroupCode)
                .orElseThrow();

        return ApiResponse.success(
                MyPropertyResponse.builder()
                        .propertyCode(property.getPropertyCode())
                        .propertyName(property.getPropertyName())
                        .hotelGroupCode(hotelGroup.getHotelGroupCode())
                        .hotelGroupName(hotelGroup.getHotelGroupName())
                        .build()
        );
    }


}
