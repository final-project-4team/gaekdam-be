package com.gaekdam.gaekdambe.customer_service.loyalty.query.controller;

import com.gaekdam.gaekdambe.customer_service.loyalty.query.dto.response.LoyaltyGradeDetailQueryResponse;
import com.gaekdam.gaekdambe.customer_service.loyalty.query.dto.response.LoyaltyGradeListQueryResponse;
import com.gaekdam.gaekdambe.customer_service.loyalty.query.service.LoyaltyGradeQueryService;
import com.gaekdam.gaekdambe.global.config.model.ApiResponse;
import com.gaekdam.gaekdambe.global.config.security.CustomUser;
import com.gaekdam.gaekdambe.iam_service.log.command.application.aop.annotation.AuditLog;
import com.gaekdam.gaekdambe.iam_service.permission_type.command.domain.seeds.PermissionTypeKey;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.annotations.Param;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@Tag(name="로열티 등급")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/loyalty-grade")
public class LoyaltyGradeQueryController {
  private final LoyaltyGradeQueryService loyaltyGradeQueryService;
  @GetMapping("")
  @AuditLog(details = "", type = PermissionTypeKey.LOYALTY_POLICY_LIST)
  @Operation(summary = "로열티 등급 리스트 조회", description = "관리자는 로열티 등급을 리스트 조회할 수 있다.")
  public ApiResponse<List<LoyaltyGradeListQueryResponse>> getLoyaltyGradeList(
      @AuthenticationPrincipal CustomUser employee,
      @Parameter(description = "정렬 값") @Param("sortBy")  String sortBy,
      @Parameter(description = "정렬 방향") @Param("direction") String direction,
      @Parameter(description = "로열티 등급 상태") @Param("STATUS")  String status
  ){
    Long hotelGroupCode= employee.getHotelGroupCode();
    return ApiResponse.success(loyaltyGradeQueryService.getLoyaltyGradeList(hotelGroupCode,sortBy,direction,status));
  }

  @GetMapping("/{loyaltyGradeCode}")
  @PreAuthorize("hasAuthority('LOYALTY_POLICY_READ')")
  @AuditLog(details = "", type = PermissionTypeKey.LOYALTY_POLICY_READ)
  @Operation(summary = "로열티 등급 상세 조회", description = "관리자는 로열티 등급을 상세 조회 할 수 있다.")
  public ApiResponse<LoyaltyGradeDetailQueryResponse> getLoyaltyGradeDetail(
      @AuthenticationPrincipal CustomUser employee,
      @Parameter(description="로열티 등급 코드") @PathVariable Long loyaltyGradeCode
  ){
    Long hotelCode= employee.getHotelGroupCode();
    return ApiResponse.success(loyaltyGradeQueryService.getLoyaltyGradeDetail(hotelCode,loyaltyGradeCode));
  }
}
