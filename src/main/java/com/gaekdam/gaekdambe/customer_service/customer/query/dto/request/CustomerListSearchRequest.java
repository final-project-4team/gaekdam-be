package com.gaekdam.gaekdambe.customer_service.customer.query.dto.request;

import com.gaekdam.gaekdambe.customer_service.customer.command.domain.ContractType;
import com.gaekdam.gaekdambe.customer_service.customer.command.domain.CustomerStatus;
import com.gaekdam.gaekdambe.customer_service.customer.command.domain.NationalityType;
import com.gaekdam.gaekdambe.customer_service.membership.command.domain.MembershipGradeName;

public record CustomerListSearchRequest(
        Long hotelGroupCode,

        // crypto(해시) 로직은 밖에서 만들고, 여기서는 결과만 받음
        Long customerCode,
        String customerNameHash,
        String contactValueHash,

        CustomerStatus status,
        ContractType contractType,
        NationalityType nationalityType,
        MembershipGradeName membershipGradeName,
        Long loyaltyGradeCode,

        Integer page,   // 1-base
        Integer size
) {
    public int normalizedPage() {
        return (page == null || page < 1) ? 1 : page;
    }

    public int normalizedSize() {
        int defaultSize = 20;
        int maxSize = 200;
        if (size == null || size < 1) return defaultSize;
        return Math.min(size, maxSize);
    }

    public int offset() {
        return (normalizedPage() - 1) * normalizedSize();
    }
}

