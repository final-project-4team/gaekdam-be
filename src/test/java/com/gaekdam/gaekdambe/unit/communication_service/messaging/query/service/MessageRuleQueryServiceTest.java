package com.gaekdam.gaekdambe.unit.communication_service.messaging.query.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.gaekdam.gaekdambe.communication_service.messaging.query.service.MessageRuleQueryService;
import com.gaekdam.gaekdambe.communication_service.messaging.query.mapper.MessageRuleQueryMapper;
import com.gaekdam.gaekdambe.communication_service.messaging.query.dto.request.MessageRuleSearch;
import com.gaekdam.gaekdambe.communication_service.messaging.query.dto.response.MessageRuleResponse;
import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.PageResponse;
import com.gaekdam.gaekdambe.global.paging.SortRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;

class MessageRuleQueryServiceTest {

    @Mock
    MessageRuleQueryMapper mapper;

    private MessageRuleQueryService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new MessageRuleQueryService(mapper);
    }

    @Test
    void getRules_returnsPage() {
        PageRequest page = new PageRequest();
        page.setPage(1);
        page.setSize(10);

        MessageRuleSearch search = new MessageRuleSearch();
        SortRequest sort = new SortRequest();
        sort.setSortBy("priority");

        MessageRuleResponse r = new MessageRuleResponse(1L, 2L, 3L, "RESERVATION", "SMS", true, 1);
        java.util.List<MessageRuleResponse> list = Collections.singletonList(r);

        when(mapper.findRules(search, page, sort)).thenReturn(list);
        when(mapper.countRules(search)).thenReturn(1L);

        PageResponse<MessageRuleResponse> res = service.getRules(page, search, sort);
        assertThat(res.getTotalElements()).isEqualTo(1L);
        assertThat(res.getContent()).hasSize(1);
    }
}
