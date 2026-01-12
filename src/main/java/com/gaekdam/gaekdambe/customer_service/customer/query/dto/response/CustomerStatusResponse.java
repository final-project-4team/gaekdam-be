package com.gaekdam.gaekdambe.customer_service.customer.query.dto.response;

import com.gaekdam.gaekdambe.customer_service.customer.command.domain.CustomerStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerStatusResponse {

    private Long customerCode;
    private CustomerStatus status;

    private LocalDateTime cautionAt;
    private LocalDateTime inactiveAt;

    private LocalDateTime updatedAt;
}
