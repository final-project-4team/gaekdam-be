package com.gaekdam.gaekdambe.customer_service.membership.query.controller;


import com.gaekdam.gaekdambe.customer_service.membership.query.dto.response.MembershipGradeDetailQueryResponse;
import com.gaekdam.gaekdambe.customer_service.membership.query.dto.response.MembershipGradeListQueryResponse;
import com.gaekdam.gaekdambe.customer_service.membership.query.service.MembershipGradeQueryService;
import com.gaekdam.gaekdambe.global.config.model.ApiResponse;
import com.gaekdam.gaekdambe.global.config.security.CustomUser;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
import com.gaekdam.gaekdambe.iam_service.log.command.application.aop.annotation.AuditLog;
import com.gaekdam.gaekdambe.iam_service.permission_type.command.domain.seeds.PermissionTypeKey;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
@Tag(name="멤버십 등급")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/membership-grade")
public class MembershipGradeQueryController {
  private final MembershipGradeQueryService membershipGradeQueryService;
  @GetMapping("")
  @AuditLog(details = "", type = PermissionTypeKey.MEMBERSHIP_POLICY_LIST)
  @Operation(summary = "멤버십 등급 리스트 조회", description = "멤버십 등급을 리스트 조회할 수 있다.")
  public ApiResponse<List<MembershipGradeListQueryResponse>> getMembershipGradeList(
      @AuthenticationPrincipal CustomUser employee,
      @Parameter(description = "멤버십 등급 상태 값")  @RequestParam(value = "status", required = false) String status,
      @Parameter(description = "정렬 기준") @ModelAttribute SortRequest sort
  ){
    Long hotelGroupCode= employee.getHotelGroupCode();
    return ApiResponse.success(membershipGradeQueryService.getMembershipGradeList(hotelGroupCode,sort,status));
  }

  @GetMapping("/{membershipGradeCode}")
  @PreAuthorize("hasAuthority('MEMBERSHIP_POLICY_READ')")
  @AuditLog(details = "", type = PermissionTypeKey.MEMBERSHIP_POLICY_READ)
  @Operation(summary = "멤버십 등급 상세 조회", description = "멤버십 등급을 상세 조회할 수 있다.")
  public ApiResponse<MembershipGradeDetailQueryResponse> getMembershipGradeDetail(
      @AuthenticationPrincipal CustomUser employee,
      @Parameter(description = "멤버십 등급 코드") @PathVariable Long membershipGradeCode
  ){
    Long hotelCode= employee.getHotelGroupCode();
    return ApiResponse.success(membershipGradeQueryService.getMembershipGradeDetail(hotelCode,membershipGradeCode));
  }
}
