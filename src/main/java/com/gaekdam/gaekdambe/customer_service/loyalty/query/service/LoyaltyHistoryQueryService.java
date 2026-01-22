package com.gaekdam.gaekdambe.customer_service.loyalty.query.service;

import com.gaekdam.gaekdambe.customer_service.loyalty.query.dto.response.LoyaltyHistoryResponse;
import com.gaekdam.gaekdambe.customer_service.loyalty.query.mapper.LoyaltyHistoryMapper;
import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LoyaltyHistoryQueryService {

    private final LoyaltyHistoryMapper mapper;

    public PageResponse<LoyaltyHistoryResponse> getHistory(
            PageRequest page,
            Long hotelGroupCode,
            Long customerCode,
            LocalDateTime from,
            LocalDateTime to
    ) {
        List<LoyaltyHistoryResponse> list =
                mapper.findHistory(page, hotelGroupCode, customerCode, from, to);

        long total =
                mapper.countHistory(hotelGroupCode, customerCode, from, to);

        return new PageResponse<>(list, page.getPage(), page.getSize(), total);
    }
}
