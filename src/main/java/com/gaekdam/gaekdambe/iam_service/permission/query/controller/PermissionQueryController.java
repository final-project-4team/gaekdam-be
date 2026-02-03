package com.gaekdam.gaekdambe.iam_service.permission.query.controller;

import com.gaekdam.gaekdambe.global.config.model.ApiResponse;
import com.gaekdam.gaekdambe.global.config.security.CustomUser;
import com.gaekdam.gaekdambe.global.paging.PageResponse;
import com.gaekdam.gaekdambe.iam_service.permission.query.dto.request.PermissionQueryRequest;
import com.gaekdam.gaekdambe.iam_service.permission.query.dto.response.PermissionListResponse;
import com.gaekdam.gaekdambe.iam_service.permission.query.dto.response.PermissionNameListResponse;
import com.gaekdam.gaekdambe.iam_service.permission.query.service.PermissionQueryService;
import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "권한")
@RestController
@RequestMapping("/api/v1/permission")
@RequiredArgsConstructor
public class PermissionQueryController {
  private final PermissionQueryService permissionQueryService;

  // 권한관리에서 보여줄 권한 리스트(권한 타입까지 출력)
  @GetMapping("")
  @PreAuthorize("hasAuthority('PERMISSION_LIST')")
  @Operation(summary = "권한 리스트 조회", description = "호텔의 권한 리스트를 조회합니다.")
  public ApiResponse<PageResponse<PermissionListResponse>> getPermissionList(
      PermissionQueryRequest query,
      @Parameter(hidden = true) @AuthenticationPrincipal CustomUser employee) {
    Long hotelGroupCode = employee.getHotelGroupCode();
    return ApiResponse.success(permissionQueryService.getPermissionList(query, hotelGroupCode));
  }

  // 권한 리스트(권한 명만 출력)
  @GetMapping("/name")
  @Operation(summary = "권한 명칭 리스트 조회", description = "호텔의 권한 명칭만을 포함한 리스트를 조회합니다.")
  public ApiResponse<List<PermissionNameListResponse>> getPermissionNameList(
      @Parameter(hidden = true) @AuthenticationPrincipal CustomUser employee) {
    Long hotelGroupCode = employee.getHotelGroupCode();
    return ApiResponse.success(permissionQueryService.getPermissionNameList(hotelGroupCode));
  }

}
