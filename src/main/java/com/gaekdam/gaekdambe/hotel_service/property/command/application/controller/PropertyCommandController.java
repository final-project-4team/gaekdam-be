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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "지점")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/property")
public class PropertyCommandController {
  private final PropertyCommandService propertyCommandService;

  @PostMapping("")
  @Operation(summary = "지점 생성", description = "새로운 지점을 생성합니다.")
  public ApiResponse<String> createProperty(
      @RequestBody PropertyRequest request,
      @AuthenticationPrincipal CustomUser customUser) {
    Long hotelCode = customUser.getHotelGroupCode();
    return ApiResponse.success(propertyCommandService.createProperty(request, hotelCode));
  }

  @PutMapping("/{propertyCode}")
  @Operation(summary = "지점 수정", description = "특정 지점 정보를 수정합니다.")
  public ApiResponse<String> updateProperty(
      @Parameter(description = "지점 코드") @PathVariable Long propertyCode,
      @RequestBody PropertyRequest request) {
    return ApiResponse.success(propertyCommandService.updateProperty(propertyCode, request));
  }

  @DeleteMapping("/{propertyCode}")
  @Operation(summary = "지점 삭제", description = "특정 지점을 삭제합니다.")
  public ApiResponse<String> deleteProperty(
      @Parameter(description = "지점 코드") @PathVariable Long propertyCode) {
    return ApiResponse.success(propertyCommandService.deleteProperty(propertyCode));
  }
}
