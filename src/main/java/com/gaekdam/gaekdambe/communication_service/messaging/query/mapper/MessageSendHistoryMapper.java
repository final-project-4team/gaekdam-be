package com.gaekdam.gaekdambe.communication_service.messaging.query.mapper;

import com.gaekdam.gaekdambe.communication_service.messaging.query.dto.request.MessageSendHistorySearchRequest;
import com.gaekdam.gaekdambe.communication_service.messaging.query.dto.response.MessageSendHistoryResponse;
import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MessageSendHistoryMapper {

    List<MessageSendHistoryResponse> findHistories(
            @Param("page") PageRequest page,
            @Param("search") MessageSendHistorySearchRequest search,
            @Param("sort") SortRequest sort
    );

    long countHistories(
            @Param("search") MessageSendHistorySearchRequest search
    );
}
