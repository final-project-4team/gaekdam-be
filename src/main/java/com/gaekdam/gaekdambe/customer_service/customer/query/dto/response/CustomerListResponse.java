package com.gaekdam.gaekdambe.customer_service.customer.query.dto.response;

import com.gaekdam.gaekdambe.customer_service.customer.query.dto.response.item.CustomerListItem;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CustomerListResponse {

    private int page;
    private int size;
    private long totalCount;
    private int totalPages;

    private List<CustomerListItem> items;
}
