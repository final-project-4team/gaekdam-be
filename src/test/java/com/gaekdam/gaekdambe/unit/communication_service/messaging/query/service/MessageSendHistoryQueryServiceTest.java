package com.gaekdam.gaekdambe.unit.communication_service.messaging.query.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.gaekdam.gaekdambe.communication_service.messaging.query.service.MessageSendHistoryQueryService;
import com.gaekdam.gaekdambe.communication_service.messaging.query.mapper.MessageSendHistoryMapper;
import com.gaekdam.gaekdambe.communication_service.messaging.query.dto.request.MessageSendHistorySearchRequest;
import com.gaekdam.gaekdambe.communication_service.messaging.query.dto.response.MessageSendHistoryResponse;
import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.PageResponse;
import com.gaekdam.gaekdambe.global.paging.SortRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Collections;

class MessageSendHistoryQueryServiceTest {

    @Mock
    MessageSendHistoryMapper mapper;

    private MessageSendHistoryQueryService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new MessageSendHistoryQueryService(mapper);
    }

    @Test
    void getHistories_returnsPageResponse() {
        PageRequest page = new PageRequest();
        page.setPage(1);
        page.setSize(10);

        MessageSendHistorySearchRequest search = new MessageSendHistorySearchRequest();
        SortRequest sort = new SortRequest();
        sort.setSortBy("sendAt");
        sort.setDirection("DESC");

        MessageSendHistoryResponse r = new MessageSendHistoryResponse();
        java.util.List<MessageSendHistoryResponse> list = Collections.singletonList(r);

        when(mapper.findHistories(page, search, sort)).thenReturn(list);
        when(mapper.countHistories(search)).thenReturn(1L);

        PageResponse<MessageSendHistoryResponse> res = service.getHistories(page, search, sort);
        assertThat(res.getTotalElements()).isEqualTo(1L);
        assertThat(res.getContent()).hasSize(1);
    }

    @Test
    void getDetail_throwsWhenNotFound() {
        when(mapper.findHistoryDetail(999L)).thenReturn(null);
        assertThatThrownBy(() -> new MessageSendHistoryQueryService(mapper).getDetail(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("MessageSendHistory not found");
    }
}
