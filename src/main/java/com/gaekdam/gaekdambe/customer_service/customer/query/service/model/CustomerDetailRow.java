package com.gaekdam.gaekdambe.customer_service.customer.query.service.model;

import com.gaekdam.gaekdambe.customer_service.customer.command.domain.ContractType;
import com.gaekdam.gaekdambe.customer_service.customer.command.domain.CustomerStatus;
import com.gaekdam.gaekdambe.customer_service.customer.command.domain.NationalityType;
import com.gaekdam.gaekdambe.customer_service.loyalty.command.domain.LoyaltyStatus;
import com.gaekdam.gaekdambe.customer_service.membership.command.domain.MembershipStatus;

import java.time.LocalDateTime;

public record CustomerDetailRow(
        Long customerCode,
        Long hotelGroupCode,
        byte[] customerNameEnc,

        NationalityType nationalityType,
        ContractType contractType,
        CustomerStatus status,

        LocalDateTime cautionAt,
        LocalDateTime inactiveAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,

        Long memberCode,

        MembershipStatus membershipStatus,
        Long membershipGradeCode,
        String membershipGradeName,
        LocalDateTime membershipJoinedAt,
        LocalDateTime membershipExpiredAt,

        LoyaltyStatus loyaltyStatus,
        Long loyaltyGradeCode,
        LocalDateTime loyaltyJoinedAt,
        LocalDateTime loyaltyCalculatedAt
) {}
