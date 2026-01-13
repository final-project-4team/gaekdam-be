package com.gaekdam.gaekdambe.hotel_service.property.command.application.controller;

import com.gaekdam.gaekdambe.global.config.model.ApiResponse;
import com.gaekdam.gaekdambe.global.config.security.CustomUser;
import com.gaekdam.gaekdambe.hotel_service.property.command.application.dto.request.PropertyRequest;
import com.gaekdam.gaekdambe.hotel_service.property.command.application.service.PropertyCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/property")
public class PropertyCommandController {
  private final PropertyCommandService propertyCommandService;


  @PostMapping("")
  public ApiResponse<String> createProperty(
      @RequestBody PropertyRequest request,
      @AuthenticationPrincipal CustomUser customUser
  ) {
    Long hotelCode=customUser.getHotelGroupCode();
    return ApiResponse.success(propertyCommandService.createProperty(request,hotelCode));
  }

  @PutMapping("/{propertyCode}")
  public ApiResponse<String> updateProperty(
      @PathVariable Long propertyCode,
      @RequestBody PropertyRequest request
  ) {
    return ApiResponse.success(propertyCommandService.updateProperty(propertyCode,request));
  }
  @DeleteMapping("/{propertyCode}")
  public ApiResponse<String> deleteProperty(
      @PathVariable Long propertyCode
  ) {
    return ApiResponse.success(propertyCommandService.deleteProperty(propertyCode));
  }
}
