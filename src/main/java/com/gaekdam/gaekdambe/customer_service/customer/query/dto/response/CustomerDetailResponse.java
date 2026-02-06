package com.gaekdam.gaekdambe.customer_service.customer.query.dto.response;

import com.gaekdam.gaekdambe.customer_service.customer.command.domain.ContractType;
import com.gaekdam.gaekdambe.customer_service.customer.command.domain.CustomerStatus;
import com.gaekdam.gaekdambe.customer_service.customer.command.domain.NationalityType;
import com.gaekdam.gaekdambe.customer_service.customer.query.dto.response.item.CustomerContactItem;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDetailResponse {

    private Long customerCode;
    private String customerName;

    private CustomerStatus status;
    private NationalityType nationalityType;
    private ContractType contractType;

    private String inflowChannel;

    private String primaryPhone;
    private String primaryEmail;

    private MemberInfo member;           // 없으면 null
    private MembershipInfo membership;   // 없으면 gradeName="미가입" 세팅해서 내려주기
    private LoyaltyInfo loyalty;         // 없으면 null

    private List<CustomerContactItem> contacts;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MemberInfo {
        private Long memberCode;
        private LocalDateTime createdAt;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MembershipInfo {
        private String gradeName;        // 없으면 "미가입"
        private String membershipStatus; // 없으면 null
        private LocalDateTime joinedAt;
        private LocalDateTime calculatedAt;
        private LocalDateTime expiredAt;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoyaltyInfo {
        private String gradeName;
        private String loyaltyStatus;
        private LocalDateTime joinedAt;
        private LocalDateTime calculatedAt;
    }
}
