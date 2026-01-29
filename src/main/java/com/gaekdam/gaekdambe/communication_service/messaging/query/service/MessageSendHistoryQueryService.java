package com.gaekdam.gaekdambe.communication_service.messaging.query.service;

import com.gaekdam.gaekdambe.communication_service.messaging.query.dto.request.MessageSendHistorySearchRequest;
import com.gaekdam.gaekdambe.communication_service.messaging.query.dto.response.MessageSendHistoryDetailResponse;
import com.gaekdam.gaekdambe.communication_service.messaging.query.dto.response.MessageSendHistoryResponse;
import com.gaekdam.gaekdambe.communication_service.messaging.query.mapper.MessageSendHistoryMapper;
import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.PageResponse;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageSendHistoryQueryService {

    private final MessageSendHistoryMapper mapper;

    public PageResponse<MessageSendHistoryResponse> getHistories(
            PageRequest page,
            MessageSendHistorySearchRequest search,
            SortRequest sort
    ) {
        List<MessageSendHistoryResponse> list =
                mapper.findHistories(page, search, sort);

        long total =
                mapper.countHistories(search);

        return new PageResponse<>(
                list,
                page.getPage(),
                page.getSize(),
                total
        );
    }


    /**
     * 발송 이력 상세 단건 조회
     */
    public MessageSendHistoryDetailResponse getDetail(Long sendCode) {

        MessageSendHistoryDetailResponse detail = mapper.findHistoryDetail(sendCode);

        if (detail == null) {
            throw new IllegalArgumentException("MessageSendHistory not found. sendCode=" + sendCode);
        }

        return detail;
    }
}
