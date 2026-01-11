package com.gaekdam.gaekdambe.customer_service.customer.query.dto.response;

import com.gaekdam.gaekdambe.customer_service.customer.command.domain.ContractType;
import com.gaekdam.gaekdambe.customer_service.customer.command.domain.CustomerStatus;
import com.gaekdam.gaekdambe.customer_service.customer.command.domain.NationalityType;

import java.time.LocalDate;
import java.util.List;

public record CustomerListResponse(
        long totalCount,
        List<Item> items
) {
    public static CustomerListResponse of(List<Item> items) {
        long totalCount = (items == null) ? 0L : items.size();
        return new CustomerListResponse(totalCount, items);
    }

    public record Item(
            Long customerCode,
            String customerName,               // 복호화 모듈 붙이면 채움
            String primaryContactValue,        // 복호화 모듈 붙이면 채움

            CustomerStatus status,

            Long membershipGradeCode,
            String membershipGradeName,

            Long loyaltyGradeCode,

            LocalDate lastUsedDate,            // 타 도메인 결합 시 채움
            String inflowChannel,              // 타 도메인 결합 시 채움

            ContractType contractType,
            NationalityType nationalityType
    ) {}
}
