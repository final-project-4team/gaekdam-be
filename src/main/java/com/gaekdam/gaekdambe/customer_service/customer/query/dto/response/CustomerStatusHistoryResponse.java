package com.gaekdam.gaekdambe.customer_service.customer.query.dto.response;

import com.gaekdam.gaekdambe.customer_service.customer.query.dto.response.item.CustomerStatusHistoryItem;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerStatusHistoryResponse {

    private List<CustomerStatusHistoryItem> content;
    private Integer page;
    private Integer size;
    private Long totalElements;
    private Integer totalPages;
}
