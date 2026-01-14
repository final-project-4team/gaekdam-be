package com.gaekdam.gaekdambe.communication_service.messaging.query.controller;

import com.gaekdam.gaekdambe.communication_service.messaging.query.dto.request.MessageRuleSearch;
import com.gaekdam.gaekdambe.communication_service.messaging.query.dto.response.MessageRuleResponse;
import com.gaekdam.gaekdambe.communication_service.messaging.query.service.MessageRuleQueryService;
import com.gaekdam.gaekdambe.global.config.model.ApiResponse;
import com.gaekdam.gaekdambe.global.config.security.CustomUser;
import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.PageResponse;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/message-rules")
public class MessageRuleQueryController {

    private final MessageRuleQueryService service;

    @GetMapping
    public ApiResponse<PageResponse<MessageRuleResponse>> getRules(
            PageRequest page,
            MessageRuleSearch search,
            SortRequest sort,
            @AuthenticationPrincipal CustomUser customUser
    ) {
        // 호텔/지점 기준
        search.setPropertyCode(customUser.getPropertyCode());

        if (sort == null || sort.getSortBy() == null) {
            sort = new SortRequest();
            sort.setSortBy("priority");
            sort.setDirection("ASC");
        }

        return ApiResponse.success(
                service.getRules(page, search, sort)
        );
    }
}
