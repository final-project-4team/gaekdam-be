package com.gaekdam.gaekdambe.iam_service.log.query.controller;

import com.gaekdam.gaekdambe.global.config.model.ApiResponse;
import com.gaekdam.gaekdambe.global.config.security.CustomUser;
import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.PageResponse;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
import com.gaekdam.gaekdambe.iam_service.log.query.dto.request.PersonalInformationLogSearchRequest;
import com.gaekdam.gaekdambe.iam_service.log.query.dto.response.PersonalInformationLogQueryResponse;
import com.gaekdam.gaekdambe.iam_service.log.query.service.PersonalInformationLogQueryService;
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
@RequestMapping("/api/v1/logs/personal-information")
public class PersonalInformationLogQueryController {
  private final PersonalInformationLogQueryService personalInformationLogQueryService;

  @PreAuthorize("hasAuthority('LOG_PERSONAL_INFORMATION_LIST')")
  @GetMapping
  @Operation(summary = "개인정보 열람 로그 리스트 조회", description = "개인정보 열람 이력 로그 리스트를 조회합니다.")
  public ResponseEntity<ApiResponse<PageResponse<PersonalInformationLogQueryResponse>>> getPersonalInformationLogs(
      @Parameter(hidden = true) @AuthenticationPrincipal CustomUser employee,
      PageRequest page,
      PersonalInformationLogSearchRequest search,
      SortRequest sort) {
    Long hotelGroupCode = employee.getHotelGroupCode();

    PageResponse<PersonalInformationLogQueryResponse> response = personalInformationLogQueryService
        .getPersonalInformationLogs(hotelGroupCode, page, search,
            sort);

    return ResponseEntity.ok(ApiResponse.success(response));
  }
}
