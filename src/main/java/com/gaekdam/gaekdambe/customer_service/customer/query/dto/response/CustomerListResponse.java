package com.gaekdam.gaekdambe.customer_service.customer.query.dto.response;

import com.gaekdam.gaekdambe.customer_service.customer.command.domain.ContractType;
import com.gaekdam.gaekdambe.customer_service.customer.command.domain.CustomerStatus;
import com.gaekdam.gaekdambe.customer_service.customer.command.domain.NationalityType;
import com.gaekdam.gaekdambe.customer_service.membership.command.domain.MembershipGradeName;

import java.time.LocalDate;
import java.util.List;

public record CustomerListResponse(
        long totalCount,
        int page,
        int size,
        List<Item> items
) {
    public static CustomerListResponse of(long totalCount, int page, int size, List<Item> items) {
        return new CustomerListResponse(totalCount, page, size, items);
    }

    public record Item(
            Long customerCode,
            String customerName,              // TODO 복호화 붙이면 채움
            String primaryContactValue,       // TODO 복호화 붙이면 채움

            CustomerStatus status,
            MembershipGradeName membershipGradeName,
            Long loyaltyGradeCode,

            LocalDate lastUsedDate,           // TODO "최근 이용" 도메인 붙이면 채움
            String inflowChannel,             // TODO "유입채널" 도메인 붙이면 채움

            ContractType contractType,
            NationalityType nationalityType
    ) {}
}