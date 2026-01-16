package com.gaekdam.gaekdambe.customer_service.membership.query.controller;

import com.gaekdam.gaekdambe.customer_service.membership.query.dto.response.MembershipGradeDetailQueryResponse;
import com.gaekdam.gaekdambe.customer_service.membership.query.dto.response.MembershipGradeListQueryResponse;
import com.gaekdam.gaekdambe.customer_service.membership.query.service.MembershipGradeQueryService;
import com.gaekdam.gaekdambe.global.config.model.ApiResponse;
import com.gaekdam.gaekdambe.global.config.security.CustomUser;
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
@RequestMapping("/api/v1/membership-grade")
public class MembershipGradeQueryController {
  private final MembershipGradeQueryService membershipGradeQueryService;
  @GetMapping("")
  public ApiResponse<List<MembershipGradeListQueryResponse>> getMembershipGradeList(
      @AuthenticationPrincipal CustomUser employee,
      @Param("sortBy")  String sortBy,
      @Param("direction") String direction,
      @Param("STATUS")  String status
  ){
    Long hotelGroupCode= employee.getHotelGroupCode();
    return ApiResponse.success(membershipGradeQueryService.getMembershipGradeList(hotelGroupCode,sortBy,direction,status));
  }

  @GetMapping("/{membershipGradeCode}")
  public ApiResponse<MembershipGradeDetailQueryResponse> getMembershipGradeDetail(
      @AuthenticationPrincipal CustomUser employee,
      @PathVariable Long membershipGradeCode
  ){
    Long hotelCode= employee.getHotelGroupCode();
    return ApiResponse.success(membershipGradeQueryService.getMembershipGradeDetail(hotelCode,membershipGradeCode));
  }
}
