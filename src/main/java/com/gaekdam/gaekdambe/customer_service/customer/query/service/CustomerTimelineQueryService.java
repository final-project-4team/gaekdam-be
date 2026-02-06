// src/main/java/com/gaekdam/gaekdambe/customer_service/customer/query/service/CustomerTimelineQueryService.java
package com.gaekdam.gaekdambe.customer_service.customer.query.service;

import com.gaekdam.gaekdambe.customer_service.customer.query.dto.response.CustomerTimelineResponse;
import com.gaekdam.gaekdambe.customer_service.customer.query.dto.response.item.CustomerTimelineItem;
import com.gaekdam.gaekdambe.customer_service.customer.query.mapper.CustomerTimelineMapper;
import com.gaekdam.gaekdambe.customer_service.customer.query.service.model.row.CustomerTimelineRow;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerTimelineQueryService {

    private final CustomerTimelineMapper timelineMapper;

    public CustomerTimelineResponse getTimeline(Long hotelGroupCode, Long customerCode, int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 200)); // 과도한 조회 방지

        List<CustomerTimelineRow> rows =
                timelineMapper.findCustomerTimeline(hotelGroupCode, customerCode, safeLimit);

        List<CustomerTimelineItem> items = rows.stream()
                .map(r -> CustomerTimelineItem.builder()
                        .eventType(r.getEventType())
                        .occurredAt(r.getOccurredAt())
                        .refId(r.getRefId())
                        .title(r.getTitle())
                        .summary(r.getSummary())
                        .build()
                )
                .toList();

        return CustomerTimelineResponse.builder()
                .customerCode(customerCode)
                .limit(safeLimit)
                .items(items)
                .build();
    }
}
