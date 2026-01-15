package com.gaekdam.gaekdambe.customer_service.customer.query.dto.response;

import com.gaekdam.gaekdambe.customer_service.customer.query.dto.response.item.CustomerMarketingConsentItem;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerMarketingConsentResponse {

    private Long customerCode;
    private List<CustomerMarketingConsentItem> consents;
}
