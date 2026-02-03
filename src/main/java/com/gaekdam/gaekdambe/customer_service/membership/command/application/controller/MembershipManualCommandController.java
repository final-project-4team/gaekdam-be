package com.gaekdam.gaekdambe.customer_service.membership.command.application.controller;

import com.gaekdam.gaekdambe.customer_service.membership.command.application.dto.request.MembershipManualChangeRequest;
import com.gaekdam.gaekdambe.customer_service.membership.command.application.service.MembershipManualCommandService;
import com.gaekdam.gaekdambe.global.config.model.ApiResponse;
import com.gaekdam.gaekdambe.global.config.security.CustomUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/memberships")
public class MembershipManualCommandController {

    private final MembershipManualCommandService membershipManualCommandService;

    @PatchMapping("/customers/{customerCode}/manual")
    public ApiResponse<String> changeMembershipManually(
            @AuthenticationPrincipal CustomUser user,
            @PathVariable Long customerCode,
            @RequestBody MembershipManualChangeRequest request
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
