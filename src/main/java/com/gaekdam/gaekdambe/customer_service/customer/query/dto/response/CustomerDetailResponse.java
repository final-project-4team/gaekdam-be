package com.gaekdam.gaekdambe.customer_service.customer.query.dto.response;

import com.gaekdam.gaekdambe.customer_service.customer.command.domain.ChangeSource;
import com.gaekdam.gaekdambe.customer_service.customer.command.domain.ContactType;
import com.gaekdam.gaekdambe.customer_service.customer.command.domain.ContractType;
import com.gaekdam.gaekdambe.customer_service.customer.command.domain.CustomerStatus;
import com.gaekdam.gaekdambe.customer_service.customer.command.domain.NationalityType;
import com.gaekdam.gaekdambe.customer_service.loyalty.command.domain.LoyaltyStatus;
import com.gaekdam.gaekdambe.customer_service.membership.command.domain.MembershipStatus;

import java.time.LocalDateTime;
import java.util.List;

public record CustomerDetailResponse(
        Basic basic,
        Classification classification,
        Membership membership,
        Loyalty loyalty,
        List<Contact> contacts,
        List<StatusHistory> statusHistories
) {
    public record Basic(
            Long customerCode,
            Long hotelGroupCode,
            String customerName,      // 복호화 붙이면 채움
            CustomerStatus status,
            LocalDateTime cautionAt,
            LocalDateTime inactiveAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            Long memberCode
    ) {}

    public record Classification(
            NationalityType nationalityType,
            ContractType contractType
    ) {}

    public record Membership(
            Boolean hasMembership,
            MembershipStatus membershipStatus,
            Long membershipGradeCode,
            String membershipGradeName,
            LocalDateTime joinedAt,
            LocalDateTime expiredAt
    ) {}

    public record Loyalty(
            Boolean hasLoyalty,
            LoyaltyStatus loyaltyStatus,
            Long loyaltyGradeCode,
            LocalDateTime joinedAt,
            LocalDateTime calculatedAt
    ) {}

    public record Contact(
            Long contactCode,
            ContactType contactType,
            String contactValue,      // 복호화 붙이면 채움
            Boolean isPrimary,
            Boolean marketingOptIn,
            LocalDateTime consentAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {}

    public record StatusHistory(
            Long customerStatusHistoryCode,
            CustomerStatus beforeStatus,
            CustomerStatus afterStatus,
            ChangeSource changeSource,
            Long changedByMemberCode,
            String reason,
            LocalDateTime changedAt
    ) {}
}
