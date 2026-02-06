package com.gaekdam.gaekdambe.customer_service.membership.command.application.controller;

import com.gaekdam.gaekdambe.customer_service.membership.command.application.dto.request.MembershipManualChangeRequest;
import com.gaekdam.gaekdambe.customer_service.membership.command.application.service.MembershipManualCommandService;
import com.gaekdam.gaekdambe.global.config.model.ApiResponse;
import com.gaekdam.gaekdambe.global.config.security.CustomUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name="멤버십")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/memberships")
public class MembershipManualCommandController {

    private final MembershipManualCommandService membershipManualCommandService;

    @PatchMapping("/customers/{customerCode}/manual")
    @Operation(summary = "고객 멤버십 수정", description = "고객의 멤버십 등급을 수동으로 수정할 수 있다.")
    public ApiResponse<String> changeMembershipManually(
            @AuthenticationPrincipal CustomUser user,
            @Parameter(description = "고객 코드") @PathVariable Long customerCode,
            @Parameter(description = "고객 등급 수정 내용")@RequestBody MembershipManualChangeRequest request
    ) {
        String result = membershipManualCommandService.changeMembershipManually(
                user.getHotelGroupCode(),
                request.employeeCode(),
                customerCode,
                request
        );

        return ApiResponse.success(result);
    }
}
