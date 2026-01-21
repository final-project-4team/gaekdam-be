package com.gaekdam.gaekdambe.customer_service.membership.query.controller;

import com.gaekdam.gaekdambe.customer_service.membership.query.dto.response.MembershipHistoryResponse;
import com.gaekdam.gaekdambe.customer_service.membership.query.service.MembershipHistoryQueryService;
import com.gaekdam.gaekdambe.global.config.model.ApiResponse;
import com.gaekdam.gaekdambe.global.config.security.CustomUser;
import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.PageResponse;import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/customers/{customerCode}/memberships")
public class MembershipHistoryQueryController {

    private final MembershipHistoryQueryService service;

    @GetMapping("/history")
    public ApiResponse<PageResponse<MembershipHistoryResponse>> getHistory(
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
