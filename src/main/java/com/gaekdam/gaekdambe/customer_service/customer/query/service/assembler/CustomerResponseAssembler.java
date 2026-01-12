package com.gaekdam.gaekdambe.customer_service.customer.query.service.assembler;

import com.gaekdam.gaekdambe.customer_service.customer.query.dto.response.CustomerDetailResponse;
import com.gaekdam.gaekdambe.customer_service.customer.query.dto.response.CustomerMarketingConsentResponse;
import com.gaekdam.gaekdambe.customer_service.customer.query.dto.response.CustomerStatusHistoryResponse;
import com.gaekdam.gaekdambe.customer_service.customer.query.dto.response.CustomerStatusResponse;
import com.gaekdam.gaekdambe.customer_service.customer.query.dto.response.item.CustomerContactItem;
import com.gaekdam.gaekdambe.customer_service.customer.query.dto.response.item.CustomerListItem;
import com.gaekdam.gaekdambe.customer_service.customer.query.dto.response.item.CustomerMarketingConsentItem;
import com.gaekdam.gaekdambe.customer_service.customer.query.dto.response.item.CustomerStatusHistoryItem;
import com.gaekdam.gaekdambe.customer_service.customer.query.service.model.row.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CustomerResponseAssembler {

    private static final String MEMBERSHIP_NOT_JOINED = "미가입";

    public CustomerListItem toCustomerListItem(CustomerListRow row) {
        // 현재: 고객 복호화 키 확정 전 → null로 내려서 조회
        String customerName = null;
        String primaryContact = null;

        return new CustomerListItem(
                row.customerCode(),
                customerName,
                primaryContact,
                row.status(),
                row.membershipGrade() == null ? MEMBERSHIP_NOT_JOINED : row.membershipGrade(),
                row.loyaltyGrade(),
                row.lastUsedDate(),
                row.inflowChannel(),
                row.contractType(),
                row.nationalityType()
        );
    }

    public CustomerDetailResponse toCustomerDetailResponse(CustomerDetailRow row, List<CustomerContactRow> contactRows) {
        CustomerDetailResponse.MemberInfo memberInfo = null;
        if (row.memberCode() != null) {
            memberInfo = new CustomerDetailResponse.MemberInfo(row.memberCode(), row.memberCreatedAt());
        }

        CustomerDetailResponse.MembershipInfo membershipInfo = new CustomerDetailResponse.MembershipInfo(
                row.membershipGradeName() == null ? MEMBERSHIP_NOT_JOINED : row.membershipGradeName(),
                row.membershipStatus(),
                row.membershipJoinedAt(),
                row.membershipCalculatedAt(),
                row.membershipExpiredAt()
        );

        CustomerDetailResponse.LoyaltyInfo loyaltyInfo = null;
        if (row.loyaltyGradeName() != null || row.loyaltyStatus() != null) {
            loyaltyInfo = new CustomerDetailResponse.LoyaltyInfo(
                    row.loyaltyGradeName(),
                    row.loyaltyStatus(),
                    row.loyaltyJoinedAt(),
                    row.loyaltyCalculatedAt()
            );
        }

        List<CustomerContactItem> contacts = contactRows.stream()
                .map(this::toCustomerContactItem)
                .toList();

        return new CustomerDetailResponse(
                row.customerCode(),
                row.customerName(),
                row.status(),
                row.nationalityType(),
                row.contractType(),
                row.inflowChannel(),
                row.primaryPhone(),
                row.primaryEmail(),
                memberInfo,
                membershipInfo,
                loyaltyInfo,
                contacts
        );
    }

    public CustomerContactItem toCustomerContactItem(CustomerContactRow row) {
        return new CustomerContactItem(
                row.contactCode(),
                row.contactType(),
                row.contactValue(),
                row.isPrimary(),
                row.marketingOptIn(),
                row.consentAt()
        );
    }

    public CustomerStatusResponse toCustomerStatusResponse(CustomerStatusRow row) {
        return new CustomerStatusResponse(
                row.customerCode(),
                row.status(),
                row.cautionAt(),
                row.inactiveAt(),
                row.updatedAt()
        );
    }

    public CustomerStatusHistoryResponse toCustomerStatusHistoryResponse(
            List<CustomerStatusHistoryRow> rows,
            Integer page,
            Integer size,
            Long totalElements
    ) {
        List<CustomerStatusHistoryItem> content = rows.stream()
                .map(this::toCustomerStatusHistoryItem)
                .toList();

        int totalPages = (int) Math.ceil((double) totalElements / size);

        return new CustomerStatusHistoryResponse(
                content,
                page,
                size,
                totalElements,
                totalPages
        );
    }

    public CustomerStatusHistoryItem toCustomerStatusHistoryItem(CustomerStatusHistoryRow row) {
        return new CustomerStatusHistoryItem(
                row.customerStatusHistoryCode(),
                row.beforeStatus(),
                row.afterStatus(),
                row.changeSource(),
                row.changeReason(),
                row.changedAt(),
                row.employeeCode()
        );
    }

    public CustomerMarketingConsentResponse toCustomerMarketingConsentResponse(Long customerCode, List<CustomerContactRow> rows) {
        List<CustomerMarketingConsentItem> items = rows.stream()
                .map(this::toCustomerMarketingConsentItem)
                .toList();

        return new CustomerMarketingConsentResponse(customerCode, items);
    }

    private CustomerMarketingConsentItem toCustomerMarketingConsentItem(CustomerContactRow row) {
        return new CustomerMarketingConsentItem(
                row.contactCode(),
                row.contactType(),
                row.contactValue(),
                row.isPrimary(),
                row.marketingOptIn(),
                row.consentAt()
        );
    }
}
