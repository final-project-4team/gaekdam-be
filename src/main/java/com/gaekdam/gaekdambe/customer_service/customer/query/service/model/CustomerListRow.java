package com.gaekdam.gaekdambe.customer_service.customer.query.service.model;

import com.gaekdam.gaekdambe.customer_service.customer.command.domain.ContractType;
import com.gaekdam.gaekdambe.customer_service.customer.command.domain.CustomerStatus;
import com.gaekdam.gaekdambe.customer_service.customer.command.domain.NationalityType;

public record CustomerListRow(
        Long customerCode,
        byte[] customerNameEnc,
        byte[] primaryContactValueEnc,

        CustomerStatus status,
        ContractType contractType,
        NationalityType nationalityType,

        Long membershipGradeCode,
        String membershipGradeName,

        Long loyaltyGradeCode
) {}
