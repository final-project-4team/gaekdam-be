package com.gaekdam.gaekdambe.customer_service.loyalty.query.controller;

import com.gaekdam.gaekdambe.customer_service.loyalty.query.dto.response.LoyaltyHistoryResponse;
import com.gaekdam.gaekdambe.customer_service.loyalty.query.service.LoyaltyHistoryQueryService;
import com.gaekdam.gaekdambe.global.config.model.ApiResponse;
import com.gaekdam.gaekdambe.global.config.security.CustomUser;
import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/customers/{customerCode}/loyalties")
public class LoyaltyHistoryQueryController {

    private final LoyaltyHistoryQueryService service;

    @GetMapping("/history")
    public ApiResponse<PageResponse<LoyaltyHistoryResponse>> getHistory(
            @AuthenticationPrincipal CustomUser user,
            @PathVariable Long customerCode,
            PageRequest page,
            @RequestParam LocalDate from,
            @RequestParam LocalDate to
    ) {
        Long hotelGroupCode = user.getHotelGroupCode();
        return ApiResponse.success(
                service.getHistory(
                        page,
                        hotelGroupCode,
                        customerCode,
                        from.atStartOfDay(),
                        to.atTime(23, 59, 59)
                )
        );
    }
}
