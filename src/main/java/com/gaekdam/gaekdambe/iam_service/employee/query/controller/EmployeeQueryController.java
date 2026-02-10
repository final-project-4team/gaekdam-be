package com.gaekdam.gaekdambe.iam_service.employee.query.controller;

import com.gaekdam.gaekdambe.global.config.model.ApiResponse;
import com.gaekdam.gaekdambe.global.config.security.CustomUser;
import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.PageResponse;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
import com.gaekdam.gaekdambe.iam_service.employee.query.dto.request.EmployeeQuerySearchRequest;
import com.gaekdam.gaekdambe.iam_service.employee.query.dto.response.EmployeeDetailResponse;
import com.gaekdam.gaekdambe.iam_service.employee.query.dto.response.EmployeeListResponse;
import com.gaekdam.gaekdambe.iam_service.employee.query.service.EmployeeQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
@Tag(name="직원")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/employee")
public class EmployeeQueryController {

  private final EmployeeQueryService employeeQueryService;

  // 다른 직원 상세 조회
  @PreAuthorize("hasAuthority('EMPLOYEE_READ')")
  @GetMapping("/detail/{employeeCode}")
  @Operation(summary = "직원 상세 조회", description = "특정 직원을 상세 조회한다.")
  public ApiResponse<EmployeeDetailResponse> getEmployee(
      @AuthenticationPrincipal CustomUser employee,
      @Parameter(description = "직원 코드") @PathVariable Long employeeCode,
      @Parameter(description = "조회 사유") @RequestParam(required = false) String reason) {
    Long hotelGroupCode = employee.getHotelGroupCode();
    return ApiResponse.success(employeeQueryService.getEmployeeDetail(hotelGroupCode, employeeCode, reason));
  }

  // 직원 리스트 조회
  @PreAuthorize("hasAnyAuthority('EMPLOYEE_LIST','INCIDENT_CREATE')")
  @GetMapping("")
  @Operation(summary = "직원 리스트 조회", description = "직원 리스트를 조회한다.")
  public ApiResponse<PageResponse<EmployeeListResponse>> searchEmployee(
      @AuthenticationPrincipal CustomUser employee,
      PageRequest page,
      EmployeeQuerySearchRequest search,
      SortRequest sort) {

    Long hotelGroupCode = employee.getHotelGroupCode();
    if (sort == null || sort.getSortBy() == null) {
      sort = new SortRequest();
      sort.setSortBy("created_at");
      sort.setDirection("DESC");
    }
    return ApiResponse.success(
        employeeQueryService.searchEmployees(hotelGroupCode, search, page, sort));
  }

  // 마이페이지
  @GetMapping("/detail")
  @Operation(summary = "마이페이지", description = "직원은 본인의 상세정보를 조회할 수 있다,")
  public ApiResponse<EmployeeDetailResponse> getMyPage(
      @AuthenticationPrincipal CustomUser employee) {
    Long hotelGroupCode = employee.getHotelGroupCode();
    String loginId = employee.getUsername();
    return ApiResponse.success(employeeQueryService.getMyPage(hotelGroupCode, loginId));
  }
}
