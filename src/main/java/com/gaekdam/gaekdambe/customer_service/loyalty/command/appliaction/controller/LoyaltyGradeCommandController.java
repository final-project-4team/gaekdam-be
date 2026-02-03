package com.gaekdam.gaekdambe.customer_service.loyalty.command.appliaction.controller;

import com.gaekdam.gaekdambe.customer_service.loyalty.command.appliaction.dto.request.LoyaltyGradeRequest;
import com.gaekdam.gaekdambe.customer_service.loyalty.command.appliaction.service.LoyaltyGradeCommandService;
import com.gaekdam.gaekdambe.global.config.model.ApiResponse;
import com.gaekdam.gaekdambe.global.config.security.CustomUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@Tag(name="로열티 등급")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/loyalty-grade")
public class LoyaltyGradeCommandController {

  private final LoyaltyGradeCommandService loyaltyGradeCommandService;

  @PostMapping("")
  @PreAuthorize("hasAuthority('LOYALTY_POLICY_CREATE')")
  @Operation(summary = "로열티 등급 생성", description = "관리자는 로열티 등급을 생성할 수 있다.")
  public ApiResponse<String> createLoyaltyGrade(
      @AuthenticationPrincipal CustomUser employee,
      @Parameter(description = "로열티 등급 내용") @Valid @RequestBody LoyaltyGradeRequest request
  ) {
    Long hotelGroupCode = employee.getHotelGroupCode();
    return ApiResponse.success(loyaltyGradeCommandService.createLoyaltyGrade(request, hotelGroupCode));
  }

  @DeleteMapping("/{loyaltyGradeCode}")
  @PreAuthorize("hasAuthority('LOYALTY_POLICY_DELETE')")
  @Operation(summary = "로열티 등급 삭제", description = "관리자는 로열티 등급을 삭제할 수 있다.")
  public ApiResponse<String> deleteLoyaltyGrade(
      @AuthenticationPrincipal CustomUser employee,
      @Parameter(description = "로열티 등급 코드") @PathVariable Long loyaltyGradeCode) {
    Long hotelGroupCode = employee.getHotelGroupCode();
    return ApiResponse.success(loyaltyGradeCommandService.deleteLoyaltyGrade(hotelGroupCode, loyaltyGradeCode));
  }

  @PutMapping("/{loyaltyGradeCode}")
  @PreAuthorize("hasAuthority('LOYALTY_POLICY_UPDATE')")
  @Operation(summary = "로열티 등급 수정", description = "관리자는 로열티 등급을 수정할 수 있다.")
  public ApiResponse<String> updateLoyaltyGrade(
      @AuthenticationPrincipal CustomUser employee,
      @Parameter(description = "로열티 등급 코드") @PathVariable Long loyaltyGradeCode,
      @Parameter(description = "로열티 등급 수정 내용")@RequestBody LoyaltyGradeRequest request) {
    Long hotelGroupCode = employee.getHotelGroupCode();
    String accessorLoingId=employee.getUsername();
    return ApiResponse
        .success(loyaltyGradeCommandService.updateLoyaltyGrade(hotelGroupCode, loyaltyGradeCode, request,accessorLoingId));
  }

}
