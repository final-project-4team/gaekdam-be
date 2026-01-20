package com.gaekdam.gaekdambe.customer_service.customer.command.application.controller;

import com.gaekdam.gaekdambe.customer_service.customer.command.application.dto.request.CustomerMemoCreateRequest;
import com.gaekdam.gaekdambe.customer_service.customer.command.application.dto.request.CustomerMemoUpdateRequest;
import com.gaekdam.gaekdambe.customer_service.customer.command.application.dto.response.CustomerMemoCommandResponse;
import com.gaekdam.gaekdambe.customer_service.customer.command.application.service.CustomerMemoCommandService;
import com.gaekdam.gaekdambe.global.config.model.ApiResponse;
import com.gaekdam.gaekdambe.global.config.security.CustomUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/customers/{customerCode}/memos")
public class CustomerMemoCommandController {

    private final CustomerMemoCommandService customerMemoCommandService;

    @PostMapping
    public ApiResponse<CustomerMemoCommandResponse> createMemo(
            @AuthenticationPrincipal CustomUser user,
            @PathVariable Long customerCode,
            @RequestBody @Valid CustomerMemoCreateRequest request
    ) {
        return ApiResponse.success(customerMemoCommandService.createCustomerMemo(user, customerCode, request));
    }

    @PutMapping("/{memoCode}")
    public ApiResponse<CustomerMemoCommandResponse> updateMemo(
            @AuthenticationPrincipal CustomUser user,
            @PathVariable Long customerCode,
            @PathVariable Long memoCode,
            @RequestBody @Valid CustomerMemoUpdateRequest request
    ) {
        return ApiResponse.success(customerMemoCommandService.updateCustomerMemo(user, customerCode, memoCode, request));
    }

    @DeleteMapping("/{memoCode}")
    public ApiResponse<Void> deleteMemo(
            @AuthenticationPrincipal CustomUser user,
            @PathVariable Long customerCode,
            @PathVariable Long memoCode
    ) {
        customerMemoCommandService.deleteCustomerMemo(user, customerCode, memoCode);
        return ApiResponse.success(null);
    }
}
