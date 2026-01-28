package com.gaekdam.gaekdambe.iam_service.log.query.controller;

import com.gaekdam.gaekdambe.global.config.model.ApiResponse;
import com.gaekdam.gaekdambe.global.config.security.CustomUser;
import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.PageResponse;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
import com.gaekdam.gaekdambe.iam_service.log.query.dto.request.AuditLogSearchRequest;
import com.gaekdam.gaekdambe.iam_service.log.query.dto.response.AuditLogQueryResponse;
import com.gaekdam.gaekdambe.iam_service.log.query.service.AuditLogQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/logs/audit")
public class AuditLogQueryController {

  private final AuditLogQueryService auditLogQueryService;

  @PreAuthorize("hasAuthority('LOG_AUDIT_LIST')")
  @GetMapping("")
  public ResponseEntity<ApiResponse<PageResponse<AuditLogQueryResponse>>> getAuditLogs(
      @AuthenticationPrincipal CustomUser employee,
      PageRequest page,
      AuditLogSearchRequest search,
      SortRequest sort) {
    Long hotelGroupCode = employee.getHotelGroupCode();

    PageResponse<AuditLogQueryResponse> response = auditLogQueryService.getAuditLogs(hotelGroupCode,
        page, search,
        sort);

    return ResponseEntity.ok(ApiResponse.success(response));
  }
}
