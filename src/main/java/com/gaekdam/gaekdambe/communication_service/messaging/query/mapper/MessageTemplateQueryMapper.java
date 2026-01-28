package com.gaekdam.gaekdambe.communication_service.messaging.query.mapper;

import com.gaekdam.gaekdambe.communication_service.messaging.query.dto.request.MessageTemplateSearch;
import com.gaekdam.gaekdambe.communication_service.messaging.query.dto.response.MessageTemplateDetailResponse;
import com.gaekdam.gaekdambe.communication_service.messaging.query.dto.response.MessageTemplateResponse;
import com.gaekdam.gaekdambe.communication_service.messaging.query.dto.response.MessageTemplateSettingResponse;
import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MessageTemplateQueryMapper {

    List<MessageTemplateResponse> findTemplates(
            @Param("search") MessageTemplateSearch search,
            @Param("page") PageRequest page,
            @Param("sort") SortRequest sort
    );


    long countTemplates(@Param("search") MessageTemplateSearch search);


    // 단건
    MessageTemplateDetailResponse findTemplateDetail(Long templateCode);
}

