package com.gaekdam.gaekdambe.customer_service.customer.query.controller;

import com.gaekdam.gaekdambe.customer_service.customer.query.dto.request.CustomerMemoSearchRequest;
import com.gaekdam.gaekdambe.customer_service.customer.query.dto.response.CustomerMemoResponse;
import com.gaekdam.gaekdambe.customer_service.customer.query.service.CustomerMemoQueryService;
import com.gaekdam.gaekdambe.global.config.model.ApiResponse;
import com.gaekdam.gaekdambe.global.config.security.CustomUser;
import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.PageResponse;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
import com.gaekdam.gaekdambe.iam_service.log.command.application.aop.annotation.AuditLog;
import com.gaekdam.gaekdambe.iam_service.permission_type.command.domain.seeds.PermissionTypeKey;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name="고객 메모")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/customers/{customerCode}/memos")
public class CustomerMemoQueryController {

    private final CustomerMemoQueryService customerMemoQueryService;

    @GetMapping
    @PreAuthorize("hasAuthority('CUSTOMER_READ')")
    @AuditLog(details = "", type = PermissionTypeKey.CUSTOMER_MEMO_LIST)
    @Operation(summary = "고객 메모 리스트 조회", description = "특정 고객에 대한 메모를 리스트로 조회 한다.")
    public ApiResponse<PageResponse<CustomerMemoResponse>> getMemos(
            @AuthenticationPrincipal CustomUser user,
            @Parameter(description = "고객 코드") @PathVariable Long customerCode,
            @Parameter(description = "페이징 값") PageRequest page,
            @Parameter(description = "검색 키워드")CustomerMemoSearchRequest search,
            @Parameter(description = "정렬 기준")SortRequest sort
    ) {
        search.setHotelGroupCode(user.getHotelGroupCode());
        search.setCustomerCode(customerCode);

        if (sort == null || sort.getSortBy() == null) {
            sort = new SortRequest();
            sort.setSortBy("created_at");
            sort.setDirection("DESC");
        }

        return ApiResponse.success(customerMemoQueryService.getCustomerMemos(page, search, sort));
    }

    @GetMapping("/{memoCode}")
    @PreAuthorize("hasAuthority('CUSTOMER_READ')")
    @AuditLog(details = "", type = PermissionTypeKey.CUSTOMER_MEMO_READ)
    @Operation(summary = "고객 메모 상세 조회", description = "특정 고객에 대한 메모를 상세 조회 한다.")
    public ApiResponse<CustomerMemoResponse> getMemoDetail(
            @AuthenticationPrincipal CustomUser user,
            @Parameter(description = "고객 코드") @PathVariable Long customerCode,
            @Parameter(description = "고객 메모 코드") @PathVariable Long memoCode,
            @Parameter(description = "검색 키워드")CustomerMemoSearchRequest search
    ) {
        search.setHotelGroupCode(user.getHotelGroupCode());
        search.setCustomerCode(customerCode);
        search.setCustomerMemoCode(memoCode);

        return ApiResponse.success(customerMemoQueryService.getCustomerMemoDetail(search));
    }
}
