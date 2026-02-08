package com.gaekdam.gaekdambe.customer_service.customer.command.application.controller;

import com.gaekdam.gaekdambe.customer_service.customer.command.application.dto.request.CustomerMemoCreateRequest;
import com.gaekdam.gaekdambe.customer_service.customer.command.application.dto.request.CustomerMemoUpdateRequest;
import com.gaekdam.gaekdambe.customer_service.customer.command.application.dto.response.CustomerMemoCommandResponse;
import com.gaekdam.gaekdambe.customer_service.customer.command.application.service.CustomerMemoCommandService;
import com.gaekdam.gaekdambe.global.config.model.ApiResponse;
import com.gaekdam.gaekdambe.global.config.security.CustomUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name="고객 메모")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/customers/{customerCode}/memos")
public class CustomerMemoCommandController {

    private final CustomerMemoCommandService customerMemoCommandService;

    @PostMapping
    @PreAuthorize("hasAuthority('CUSTOMER_UPDATE')")
    @Operation(summary = "고객 메모를 생성한다.", description = "직원이 특정 고객에 대한 메모를 작성한다")
    public ApiResponse<CustomerMemoCommandResponse> createMemo(
            @AuthenticationPrincipal CustomUser user,
            @Parameter(description = "고객 코드") @PathVariable Long customerCode,
            @Parameter(description = "고객 메모 내용")@RequestBody @Valid CustomerMemoCreateRequest request
    ) {
        return ApiResponse.success(customerMemoCommandService.createCustomerMemo(user, customerCode, request));
    }

    @PutMapping("/{memoCode}")
    @PreAuthorize("hasAuthority('CUSTOMER_UPDATE')")
    @Operation(summary = "고객 메모를 수정 한다.", description = "직원이 특정 고객에 대한 메모를 수정 한다")
    public ApiResponse<CustomerMemoCommandResponse> updateMemo(
            @AuthenticationPrincipal CustomUser user,
            @Parameter(description = "고객 코드") @PathVariable Long customerCode,
            @Parameter(description = "고객 메모 코드") @PathVariable Long memoCode,
            @Parameter(description = "고객 내용") @RequestBody @Valid CustomerMemoUpdateRequest request
    ) {
        return ApiResponse.success(customerMemoCommandService.updateCustomerMemo(user, customerCode, memoCode, request));
    }

    @DeleteMapping("/{memoCode}")
    @PreAuthorize("hasAuthority('CUSTOMER_UPDATE')")
    @Operation(summary = "고객 메모를 삭제 한다.", description = "직원이 특정 고객에 대한 메모를 삭제 할 수 있다.")
    public ApiResponse<Void> deleteMemo(
            @AuthenticationPrincipal CustomUser user,
            @Parameter(description = "고객 코드") @PathVariable Long customerCode,
            @Parameter(description = "고객 메모 코드") @PathVariable Long memoCode
    ) {
        customerMemoCommandService.deleteCustomerMemo(user, customerCode, memoCode);
        return ApiResponse.success(null);
    }
}
