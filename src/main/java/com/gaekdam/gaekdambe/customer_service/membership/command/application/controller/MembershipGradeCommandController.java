package com.gaekdam.gaekdambe.customer_service.membership.command.application.controller;

import com.gaekdam.gaekdambe.customer_service.membership.command.application.dto.request.MembershipGradeRequest;
import com.gaekdam.gaekdambe.customer_service.membership.command.application.service.MembershipGradeCommandService;
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

@Tag(name="멤버십 등급")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/membership-grade")
public class MembershipGradeCommandController {

  private final MembershipGradeCommandService memberShipGradeCommandService;

  //멤버십 등급 생성
  @PostMapping("")
  @PreAuthorize("hasAuthority('MEMBERSHIP_POLICY_CREATE')")
  @Operation(summary = "멤버십 등급 생성", description = "멤버십 등급을 생성 할 수 있다.")
  public ApiResponse<String> createMemberShipGrade(
      @AuthenticationPrincipal CustomUser employee,
      @Parameter(description = "멤버십 등급 내용") @RequestBody MembershipGradeRequest request

  ) {
    Long hotelGroupCode = employee.getHotelGroupCode();
    return ApiResponse.success(memberShipGradeCommandService.createMembershipGrade(request, hotelGroupCode));
  }

  //멤버십 등급 삭제
  @DeleteMapping("/{membershipGradeCode}")
  @PreAuthorize("hasAuthority('MEMBERSHIP_POLICY_DELETE')")
  @Operation(summary = "멤버십 등급 삭제", description = "멤버십 등급을 삭제 할 수 있다.")
  public ApiResponse<String> deleteMemberShipGrade(
      @AuthenticationPrincipal CustomUser employee,
      @Parameter(description = "멤버십 등급 코드") @PathVariable Long membershipGradeCode) {
    Long hotelGroupCode = employee.getHotelGroupCode();
    return ApiResponse
        .success(memberShipGradeCommandService.deleteMembershipGrade(hotelGroupCode, membershipGradeCode));
  }

  //멤버십 등급 수정
  @PutMapping("/{membershipGradeCode}")
  @PreAuthorize("hasAuthority('MEMBERSHIP_POLICY_UPDATE')")
  @Operation(summary = "멤버십 등급 수정", description = "멤버십 등급을 수정 할 수 있다.")
  public ApiResponse<String> updateMemberShipGrade(
      @AuthenticationPrincipal CustomUser employee,
      @Parameter(description = "멤버십 등급 코드") @PathVariable Long membershipGradeCode,
      @Parameter(description = "멤버십 등급 수정 내용")@RequestBody MembershipGradeRequest request) {
    Long hotelGroupCode = employee.getHotelGroupCode();
    return ApiResponse.success(memberShipGradeCommandService.updateMembershipGrade(hotelGroupCode, membershipGradeCode,
        request, employee.getUsername()));
  }

}
