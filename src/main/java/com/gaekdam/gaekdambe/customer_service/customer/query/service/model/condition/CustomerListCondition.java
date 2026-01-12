package com.gaekdam.gaekdambe.customer_service.customer.query.service.model.condition;

import com.gaekdam.gaekdambe.customer_service.customer.command.domain.ContractType;
import com.gaekdam.gaekdambe.customer_service.customer.command.domain.CustomerStatus;
import com.gaekdam.gaekdambe.customer_service.customer.command.domain.NationalityType;

public record CustomerListCondition(
        Long hotelGroupCode,

        Long customerCode,
        String customerNameHash,
        String phoneHash,
        String emailHash,

        CustomerStatus status,
        ContractType contractType,
        NationalityType nationalityType,

        Long membershipGradeCode,
        Long loyaltyGradeCode,
        String inflowChannel,

        Integer offset,
        Integer limit,

        String sortBy,
        String direction
) {
}
