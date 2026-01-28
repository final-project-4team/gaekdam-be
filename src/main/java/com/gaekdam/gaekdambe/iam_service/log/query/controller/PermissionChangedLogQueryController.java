package com.gaekdam.gaekdambe.iam_service.log.query.controller;

import com.gaekdam.gaekdambe.global.config.model.ApiResponse;
import com.gaekdam.gaekdambe.global.config.security.CustomUser;
import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.PageResponse;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
import com.gaekdam.gaekdambe.iam_service.log.query.dto.request.PermissionChangedLogSearchRequest;
import com.gaekdam.gaekdambe.iam_service.log.query.dto.response.PermissionChangedLogQueryResponse;
import com.gaekdam.gaekdambe.iam_service.log.query.service.PermissionChangedLogQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/logs/permission-changed")
public class PermissionChangedLogQueryController {

  private final PermissionChangedLogQueryService permissionChangedLogQueryService;

  @PreAuthorize("hasAuthority('LOG_PERMISSION_CHANGED_LIST')")
  @GetMapping
  public ResponseEntity<ApiResponse<PageResponse<PermissionChangedLogQueryResponse>>> getPermissionChangedLogs(
      @AuthenticationPrincipal CustomUser employee,
      PageRequest page,
      PermissionChangedLogSearchRequest search,
      SortRequest sort) {
    Long hotelGroupCode = employee.getHotelGroupCode();

    PageResponse<PermissionChangedLogQueryResponse> response = permissionChangedLogQueryService.getPermissionChangedLogs(
        hotelGroupCode, page, search,
        sort);

    return ResponseEntity.ok(ApiResponse.success(response));
  }
}
