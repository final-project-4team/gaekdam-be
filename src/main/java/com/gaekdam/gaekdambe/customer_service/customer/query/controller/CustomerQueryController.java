package com.gaekdam.gaekdambe.customer_service.customer.query.controller;

import com.gaekdam.gaekdambe.customer_service.customer.query.dto.request.CustomerDetailRequest;
import com.gaekdam.gaekdambe.customer_service.customer.query.dto.request.CustomerListSearchRequest;
import com.gaekdam.gaekdambe.customer_service.customer.query.dto.response.*;
import com.gaekdam.gaekdambe.customer_service.customer.query.service.CustomerQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/customers")
public class CustomerQueryController {

    private final CustomerQueryService customerQueryService;

    @GetMapping
    public CustomerListResponse getCustomerList(@ModelAttribute CustomerListSearchRequest request) {
        return customerQueryService.getCustomerList(request);
    }

    @GetMapping("/{customerCode}")
    public CustomerDetailResponse getCustomerDetail(
            @PathVariable Long customerCode,
            @RequestParam Long hotelGroupCode
    ) {
        return customerQueryService.getCustomerDetail(new CustomerDetailRequest(hotelGroupCode, customerCode));
    }

    @GetMapping("/{customerCode}/status")
    public CustomerStatusResponse getCustomerStatus(
            @PathVariable Long customerCode,
            @RequestParam Long hotelGroupCode
    ) {
        return customerQueryService.getCustomerStatus(new CustomerDetailRequest(hotelGroupCode, customerCode));
    }

    @GetMapping("/{customerCode}/status-histories")
    public CustomerStatusHistoryResponse getCustomerStatusHistories(
            @PathVariable Long customerCode,
            @RequestParam Long hotelGroupCode
    ) {
        return customerQueryService.getCustomerStatusHistories(new CustomerDetailRequest(hotelGroupCode, customerCode));
    }

    @GetMapping("/{customerCode}/marketing-consents")
    public CustomerMarketingConsentResponse getCustomerMarketingConsents(
            @PathVariable Long customerCode,
            @RequestParam Long hotelGroupCode
    ) {
        return customerQueryService.getCustomerMarketingConsents(new CustomerDetailRequest(hotelGroupCode, customerCode));
    }
}
