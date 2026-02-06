package com.gaekdam.gaekdambe.iam_service.permission.command.application.controller;

import com.gaekdam.gaekdambe.global.config.model.ApiResponse;
import com.gaekdam.gaekdambe.global.config.security.CustomUser;
import com.gaekdam.gaekdambe.iam_service.permission.command.application.dto.request.PermissionCreateRequest;
import com.gaekdam.gaekdambe.iam_service.permission.command.application.dto.request.PermissionUpdateRequest;
import com.gaekdam.gaekdambe.iam_service.permission.command.application.service.PermissionCommandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "권한")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/permission")
public class PermissionCommandController {
  private final PermissionCommandService permissionCommandService;

  // 권한 생성
  @PostMapping("")
  @PreAuthorize("hasAuthority('PERMISSION_CREATE')")
  @Operation(summary = "권한 생성", description = "호텔에 새로운 권한을 생성합니다.")
  public ApiResponse<String> createPermission(
      @RequestBody PermissionCreateRequest request,
      @Parameter(hidden = true) @AuthenticationPrincipal CustomUser customUser) {
    Long hotelGroupCode = customUser.getHotelGroupCode();
    return ApiResponse.success(permissionCommandService.createPermission(request, hotelGroupCode));

  }

  // 권한 변경
  @PutMapping("/{permissionCode}")
  @PreAuthorize("hasAuthority('PERMISSION_UPDATE')")
  @Operation(summary = "권한 수정", description = "권한 정보를 수정합니다.")
  public ApiResponse<String> upatePermission(
      @Parameter(description = "권한 코드") @PathVariable Long permissionCode,
      @RequestBody PermissionUpdateRequest request,
      @Parameter(hidden = true) @AuthenticationPrincipal CustomUser customUser) {
    Long hotelGroupCode = customUser.getHotelGroupCode();
    return ApiResponse.success(
        permissionCommandService.updatePermission(permissionCode, request, hotelGroupCode, customUser.getUsername()));

  }

  // 권한 삭제
  @DeleteMapping("/{permissionCode}")
  @PreAuthorize("hasAuthority('PERMISSION_DELETE')")
  @Operation(summary = "권한 삭제", description = "특정 권한을 삭제합니다.")
  public ApiResponse<String> deletePermission(
      @Parameter(description = "권한 코드") @PathVariable Long permissionCode,
      @Parameter(hidden = true) @AuthenticationPrincipal CustomUser customUser) {
    Long hotelGroupCode = customUser.getHotelGroupCode();
    return ApiResponse.success(permissionCommandService.deletePermission(permissionCode, hotelGroupCode));

  }
}
