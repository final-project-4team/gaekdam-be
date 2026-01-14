package com.gaekdam.gaekdambe.communication_service.messaging.query.controller;

import com.gaekdam.gaekdambe.communication_service.messaging.query.dto.request.MessageTemplateSearch;
import com.gaekdam.gaekdambe.communication_service.messaging.query.dto.response.MessageTemplateResponse;
import com.gaekdam.gaekdambe.communication_service.messaging.query.service.MessageTemplateQueryService;
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
@RequestMapping("/api/v1/message-templates")
public class MessageTemplateQueryController {

    private final MessageTemplateQueryService service;

    @GetMapping
    public ApiResponse<PageResponse<MessageTemplateResponse>> getTemplates(
            PageRequest page,
            MessageTemplateSearch search,
            SortRequest sort,
            @AuthenticationPrincipal CustomUser customUser
    ) {
        //  로그인 객체에서 지점 스코프 주입
        search.setPropertyCode(customUser.getPropertyCode());

        if (sort == null || sort.getSortBy() == null) {
            sort = new SortRequest();
            sort.setSortBy("stage_code");
            sort.setDirection("ASC");
        }

        return ApiResponse.success(
                service.getTemplates(page, search, sort) );
    }
}

