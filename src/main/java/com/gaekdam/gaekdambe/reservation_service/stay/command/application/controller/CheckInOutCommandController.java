package com.gaekdam.gaekdambe.reservation_service.stay.command.application.controller;

import com.gaekdam.gaekdambe.global.config.model.ApiResponse;
import com.gaekdam.gaekdambe.reservation_service.stay.command.application.dto.request.CheckInRequest;
import com.gaekdam.gaekdambe.reservation_service.stay.command.application.dto.request.CheckOutRequest;
import com.gaekdam.gaekdambe.reservation_service.stay.command.application.service.CheckInOutCommandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "투숙")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/checkinout")
public class CheckInOutCommandController {

    private final CheckInOutCommandService checkInOutCommandService;

    // 오늘 예약정보 안에 체크인 등록
    @PostMapping("/checkin")
    @PreAuthorize("hasAuthority('CHECK_IN_CREATE')")
    @Operation(summary = "체크인 등록", description = "예약 정보를 기반으로 체크인을 등록합니다.")
    public ApiResponse<Void> checkIn(
            @RequestBody CheckInRequest request) {
        checkInOutCommandService.checkIn(request);
        return ApiResponse.success();
    }

    // 오늘 예약정보 안에 체크아웃 등록
    @PostMapping("/checkout")
    @PreAuthorize("hasAuthority('CHECK_OUT_CREATE')")
    @Operation(summary = "체크아웃 등록", description = "투숙 정보를 기반으로 체크아웃을 등록합니다.")
    public ApiResponse<Void> checkOut(
            @RequestBody CheckOutRequest request) {
        checkInOutCommandService.checkOut(request);
        return ApiResponse.success();
    }
}
