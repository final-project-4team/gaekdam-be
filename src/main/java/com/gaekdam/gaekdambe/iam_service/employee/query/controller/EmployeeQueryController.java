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
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/employee")
public class EmployeeQueryController {

  private final EmployeeQueryService employeeQueryService;

  // 다른 직원 상세 조회
  @PreAuthorize("hasAuthority('EMPLOYEE_READ')")
  @GetMapping("/detail/{employeeCode}")
  public ApiResponse<EmployeeDetailResponse> getEmployee(
      @AuthenticationPrincipal CustomUser employee,
      @PathVariable Long employeeCode,
      @RequestParam(required = false) String reason) {
    Long hotelGroupCode = employee.getHotelGroupCode();
    return ApiResponse.success(employeeQueryService.getEmployeeDetail(hotelGroupCode, employeeCode, reason));
  }

  // 직원 리스트 조회
  @PreAuthorize("hasAuthority('EMPLOYEE_LIST')")
  @GetMapping("")
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

  // 직원 상세 조회
  @GetMapping("/detail")
  public ApiResponse<EmployeeDetailResponse> getMyPage(
      @AuthenticationPrincipal CustomUser employee) {
    Long hotelGroupCode = employee.getHotelGroupCode();
    String loginId = employee.getUsername();
    return ApiResponse.success(employeeQueryService.getMyPage(hotelGroupCode, loginId));
  }
}
