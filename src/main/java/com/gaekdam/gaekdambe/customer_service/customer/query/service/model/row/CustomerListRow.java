package com.gaekdam.gaekdambe.customer_service.customer.query.service.model.row;

import com.gaekdam.gaekdambe.customer_service.customer.command.domain.ContractType;
import com.gaekdam.gaekdambe.customer_service.customer.command.domain.CustomerStatus;
import com.gaekdam.gaekdambe.customer_service.customer.command.domain.NationalityType;

import java.time.LocalDate;

public record CustomerListRow(
        Long customerCode,
        byte[] customerNameEnc,
        byte[] primaryContactEnc,
        CustomerStatus status,

        String membershipGrade,
        String loyaltyGrade,

        LocalDate lastUsedDate,
        String inflowChannel,

        ContractType contractType,
        NationalityType nationalityType,
        byte[] dekEnc
) {
}
