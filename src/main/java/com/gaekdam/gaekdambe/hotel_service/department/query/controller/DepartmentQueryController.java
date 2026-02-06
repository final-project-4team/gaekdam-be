package com.gaekdam.gaekdambe.hotel_service.department.query.controller;

import com.gaekdam.gaekdambe.global.config.model.ApiResponse;
import com.gaekdam.gaekdambe.global.config.security.CustomUser;
import com.gaekdam.gaekdambe.hotel_service.department.query.dto.response.DepartmentListResponse;
import com.gaekdam.gaekdambe.hotel_service.department.query.service.DepartmentQueryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "부서")
@RestController
@RequestMapping("/api/v1/department")
@RequiredArgsConstructor
public class DepartmentQueryController {
  private final DepartmentQueryService departmentQueryService;

  // 부서명 리스트
  @GetMapping("")
  @Operation(summary = "부서 목록 조회", description = "호텔 그룹에 속한 부서 리스트를 조회합니다.")
  public ApiResponse<List<DepartmentListResponse>> getDepartmentList(
      @Parameter(hidden = true) @AuthenticationPrincipal CustomUser employee) {
    Long hotelGroupCode = employee.getHotelGroupCode();
    return ApiResponse.success(departmentQueryService.getDepartmentList(hotelGroupCode));
  }

}
