package com.gaekdam.gaekdambe.iam_service.employee.query.controller;

import com.gaekdam.gaekdambe.global.config.model.ApiResponse;
import com.gaekdam.gaekdambe.global.config.security.CustomUser;
import com.gaekdam.gaekdambe.global.paging.PageResponse;
import com.gaekdam.gaekdambe.iam_service.employee.query.dto.response.EmployeeDetailResponse;
import com.gaekdam.gaekdambe.iam_service.employee.query.dto.response.EmployeeListResponse;
import com.gaekdam.gaekdambe.iam_service.employee.query.service.EmployeeQueryService;
import lombok.RequiredArgsConstructor;
import com.gaekdam.gaekdambe.iam_service.employee.command.domain.EmployeeStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/employee")
public class EmployeeQueryController {
  private final EmployeeQueryService employeeQueryService;


  @PreAuthorize("hasAuthority('EMPLOYEE_READ')")
  @GetMapping("/detail/{employeeCode}")
  public ApiResponse<EmployeeDetailResponse> getEmployee(@PathVariable Long employeeCode) {
    return ApiResponse.success(employeeQueryService.getEmployeeDetail(employeeCode));
  }

  @PreAuthorize("hasAuthority('EMPLOYEE_LIST')")
  @GetMapping("")
  public ApiResponse<PageResponse<EmployeeListResponse>> searchEmployee(
      @AuthenticationPrincipal CustomUser employee,
      @RequestParam(required = false) String name,
      @RequestParam(required = false) String phone,
      @RequestParam(required = false) String email,
      @RequestParam(required = false) String departmentName,
      @RequestParam(required = false) String hotelPositionName,
      @RequestParam(required = false) EmployeeStatus employeeStatus,
      Pageable pageable) {

    Long hotelGroupCode=employee.getHotelGroupCode();

    return ApiResponse.success(employeeQueryService.searchEmployees(hotelGroupCode,name, phone, email,
        departmentName, hotelPositionName, employeeStatus, pageable));
  }
}
