package com.gaekdam.gaekdambe.customer_service.customer.query.service;

import com.gaekdam.gaekdambe.customer_service.customer.query.dto.request.CustomerListSearchRequest;
import com.gaekdam.gaekdambe.customer_service.customer.query.dto.response.CustomerListResponse;
import com.gaekdam.gaekdambe.customer_service.customer.query.mapper.CustomerMapper;
import com.gaekdam.gaekdambe.customer_service.customer.query.service.model.CustomerUnifiedListCondition;
import com.gaekdam.gaekdambe.customer_service.customer.query.service.model.CustomerUnifiedRow;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerQueryService {

    private final CustomerMapper customerMapper;

    public CustomerListResponse loadCustomerUnifiedList(CustomerListSearchRequest request) {
        if (request.hotelGroupCode() == null) {
            throw new IllegalArgumentException("hotelGroupCode must not be null");
        }

        int page = request.normalizedPage();
        int size = request.normalizedSize();

        CustomerUnifiedListCondition condition = CustomerUnifiedListCondition.of(
                request.hotelGroupCode(),
                request.customerCode(),
                request.customerNameHash(),
                request.contactValueHash(),
                request.status(),
                request.contractType(),
                request.nationalityType(),
                request.membershipGradeName(),
                request.loyaltyGradeCode(),
                request.offset(),
                size
        );

        long totalCount = customerMapper.countCustomerUnifiedList(condition);
        List<CustomerUnifiedRow> rows = customerMapper.findCustomerUnifiedList(condition);

        List<CustomerListResponse.Item> items = rows.stream()
                .map(row -> new CustomerListResponse.Item(
                        row.customerCode(),
                        null, // 복호화 모듈 붙기 전까지는 표시 불가
                        null, // 복호화 모듈 붙기 전까지는 표시 불가
                        row.customerStatus(),
                        row.membershipGradeName(),
                        row.loyaltyGradeCode(),
                        null, // TODO 최근 이용 도메인
                        null, // TODO 유입 채널 도메인
                        row.contractType(),
                        row.nationalityType()
                ))
                .toList();

        return CustomerListResponse.of(totalCount, page, size, items);
    }
}
