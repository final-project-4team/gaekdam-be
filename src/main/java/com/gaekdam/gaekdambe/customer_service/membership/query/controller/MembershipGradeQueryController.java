package com.gaekdam.gaekdambe.customer_service.membership.query.controller;


import com.gaekdam.gaekdambe.customer_service.membership.query.dto.response.MembershipGradeDetailQueryResponse;
import com.gaekdam.gaekdambe.customer_service.membership.query.dto.response.MembershipGradeListQueryResponse;
import com.gaekdam.gaekdambe.customer_service.membership.query.service.MembershipGradeQueryService;
import com.gaekdam.gaekdambe.global.config.model.ApiResponse;
import com.gaekdam.gaekdambe.global.config.security.CustomUser;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
import com.gaekdam.gaekdambe.iam_service.log.command.application.aop.annotation.AuditLog;
import com.gaekdam.gaekdambe.iam_service.permission_type.command.domain.seeds.PermissionTypeKey;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/membership-grade")
public class MembershipGradeQueryController {
  private final MembershipGradeQueryService membershipGradeQueryService;
  @GetMapping("")
  @PreAuthorize("hasAuthority('MEMBERSHIP_POLICY_LIST')")
  @AuditLog(details = "", type = PermissionTypeKey.MEMBERSHIP_POLICY_LIST)
  public ApiResponse<List<MembershipGradeListQueryResponse>> getMembershipGradeList(
      @AuthenticationPrincipal CustomUser employee,
      @RequestParam(value = "status", required = false) String status,
      @ModelAttribute SortRequest sort
  ){
    Long hotelGroupCode= employee.getHotelGroupCode();
    return ApiResponse.success(membershipGradeQueryService.getMembershipGradeList(hotelGroupCode,sort,status));
  }

  @GetMapping("/{membershipGradeCode}")
  @PreAuthorize("hasAuthority('MEMBERSHIP_POLICY_READ')")
  @AuditLog(details = "", type = PermissionTypeKey.MEMBERSHIP_POLICY_READ)
  public ApiResponse<MembershipGradeDetailQueryResponse> getMembershipGradeDetail(
      @AuthenticationPrincipal CustomUser employee,
      @PathVariable Long membershipGradeCode
  ){
    Long hotelCode= employee.getHotelGroupCode();
    return ApiResponse.success(membershipGradeQueryService.getMembershipGradeDetail(hotelCode,membershipGradeCode));
  }
}
