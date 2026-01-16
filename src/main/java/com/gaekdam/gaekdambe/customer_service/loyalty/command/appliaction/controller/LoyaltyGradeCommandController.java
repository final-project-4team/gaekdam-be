package com.gaekdam.gaekdambe.customer_service.loyalty.command.appliaction.controller;

import com.gaekdam.gaekdambe.customer_service.loyalty.command.appliaction.dto.request.LoyaltyGradeRequest;
import com.gaekdam.gaekdambe.customer_service.loyalty.command.appliaction.service.LoyaltyGradeCommandService;
import com.gaekdam.gaekdambe.global.config.model.ApiResponse;
import com.gaekdam.gaekdambe.global.config.security.CustomUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/loyalty-grade")
public class LoyaltyGradeCommandController {

  private final LoyaltyGradeCommandService loyaltyGradeCommandService;

  @PostMapping("")
  public ApiResponse<String> createLoyaltyGrade(
      @AuthenticationPrincipal CustomUser employee,
      @Valid @RequestBody LoyaltyGradeRequest request
  ) {
    Long hotelGroupCode = employee.getHotelGroupCode();
    return ApiResponse.success(loyaltyGradeCommandService.createLoyaltyGrade(request, hotelGroupCode));
  }

  @DeleteMapping("/{loyaltyGradeCode}")
  public ApiResponse<String> deleteLoyaltyGrade(
      @AuthenticationPrincipal CustomUser employee,
      @PathVariable Long loyaltyGradeCode) {
    Long hotelGroupCode = employee.getHotelGroupCode();
    return ApiResponse.success(loyaltyGradeCommandService.deleteLoyaltyGrade(hotelGroupCode, loyaltyGradeCode));
  }

  @PutMapping("/{loyaltyGradeCode}")
  public ApiResponse<String> updateLoyaltyGrade(
      @AuthenticationPrincipal CustomUser employee,
      @PathVariable Long loyaltyGradeCode,
       @RequestBody LoyaltyGradeRequest request) {
    Long hotelGroupCode = employee.getHotelGroupCode();
    return ApiResponse
        .success(loyaltyGradeCommandService.updateLoyaltyGrade(hotelGroupCode, loyaltyGradeCode, request));
  }

}
