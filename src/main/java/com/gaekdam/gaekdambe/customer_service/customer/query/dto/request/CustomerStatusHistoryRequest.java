package com.gaekdam.gaekdambe.customer_service.customer.query.dto.request;

import com.gaekdam.gaekdambe.global.paging.PageRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerStatusHistoryRequest extends PageRequest {

    private String sortBy;      // 예: changed_at
    private String direction;   // ASC / DESC
}
