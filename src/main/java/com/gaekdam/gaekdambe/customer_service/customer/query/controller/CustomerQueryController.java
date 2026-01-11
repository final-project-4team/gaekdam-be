package com.gaekdam.gaekdambe.customer_service.customer.query.controller;

import com.gaekdam.gaekdambe.customer_service.customer.query.dto.request.CustomerListSearchRequest;
import com.gaekdam.gaekdambe.customer_service.customer.query.dto.response.CustomerListResponse;
import com.gaekdam.gaekdambe.customer_service.customer.query.service.CustomerQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/customers")
public class CustomerQueryController {

    private final CustomerQueryService customerUnifiedQueryService;

    @GetMapping
    public CustomerListResponse loadCustomerUnifiedList(@ModelAttribute CustomerListSearchRequest request) {
        return customerUnifiedQueryService.loadCustomerUnifiedList(request);
    }
}
