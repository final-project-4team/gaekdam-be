package com.gaekdam.gaekdambe.iam_service.employee.query.controller;

import com.gaekdam.gaekdambe.global.config.model.ApiResponse;
import com.gaekdam.gaekdambe.global.paging.PageResponse;
import com.gaekdam.gaekdambe.iam_service.employee.query.dto.response.EmployeeDetailResponse;
import com.gaekdam.gaekdambe.iam_service.employee.query.dto.response.EmployeeListResponse;
import com.gaekdam.gaekdambe.iam_service.employee.query.service.EmployeeQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/employee")
public class EmployeeQueryController {
  private final EmployeeQueryService employeeQueryService;

  @GetMapping("/detail/{employeeCode}")
  public ResponseEntity<ApiResponse<EmployeeDetailResponse>> getEmployee(@PathVariable Long employeeCode) {
    return ResponseEntity.ok(ApiResponse.success(employeeQueryService.getEmployeeDetail(employeeCode)));
  }

  @GetMapping("/search")
  public ResponseEntity<ApiResponse<PageResponse<EmployeeListResponse>>> searchEmployee(
      @RequestParam(required = false) String name,
      @RequestParam(required = false) String phone,
      @RequestParam(required = false) String email,
      Pageable pageable) {

    return ResponseEntity.ok(ApiResponse.success(employeeQueryService.searchEmployees(name, phone, email, pageable)));
  }
}
