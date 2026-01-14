package com.gaekdam.gaekdambe.iam_service.permission.command.application.controller;

import com.gaekdam.gaekdambe.global.config.model.ApiResponse;
import com.gaekdam.gaekdambe.global.config.security.CustomUser;
import com.gaekdam.gaekdambe.iam_service.permission.command.application.dto.request.PermissionCreateRequest;
import com.gaekdam.gaekdambe.iam_service.permission.command.application.dto.request.PermissionUpdateRequest;
import com.gaekdam.gaekdambe.iam_service.permission.command.application.service.PermissionCommandService;
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

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/permission")
public class PermissionCommandController {
  private final PermissionCommandService permissionCommandService;

  //권한 생성
  @PostMapping("")
  @PreAuthorize("hasAuthority('PERMISSION_CREATE')")
  public ApiResponse<String> createPermission(
      @RequestBody PermissionCreateRequest request,
      @AuthenticationPrincipal CustomUser customUser
  ){
    Long hotelGroupCode=customUser.getHotelGroupCode();
    return ApiResponse.success(permissionCommandService.createPermission(request,hotelGroupCode));

  }
  //권한 변경
  @PutMapping("/{permissionCode}")
  @PreAuthorize("hasAuthority('PERMISSION_UPDATE')")
  public ApiResponse<String> upatePermission(
      @PathVariable Long permissionCode,
      @RequestBody PermissionUpdateRequest request,
      @AuthenticationPrincipal CustomUser customUser
  ){
    Long hotelGroupCode=customUser.getHotelGroupCode();
    return ApiResponse.success(permissionCommandService.updatePermission(permissionCode,request,hotelGroupCode));

  }
  //권한 삭제
  @DeleteMapping("/{permissionCode}")
  @PreAuthorize("hasAuthority('PERMISSION_DELETE')")
  public ApiResponse<String> deletePermission(
      @PathVariable Long permissionCode,
      @AuthenticationPrincipal CustomUser customUser
  ){
    Long hotelGroupCode=customUser.getHotelGroupCode();
    return ApiResponse.success(permissionCommandService.deletePermission(permissionCode,hotelGroupCode));

  }
}
