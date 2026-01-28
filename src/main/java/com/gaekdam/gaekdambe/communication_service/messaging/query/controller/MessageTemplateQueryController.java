package com.gaekdam.gaekdambe.communication_service.messaging.query.controller;

import com.gaekdam.gaekdambe.communication_service.messaging.query.dto.request.MessageTemplateSearch;
import com.gaekdam.gaekdambe.communication_service.messaging.query.dto.response.MessageTemplateDetailResponse;
import com.gaekdam.gaekdambe.communication_service.messaging.query.dto.response.MessageTemplateResponse;
import com.gaekdam.gaekdambe.communication_service.messaging.query.dto.response.MessageTemplateSettingResponse;
import com.gaekdam.gaekdambe.communication_service.messaging.query.service.MessageTemplateQueryService;
import com.gaekdam.gaekdambe.global.config.model.ApiResponse;
import com.gaekdam.gaekdambe.global.config.security.CustomUser;
import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.PageResponse;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/message-templates")
public class MessageTemplateQueryController {

    private final MessageTemplateQueryService service;

    /** 기존: 리스트/검색/페이징용 */
    @GetMapping
    public ApiResponse<PageResponse<MessageTemplateResponse>> getTemplates(
            PageRequest page,
            MessageTemplateSearch search,
            SortRequest sort,
            @AuthenticationPrincipal CustomUser user
    ) {
        search.setPropertyCode(user.getPropertyCode());
        return ApiResponse.success(service.getTemplates(page, search, sort));
    }


    @GetMapping("/{templateCode}")
    public ApiResponse<MessageTemplateDetailResponse> getTemplate(
            @PathVariable Long templateCode
    ) {
        return ApiResponse.success(
                service.getTemplate(templateCode)
        );
    }


    /** 설정 화면 전용 (여정 기준) */
    @GetMapping("/setting")
    public ApiResponse<List<MessageTemplateSettingResponse>> getSettingTemplates(
            @AuthenticationPrincipal CustomUser user
    ) {
        return ApiResponse.success(
                service.getSettingTemplates(user.getPropertyCode())
        );
    }
}
