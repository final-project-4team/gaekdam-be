package com.gaekdam.gaekdambe.customer_service.customer.query.dto.response.item;

import com.gaekdam.gaekdambe.customer_service.customer.command.domain.ChangeSource;
import com.gaekdam.gaekdambe.customer_service.customer.command.domain.CustomerStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerStatusHistoryItem {

    private Long customerStatusHistoryCode;

    private CustomerStatus beforeStatus;
    private CustomerStatus afterStatus;

    private ChangeSource changeSource; // SYSTEM/MANUAL
    private String changeReason;

    private LocalDateTime changedAt;
    private Long employeeCode;
    private String employeeName;
}
