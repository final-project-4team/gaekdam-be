package com.gaekdam.gaekdambe.customer_service.customer.query.service.model;


import com.gaekdam.gaekdambe.customer_service.customer.command.domain.ContractType;
import com.gaekdam.gaekdambe.customer_service.customer.command.domain.CustomerStatus;
import com.gaekdam.gaekdambe.customer_service.customer.command.domain.NationalityType;
import com.gaekdam.gaekdambe.customer_service.membership.command.domain.MembershipGradeName;

public record CustomerUnifiedRow(
        Long customerCode,
        byte[] customerNameEnc,
        byte[] primaryContactValueEnc,

        CustomerStatus customerStatus,
        ContractType contractType,
        NationalityType nationalityType,

        MembershipGradeName membershipGradeName,
        Long loyaltyGradeCode
) {}