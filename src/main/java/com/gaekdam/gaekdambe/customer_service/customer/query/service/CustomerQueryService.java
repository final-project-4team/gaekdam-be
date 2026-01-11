package com.gaekdam.gaekdambe.customer_service.customer.query.service;

import com.gaekdam.gaekdambe.customer_service.customer.query.dto.request.CustomerDetailRequest;
import com.gaekdam.gaekdambe.customer_service.customer.query.dto.request.CustomerListSearchRequest;
import com.gaekdam.gaekdambe.customer_service.customer.query.dto.response.*;
import com.gaekdam.gaekdambe.customer_service.customer.query.mapper.CustomerMapper;
import com.gaekdam.gaekdambe.customer_service.customer.query.service.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerQueryService {

    private final CustomerMapper customerMapper;

    public CustomerListResponse getCustomerList(CustomerListSearchRequest request) {
        if (request == null) throw new IllegalArgumentException("request must not be null");
        if (request.hotelGroupCode() == null) throw new IllegalArgumentException("hotelGroupCode must not be null");

        CustomerListCondition condition = new CustomerListCondition(
                request.hotelGroupCode(),
                request.customerCode(),
                request.customerNameHash(),
                request.contactValueHash(),
                request.status(),
                request.contractType(),
                request.nationalityType(),
                request.membershipGradeCode(),
                request.loyaltyGradeCode()
        );

        List<CustomerListRow> rows = customerMapper.findCustomerList(condition);

        List<CustomerListResponse.Item> items = rows.stream()
                .map(row -> new CustomerListResponse.Item(
                        row.customerCode(),
                        null,
                        null,
                        row.status(),
                        row.membershipGradeCode(),
                        row.membershipGradeName(),
                        row.loyaltyGradeCode(),
                        null,
                        null,
                        row.contractType(),
                        row.nationalityType()
                ))
                .toList();

        return CustomerListResponse.of(items);
    }

    public CustomerDetailResponse getCustomerDetail(CustomerDetailRequest request) {
        CustomerDetailCondition condition = validateAndBuildDetailCondition(request);

        CustomerDetailRow row = customerMapper.findCustomerDetail(condition);
        if (row == null) {
            throw new IllegalArgumentException("Customer not found. customerCode=" + request.customerCode());
        }

        List<CustomerContactRow> contacts = customerMapper.findCustomerContacts(condition);
        List<CustomerStatusHistoryRow> histories = customerMapper.findCustomerStatusHistories(condition);

        return new CustomerDetailResponse(
                new CustomerDetailResponse.Basic(
                        row.customerCode(),
                        row.hotelGroupCode(),
                        null,
                        row.status(),
                        row.cautionAt(),
                        row.inactiveAt(),
                        row.createdAt(),
                        row.updatedAt(),
                        row.memberCode()
                ),
                new CustomerDetailResponse.Classification(
                        row.nationalityType(),
                        row.contractType()
                ),
                buildMembership(row), // ✅ hasMembership
                buildLoyalty(row),    // ✅ hasLoyalty
                toContactResponses(contacts),
                histories.stream().map(this::toStatusHistoryResponse).toList()
        );
    }

    public CustomerStatusResponse getCustomerStatus(CustomerDetailRequest request) {
        CustomerDetailCondition condition = validateAndBuildDetailCondition(request);
        assertCustomerExists(condition, request.customerCode());

        CustomerStatusRow row = customerMapper.findCustomerStatus(condition);
        return new CustomerStatusResponse(
                row.customerCode(),
                row.hotelGroupCode(),
                row.status(),
                row.cautionAt(),
                row.inactiveAt(),
                row.updatedAt()
        );
    }

    public CustomerStatusHistoryResponse getCustomerStatusHistories(CustomerDetailRequest request) {
        CustomerDetailCondition condition = validateAndBuildDetailCondition(request);
        assertCustomerExists(condition, request.customerCode());

        List<CustomerStatusHistoryRow> rows = customerMapper.findCustomerStatusHistories(condition);
        List<CustomerDetailResponse.StatusHistory> items = rows.stream()
                .map(this::toStatusHistoryResponse)
                .toList();

        return CustomerStatusHistoryResponse.of(request.customerCode(), request.hotelGroupCode(), items);
    }

    public CustomerMarketingConsentResponse getCustomerMarketingConsents(CustomerDetailRequest request) {
        CustomerDetailCondition condition = validateAndBuildDetailCondition(request);
        assertCustomerExists(condition, request.customerCode());

        List<CustomerContactRow> contacts = customerMapper.findCustomerContacts(condition);

        List<CustomerMarketingConsentResponse.Item> items = contacts.stream()
                .map(c -> new CustomerMarketingConsentResponse.Item(
                        c.contactCode(),
                        c.contactType(),
                        c.marketingOptIn(),
                        c.consentAt(),
                        c.isPrimary()
                ))
                .toList();

        return CustomerMarketingConsentResponse.of(request.customerCode(), request.hotelGroupCode(), items);
    }

    private CustomerDetailCondition validateAndBuildDetailCondition(CustomerDetailRequest request) {
        if (request == null) throw new IllegalArgumentException("request must not be null");
        if (request.hotelGroupCode() == null) throw new IllegalArgumentException("hotelGroupCode must not be null");
        if (request.customerCode() == null) throw new IllegalArgumentException("customerCode must not be null");
        return new CustomerDetailCondition(request.hotelGroupCode(), request.customerCode());
    }

    private void assertCustomerExists(CustomerDetailCondition condition, Long customerCode) {
        if (!customerMapper.existsCustomer(condition)) {
            throw new IllegalArgumentException("Customer not found. customerCode=" + customerCode);
        }
    }

    private List<CustomerDetailResponse.Contact> toContactResponses(List<CustomerContactRow> contacts) {
        return contacts.stream()
                .map(c -> new CustomerDetailResponse.Contact(
                        c.contactCode(),
                        c.contactType(),
                        null,
                        c.isPrimary(),
                        c.marketingOptIn(),
                        c.consentAt(),
                        c.createdAt(),
                        c.updatedAt()
                ))
                .toList();
    }

    private CustomerDetailResponse.StatusHistory toStatusHistoryResponse(CustomerStatusHistoryRow h) {
        return new CustomerDetailResponse.StatusHistory(
                h.customerStatusHistoryCode(),
                h.beforeStatus(),
                h.afterStatus(),
                h.changeSource(),
                h.changedByMemberCode(),
                h.reason(),
                h.changedAt()
        );
    }

    private CustomerDetailResponse.Membership buildMembership(CustomerDetailRow row) {
        boolean hasMembership = (row.membershipStatus() != null);

        if (!hasMembership) {
            return new CustomerDetailResponse.Membership(
                    Boolean.FALSE,
                    null,
                    null,
                    null,
                    null,
                    null
            );
        }

        return new CustomerDetailResponse.Membership(
                Boolean.TRUE,
                row.membershipStatus(),
                row.membershipGradeCode(),
                row.membershipGradeName(),
                row.membershipJoinedAt(),
                row.membershipExpiredAt()
        );
    }

    private CustomerDetailResponse.Loyalty buildLoyalty(CustomerDetailRow row) {
        boolean hasLoyalty = (row.loyaltyStatus() != null);

        if (!hasLoyalty) {
            return new CustomerDetailResponse.Loyalty(
                    Boolean.FALSE,
                    null,
                    null,
                    null,
                    null
            );
        }

        return new CustomerDetailResponse.Loyalty(
                Boolean.TRUE,
                row.loyaltyStatus(),
                row.loyaltyGradeCode(),
                row.loyaltyJoinedAt(),
                row.loyaltyCalculatedAt()
        );
    }
}
