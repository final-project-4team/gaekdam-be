package com.gaekdam.gaekdambe.customer_service.customer.query.service.model.row;

import com.gaekdam.gaekdambe.customer_service.customer.command.domain.ContractType;
import com.gaekdam.gaekdambe.customer_service.customer.command.domain.CustomerStatus;
import com.gaekdam.gaekdambe.customer_service.customer.command.domain.NationalityType;

import java.time.LocalDateTime;

public record CustomerDetailRow(

        Long customerCode,

        byte[] customerNameEnc,
        byte[] dekEnc,

        CustomerStatus status,
        NationalityType nationalityType,
        ContractType contractType,

        String inflowChannel,

        byte[] primaryPhoneEnc,
        byte[] primaryEmailEnc,

        // member
        Long memberCode,
        LocalDateTime memberCreatedAt,

        // membership (없으면 null일 수 있음)
        String membershipGradeName,
        String membershipStatus,
        LocalDateTime membershipJoinedAt,
        LocalDateTime membershipCalculatedAt,
        LocalDateTime membershipExpiredAt,

        // loyalty (없으면 null일 수 있음)
        String loyaltyGradeName,
        String loyaltyStatus,
        LocalDateTime loyaltyJoinedAt,
        LocalDateTime loyaltyCalculatedAt
) {
}
