// src/main/java/com/gaekdam/gaekdambe/customer_service/customer/query/dto/response/CustomerTimelineResponse.java
package com.gaekdam.gaekdambe.customer_service.customer.query.dto.response;

import com.gaekdam.gaekdambe.customer_service.customer.query.dto.response.item.CustomerTimelineItem;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CustomerTimelineResponse {

    private Long customerCode;
    private int limit;
    private List<CustomerTimelineItem> items;
}
