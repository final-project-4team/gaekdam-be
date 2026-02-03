package com.gaekdam.gaekdambe.reservation_service.stay.command.application.controller;

import com.gaekdam.gaekdambe.global.config.model.ApiResponse;
import com.gaekdam.gaekdambe.reservation_service.stay.command.application.dto.request.CheckInRequest;
import com.gaekdam.gaekdambe.reservation_service.stay.command.application.dto.request.CheckOutRequest;
import com.gaekdam.gaekdambe.reservation_service.stay.command.application.service.CheckInOutCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/checkinout")
public class CheckInOutCommandController {

    private final CheckInOutCommandService checkInOutCommandService;

    // 오늘 예약정보 안에 체크인 등록
    @PostMapping("/checkin")
    @PreAuthorize("hasAuthority('CHECK_IN_CREATE')")
    public ApiResponse<Void> checkIn(
            @RequestBody CheckInRequest request
    ) {
        checkInOutCommandService.checkIn(request);
        return ApiResponse.success();
    }

    // 오늘 예약정보 안에 체크아웃 등록
    @PostMapping("/checkout")
    @PreAuthorize("hasAuthority('CHECK_OUT_CREATE')")
    public ApiResponse<Void> checkOut(
            @RequestBody CheckOutRequest request
    ) {
        checkInOutCommandService.checkOut(request);
        return ApiResponse.success();
    }
}
