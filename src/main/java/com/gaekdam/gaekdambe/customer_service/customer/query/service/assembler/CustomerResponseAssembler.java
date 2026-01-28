package com.gaekdam.gaekdambe.customer_service.customer.query.service.assembler;

import com.gaekdam.gaekdambe.customer_service.customer.command.domain.ChangeSource;
import com.gaekdam.gaekdambe.customer_service.customer.query.dto.response.CustomerDetailResponse;
import com.gaekdam.gaekdambe.customer_service.customer.query.dto.response.CustomerMarketingConsentResponse;
import com.gaekdam.gaekdambe.customer_service.customer.query.dto.response.CustomerStatusHistoryResponse;
import com.gaekdam.gaekdambe.customer_service.customer.query.dto.response.CustomerStatusResponse;
import com.gaekdam.gaekdambe.customer_service.customer.query.dto.response.item.CustomerContactItem;
import com.gaekdam.gaekdambe.customer_service.customer.query.dto.response.item.CustomerListItem;
import com.gaekdam.gaekdambe.customer_service.customer.query.dto.response.item.CustomerMarketingConsentItem;
import com.gaekdam.gaekdambe.customer_service.customer.query.dto.response.item.CustomerStatusHistoryItem;
import com.gaekdam.gaekdambe.customer_service.customer.query.service.model.row.*;
import com.gaekdam.gaekdambe.global.crypto.DecryptionService;
import com.gaekdam.gaekdambe.global.crypto.MaskingUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CustomerResponseAssembler {

    private final DecryptionService decryptionService;

    private static final String MEMBERSHIP_NOT_JOINED = "미가입";

    /**
     * 고객 목록 Row -> Item
     * - customerName / primaryContact는 암호문(byte[])이라 복호화해서 내려줌
     */
    public CustomerListItem toCustomerListItem(CustomerListRow row) {
        Long customerCode = row.customerCode();

        byte[] dekEnc = row.dekEnc();
        byte[] customerNameEnc = row.customerNameEnc();
        byte[] primaryContactEnc = row.primaryContactEnc();

        String customerName = decryptionService.decrypt(customerCode, dekEnc, customerNameEnc);
        String primaryContact = decryptionService.decrypt(customerCode, dekEnc, primaryContactEnc);

        customerName = MaskingUtils.maskName(customerName);
        primaryContact = maskContact(primaryContact);

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

    /**
     * 고객 상세 Row + 연락처 Row들 -> 상세 Response
     * - customerName / primaryPhone / primaryEmail은 암호문(byte[])이라 복호화해서 내려줌
     */
    public CustomerDetailResponse toCustomerDetailResponse(CustomerDetailRow row, List<CustomerContactRow> contactRows) {
        Long customerCode = row.customerCode();
        byte[] dekEnc = row.dekEnc();

        String customerName = decryptionService.decrypt(customerCode, dekEnc, row.customerNameEnc());
        String primaryPhone = decryptionService.decrypt(customerCode, dekEnc, row.primaryPhoneEnc());
        String primaryEmail = decryptionService.decrypt(customerCode, dekEnc, row.primaryEmailEnc());

        // 상세에서 마스킹 필요 시 사용
//        customerName = MaskingUtils.maskName(customerName);
//        primaryPhone = MaskingUtils.maskPhone(primaryPhone);
//        primaryEmail = MaskingUtils.maskEmail(primaryEmail);

        CustomerDetailResponse.MemberInfo memberInfo = null;
        if (row.memberCode() != null) {
            memberInfo = new CustomerDetailResponse.MemberInfo(row.memberCode(), row.memberCreatedAt());
        }

        // 멤버십이 없으면 gradeName만 "미가입"으로 내려줌
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

        // contacts는 contact_value_enc를 복호화해서 item으로 변환
        List<CustomerContactItem> contacts = contactRows.stream()
                .map(cr -> toCustomerContactItem(customerCode, dekEnc, cr))
                .toList();

        return new CustomerDetailResponse(
                row.customerCode(),
                customerName,
                row.status(),
                row.nationalityType(),
                row.contractType(),
                row.inflowChannel(),
                primaryPhone,
                primaryEmail,
                memberInfo,
                membershipInfo,
                loyaltyInfo,
                contacts
        );
    }

    /**
     * 고객 연락처 Row -> Item
     * - contact_value_enc만 복호화
     * - contactType은 enum이라 name()으로 String 변환
     */
    private CustomerContactItem toCustomerContactItem(Long customerCode, byte[] dekEnc, CustomerContactRow row) {
        String contactValue = decryptionService.decrypt(customerCode, dekEnc, row.contactValueEnc());

        return new CustomerContactItem(
                row.contactCode(),
                row.contactType().name(),
                contactValue,
                row.isPrimary(),
                row.marketingOptIn(),
                row.consentAt()
        );
    }

    /**
     * 고객 상태 Row -> Response
     */
    public CustomerStatusResponse toCustomerStatusResponse(CustomerStatusRow row) {
        return new CustomerStatusResponse(
                row.customerCode(),
                row.status(),
                row.cautionAt(),
                row.inactiveAt(),
                row.updatedAt()
        );
    }

    /**
     * 고객 상태 이력 Rows -> Response(paging)
     */
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

    /**
     * 고객 상태 이력 Row -> Item
     */
    public CustomerStatusHistoryItem toCustomerStatusHistoryItem(CustomerStatusHistoryRow row) {

        String employeeName = resolveEmployeeName(row);

        return new CustomerStatusHistoryItem(
                row.customerStatusHistoryCode(),
                row.beforeStatus(),
                row.afterStatus(),
                row.changeSource(),
                row.changeReason(),
                row.changedAt(),
                row.employeeCode(),
                employeeName
        );
    }

    /**
     * 마케팅 수신동의: 동의 여부만 내려줌 (연락처 값 복호화/노출 안함)
     * - Response 필드는 consents로 통일
     */
    public CustomerMarketingConsentResponse toCustomerMarketingConsentResponse(Long customerCode, List<CustomerContactRow> rows) {
        List<CustomerMarketingConsentItem> consents = rows.stream()
                .map(this::toCustomerMarketingConsentItem)
                .toList();

        return new CustomerMarketingConsentResponse(customerCode, consents);
    }

    /**
     * 마케팅 수신동의 Row -> Item
     * - contactType은 enum이라 name()으로 String 변환
     * - contact_value_enc는 사용하지 않음
     */
    private CustomerMarketingConsentItem toCustomerMarketingConsentItem(CustomerContactRow row) {
        return new CustomerMarketingConsentItem(
                row.contactCode(),
                row.contactType().name(),
                row.isPrimary(),
                row.marketingOptIn(),
                row.consentAt()
        );
    }
    private String maskContact(String value) {
        if (value == null || value.isBlank()) return value;

        return value.contains("@")
                ? MaskingUtils.maskEmail(value)
                : MaskingUtils.maskPhone(value);
    }
    private String resolveEmployeeName(CustomerStatusHistoryRow row) {
        if (row.changeSource() == ChangeSource.SYSTEM) {
            return "SYSTEM";
        }

        if (row.employeeCode() == null || row.employeeDekEnc() == null || row.employeeNameEnc() == null) {
            return null;
        }

        String name = decryptionService.decrypt(
                row.employeeCode(),
                row.employeeDekEnc(),
                row.employeeNameEnc()
        );

        if (name == null || name.isBlank()) return null;
        return MaskingUtils.maskName(name);
    }

}
