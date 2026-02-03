package com.gaekdam.gaekdambe.iam_service.log.query.controller;

import com.gaekdam.gaekdambe.global.config.model.ApiResponse;
import com.gaekdam.gaekdambe.global.config.security.CustomUser;
import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.PageResponse;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
import com.gaekdam.gaekdambe.iam_service.log.query.dto.request.AuditLogSearchRequest;
import com.gaekdam.gaekdambe.iam_service.log.query.dto.response.AuditLogQueryResponse;
import com.gaekdam.gaekdambe.iam_service.log.query.service.AuditLogQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "로그")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/logs/audit")
public class AuditLogQueryController {

  private final AuditLogQueryService auditLogQueryService;

  @PreAuthorize("hasAuthority('LOG_AUDIT_LIST')")
  @GetMapping("")
  @Operation(summary = "활동 로그 목록 조회", description = "시스템 내 발생한 활동을 로그 리스트로 조회합니다.")
  public ResponseEntity<ApiResponse<PageResponse<AuditLogQueryResponse>>> getAuditLogs(
      @Parameter(hidden = true) @AuthenticationPrincipal CustomUser employee,
      PageRequest page,
      AuditLogSearchRequest search,
      SortRequest sort) {
    Long hotelGroupCode = employee.getHotelGroupCode();

    PageResponse<AuditLogQueryResponse> response = auditLogQueryService.getAuditLogs(hotelGroupCode,
        page, search,
        sort);

    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @PreAuthorize("hasAuthority('LOG_AUDIT_READ')")
  @GetMapping("/{auditLogCode}")
  @Operation(summary = "활동 로그 상세 조회", description = "활동 로그의 상세 정보를 조회합니다.")
  public ResponseEntity<ApiResponse<AuditLogQueryResponse>> getAuditLogDetail(
      @Parameter(description = "활동 로그 코드") @PathVariable Long auditLogCode) {
    AuditLogQueryResponse response = auditLogQueryService.getAuditLog(auditLogCode);
    return ResponseEntity.ok(ApiResponse.success(response));
  }
}
