package com.gaekdam.gaekdambe.customer_service.loyalty.query.controller;

import com.gaekdam.gaekdambe.customer_service.loyalty.query.dto.response.LoyaltyGradeDetailQueryResponse;
import com.gaekdam.gaekdambe.customer_service.loyalty.query.dto.response.LoyaltyGradeListQueryResponse;
import com.gaekdam.gaekdambe.customer_service.loyalty.query.service.LoyaltyGradeQueryService;
import com.gaekdam.gaekdambe.global.config.model.ApiResponse;
import com.gaekdam.gaekdambe.global.config.security.CustomUser;
import com.gaekdam.gaekdambe.iam_service.log.command.application.aop.annotation.AuditLog;
import com.gaekdam.gaekdambe.iam_service.permission_type.command.domain.seeds.PermissionTypeKey;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.annotations.Param;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/loyalty-grade")
public class LoyaltyGradeQueryController {
  private final LoyaltyGradeQueryService loyaltyGradeQueryService;
  @GetMapping("")
  @AuditLog(details = "", type = PermissionTypeKey.LOYALTY_POLICY_LIST)
  public ApiResponse<List<LoyaltyGradeListQueryResponse>> getLoyaltyGradeList(
      @AuthenticationPrincipal CustomUser employee,
      @Param("sortBy")  String sortBy,
      @Param("direction") String direction,
      @Param("STATUS")  String status
  ){
    Long hotelGroupCode= employee.getHotelGroupCode();
    return ApiResponse.success(loyaltyGradeQueryService.getLoyaltyGradeList(hotelGroupCode,sortBy,direction,status));
  }

  @GetMapping("/{loyaltyGradeCode}")
  @AuditLog(details = "", type = PermissionTypeKey.LOYALTY_POLICY_READ)
  public ApiResponse<LoyaltyGradeDetailQueryResponse> getLoyaltyGradeDetail(
      @AuthenticationPrincipal CustomUser employee,
      @PathVariable Long loyaltyGradeCode
  ){
    Long hotelCode= employee.getHotelGroupCode();
    return ApiResponse.success(loyaltyGradeQueryService.getLoyaltyGradeDetail(hotelCode,loyaltyGradeCode));
  }
}
