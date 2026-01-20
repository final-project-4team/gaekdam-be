package com.gaekdam.gaekdambe.customer_service.customer.query.controller;

import com.gaekdam.gaekdambe.customer_service.customer.query.dto.request.CustomerMemoSearchRequest;
import com.gaekdam.gaekdambe.customer_service.customer.query.dto.response.CustomerMemoResponse;
import com.gaekdam.gaekdambe.customer_service.customer.query.service.CustomerMemoQueryService;
import com.gaekdam.gaekdambe.global.config.model.ApiResponse;
import com.gaekdam.gaekdambe.global.config.security.CustomUser;
import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.PageResponse;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/customers/{customerCode}/memos")
public class CustomerMemoQueryController {

    private final CustomerMemoQueryService customerMemoQueryService;

    @GetMapping
    public ApiResponse<PageResponse<CustomerMemoResponse>> getMemos(
            @AuthenticationPrincipal CustomUser user,
            @PathVariable Long customerCode,
            PageRequest page,
            CustomerMemoSearchRequest search,
            SortRequest sort
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
    public ApiResponse<CustomerMemoResponse> getMemoDetail(
            @AuthenticationPrincipal CustomUser user,
            @PathVariable Long customerCode,
            @PathVariable Long memoCode,
            CustomerMemoSearchRequest search
    ) {
        search.setHotelGroupCode(user.getHotelGroupCode());
        search.setCustomerCode(customerCode);
        search.setCustomerMemoCode(memoCode);

        return ApiResponse.success(customerMemoQueryService.getCustomerMemoDetail(search));
    }
}
