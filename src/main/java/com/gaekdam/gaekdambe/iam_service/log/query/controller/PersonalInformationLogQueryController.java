package com.gaekdam.gaekdambe.iam_service.log.query.controller;

import com.gaekdam.gaekdambe.global.config.model.ApiResponse;
import com.gaekdam.gaekdambe.global.config.security.CustomUser;
import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.PageResponse;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
import com.gaekdam.gaekdambe.iam_service.log.query.dto.request.PersonalInformationLogSearchRequest;
import com.gaekdam.gaekdambe.iam_service.log.query.dto.response.PersonalInformationLogQueryResponse;
import com.gaekdam.gaekdambe.iam_service.log.query.service.PersonalInformationLogQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/logs/personal-information")
public class PersonalInformationLogQueryController {
  private final PersonalInformationLogQueryService personalInformationLogQueryService;

  @GetMapping

  public ResponseEntity<ApiResponse<PageResponse<PersonalInformationLogQueryResponse>>> getPersonalInformationLogs(
      @AuthenticationPrincipal CustomUser employee,
      PageRequest page,
      PersonalInformationLogSearchRequest search,
      SortRequest sort) {
    Long hotelGroupCode = employee.getHotelGroupCode();

    PageResponse<PersonalInformationLogQueryResponse> response = personalInformationLogQueryService.getPersonalInformationLogs(hotelGroupCode, page, search,
        sort);

    return ResponseEntity.ok(ApiResponse.success(response));
  }
}
