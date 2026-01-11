package com.gaekdam.gaekdambe.customer_service.customer.query.service.model;


import com.gaekdam.gaekdambe.customer_service.customer.command.domain.ContractType;
import com.gaekdam.gaekdambe.customer_service.customer.command.domain.CustomerStatus;
import com.gaekdam.gaekdambe.customer_service.customer.command.domain.NationalityType;
import com.gaekdam.gaekdambe.customer_service.membership.command.domain.MembershipGradeName;

public record CustomerUnifiedListCondition(
        Long hotelGroupCode,

        Long customerCodeExact,
        String customerNameHash,
        String contactValueHash,

        CustomerStatus status,
        ContractType contractType,
        NationalityType nationalityType,
        MembershipGradeName membershipGradeName,
        Long loyaltyGradeCode,

        int offset,
        int limit
) {
    public static CustomerUnifiedListCondition of(
            Long hotelGroupCode,
            Long customerCodeExact,
            String customerNameHash,
            String contactValueHash,
            CustomerStatus status,
            ContractType contractType,
            NationalityType nationalityType,
            MembershipGradeName membershipGradeName,
            Long loyaltyGradeCode,
            int offset,
            int limit
    ) {
        return new CustomerUnifiedListCondition(
                hotelGroupCode,
                customerCodeExact,
                customerNameHash,
                contactValueHash,
                status,
                contractType,
                nationalityType,
                membershipGradeName,
                loyaltyGradeCode,
                offset,
                limit
        );
    }
}