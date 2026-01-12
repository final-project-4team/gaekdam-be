package com.gaekdam.gaekdambe.iam_service.employee.query.controller;

import com.gaekdam.gaekdambe.global.config.model.ApiResponse;
import com.gaekdam.gaekdambe.global.paging.PageResponse;
import com.gaekdam.gaekdambe.iam_service.employee.query.dto.response.EmployeeDetailResponse;
import com.gaekdam.gaekdambe.iam_service.employee.query.dto.response.EmployeeListResponse;
import com.gaekdam.gaekdambe.iam_service.employee.query.service.EmployeeQueryService;
import lombok.RequiredArgsConstructor;
import com.gaekdam.gaekdambe.iam_service.employee.command.domain.EmployeeStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/employee")
public class EmployeeQueryController {
  private final EmployeeQueryService employeeQueryService;


  @PreAuthorize("hasAuthority('CUSTOMER_LIST_READ')")
  @GetMapping("/detail/{employeeCode}")
  public ResponseEntity<ApiResponse<EmployeeDetailResponse>> getEmployee(@PathVariable Long employeeCode) {
    return ResponseEntity.ok(ApiResponse.success(employeeQueryService.getEmployeeDetail(employeeCode)));
  }

  @GetMapping("/search")
  public ResponseEntity<ApiResponse<PageResponse<EmployeeListResponse>>> searchEmployee(
      @RequestParam(required = false) String name,
      @RequestParam(required = false) String phone,
      @RequestParam(required = false) String email,
      @RequestParam(required = false) String departmentName,
      @RequestParam(required = false) String hotelPositionName,
      @RequestParam(required = false) EmployeeStatus employeeStatus,
      Pageable pageable) {

    return ResponseEntity.ok(ApiResponse.success(employeeQueryService.searchEmployees(name, phone, email,
        departmentName, hotelPositionName, employeeStatus, pageable)));
  }
}
