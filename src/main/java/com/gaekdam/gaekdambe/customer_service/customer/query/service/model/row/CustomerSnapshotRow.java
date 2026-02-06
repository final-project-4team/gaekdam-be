package com.gaekdam.gaekdambe.customer_service.customer.query.service.model.row;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class CustomerSnapshotRow {
    private Long customerCode;
    private Long totalStayCount;
    private BigDecimal ltvAmount;
    private LocalDateTime lastUsedAt;
    private Long unresolvedInquiryCount;
}

