package com.gaekdam.gaekdambe.communication_service.messaging.query.mapper;

import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.entity.MessageRule;
import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.enums.VisitorType;
import com.gaekdam.gaekdambe.communication_service.messaging.query.dto.request.MessageRuleSearch;
import com.gaekdam.gaekdambe.communication_service.messaging.query.dto.response.MessageRuleResponse;
import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MessageRuleQueryMapper {

    List<MessageRuleResponse> findRules(
            @Param("search") MessageRuleSearch search,
            @Param("page") PageRequest page,
            @Param("sort") SortRequest sort
    );

    long countRules(@Param("search") MessageRuleSearch search);

    List<MessageRule> findActiveRulesForSchedule(
            @Param("hotelGroupCode") Long hotelGroupCode,
            @Param("stageCode") Long stageCode,
            @Param("visitorType") VisitorType visitorType
    );
}

