// src/main/java/com/gaekdam/gaekdambe/customer_service/customer/query/service/CustomerSnapshotQueryService.java
package com.gaekdam.gaekdambe.customer_service.customer.query.service;

import com.gaekdam.gaekdambe.customer_service.customer.query.dto.response.CustomerSnapshotResponse;
import com.gaekdam.gaekdambe.customer_service.customer.query.mapper.CustomerSnapshotMapper;
import com.gaekdam.gaekdambe.customer_service.customer.query.service.model.row.CustomerSnapshotRow;
import com.gaekdam.gaekdambe.global.exception.CustomException;
import com.gaekdam.gaekdambe.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerSnapshotQueryService {

    private final CustomerSnapshotMapper snapshotMapper;

    public CustomerSnapshotResponse getSnapshot(Long hotelGroupCode, Long customerCode) {
        CustomerSnapshotRow row = snapshotMapper.findCustomerSnapshot(hotelGroupCode, customerCode);
        if (row == null) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "존재하지 않는 고객입니다.");
        }

        return CustomerSnapshotResponse.builder()
                .customerCode(row.getCustomerCode())
                .totalStayCount(row.getTotalStayCount())
                .ltvAmount(row.getLtvAmount() == null ? BigDecimal.ZERO : row.getLtvAmount())
                .lastUsedAt(row.getLastUsedAt())
                .unresolvedInquiryCount(row.getUnresolvedInquiryCount())
                .build();
    }
}
