package com.gaekdam.gaekdambe.communication_service.messaging.query.service;

import com.gaekdam.gaekdambe.communication_service.messaging.query.dto.request.MessageRuleSearch;
import com.gaekdam.gaekdambe.communication_service.messaging.query.dto.response.MessageRuleResponse;
import com.gaekdam.gaekdambe.communication_service.messaging.query.mapper.MessageRuleQueryMapper;
import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.PageResponse;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageRuleQueryService {

    private final MessageRuleQueryMapper mapper;

    public PageResponse<MessageRuleResponse> getRules(
            PageRequest page,
            MessageRuleSearch search,
            SortRequest sort
    ) {

        List<MessageRuleResponse> list =
                mapper.findRules( search, page, sort);

        long total =
                mapper.countRules(search);

        return new PageResponse<>(
                list,
                page.getPage(),
                page.getSize(),
                total
        );
    }
}
