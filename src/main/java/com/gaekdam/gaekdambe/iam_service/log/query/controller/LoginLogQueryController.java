package com.gaekdam.gaekdambe.iam_service.log.query.controller;

import com.gaekdam.gaekdambe.global.config.model.ApiResponse;
import com.gaekdam.gaekdambe.global.config.security.CustomUser;
import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.PageResponse;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
import com.gaekdam.gaekdambe.iam_service.log.query.dto.request.LoginLogSearchRequest;
import com.gaekdam.gaekdambe.iam_service.log.query.dto.response.LoginLogQueryResponse;
import com.gaekdam.gaekdambe.iam_service.log.query.service.LoginLogQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "로그")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/logs/login")
public class LoginLogQueryController {

  private final LoginLogQueryService loginLogQueryService;

  @PreAuthorize("hasAuthority('LOG_LOGIN_LIST')")
  @GetMapping
  @Operation(summary = "로그인 로그 리스트 조회", description = "사용자의 로그인 및 로그아웃 기록을 리스트 조회합니다.")
  public ResponseEntity<ApiResponse<PageResponse<LoginLogQueryResponse>>> getLoginLogs(
      @Parameter(hidden = true) @AuthenticationPrincipal CustomUser customUser,
      PageRequest page,
      LoginLogSearchRequest search,
      SortRequest sort) {
    Long hotelGroupCode = customUser.getHotelGroupCode();

    PageResponse<LoginLogQueryResponse> response = loginLogQueryService.getLoginLogs(hotelGroupCode,
        page, search,
        sort);

    return ResponseEntity.ok(ApiResponse.success(response));
  }
}
