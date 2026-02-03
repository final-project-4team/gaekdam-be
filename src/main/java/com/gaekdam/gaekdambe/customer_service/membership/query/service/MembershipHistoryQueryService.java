package com.gaekdam.gaekdambe.customer_service.membership.query.service;

import com.gaekdam.gaekdambe.customer_service.membership.query.dto.response.MembershipHistoryResponse;
import com.gaekdam.gaekdambe.customer_service.membership.query.mapper.MembershipHistoryMapper;
import com.gaekdam.gaekdambe.global.exception.CustomException;
import com.gaekdam.gaekdambe.global.exception.ErrorCode;
import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MembershipHistoryQueryService {

    private final MembershipHistoryMapper mapper;

    public PageResponse<MembershipHistoryResponse> getHistory(
            PageRequest page,
            Long hotelGroupCode,
            Long customerCode,
            LocalDateTime from,
            LocalDateTime to
    ) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new CustomException(ErrorCode.MEMBERSHIP_HISTORY_INVALID_PERIOD);
        }
        List<MembershipHistoryResponse> list =
                mapper.findHistory(page, hotelGroupCode, customerCode, from, to);

        long total =
                mapper.countHistory(hotelGroupCode, customerCode, from, to);

        return new PageResponse<>(list, page.getPage(), page.getSize(), total);
    }
}
