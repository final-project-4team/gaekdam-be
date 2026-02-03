package com.gaekdam.gaekdambe.customer_service.loyalty.query.controller;

import com.gaekdam.gaekdambe.customer_service.loyalty.query.dto.response.LoyaltyHistoryResponse;
import com.gaekdam.gaekdambe.customer_service.loyalty.query.service.LoyaltyHistoryQueryService;
import com.gaekdam.gaekdambe.global.config.model.ApiResponse;
import com.gaekdam.gaekdambe.global.config.security.CustomUser;
import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Tag(name="로열티 이력")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/customers/{customerCode}/loyalties")
public class LoyaltyHistoryQueryController {

    private final LoyaltyHistoryQueryService service;

    @GetMapping("/history")
    //  @PreAuthorize("hasAuthority('LOYALTY_LIST')") 아직 없음
    @Operation(summary = "로열티 이력 리스트 조회", description = "로열티 이력 리스트를 조회할 수 있다.")
    public ApiResponse<PageResponse<LoyaltyHistoryResponse>> getHistory(
            @AuthenticationPrincipal CustomUser user,
            @Parameter(description = "고객 코드") @PathVariable Long customerCode,
            @Parameter(description = "페이징 값") PageRequest page,
            @Parameter(description = "시작일") @RequestParam LocalDate from,
            @Parameter(description = "마감일")@RequestParam LocalDate to
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
