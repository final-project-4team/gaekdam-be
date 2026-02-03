package com.gaekdam.gaekdambe.communication_service.messaging.query.controller;

import com.gaekdam.gaekdambe.communication_service.messaging.query.dto.request.MessageRuleSearch;
import com.gaekdam.gaekdambe.communication_service.messaging.query.dto.response.MessageRuleResponse;
import com.gaekdam.gaekdambe.communication_service.messaging.query.service.MessageRuleQueryService;
import com.gaekdam.gaekdambe.global.config.model.ApiResponse;
import com.gaekdam.gaekdambe.global.config.security.CustomUser;
import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.PageResponse;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name="메시지 규칙")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/message-rules")
public class MessageRuleQueryController {

    private final MessageRuleQueryService service;

    @GetMapping
    @Operation(summary = "메시지 규칙 리스트 조회", description = "메시지 전송에 사용될 메시지 규칙 리스트를 조회한다.")
    public ApiResponse<PageResponse<MessageRuleResponse>> getRules(
            @Parameter(description = "페이징 값") PageRequest page,
            @Parameter(description = "검색 키워드") MessageRuleSearch search,
            @Parameter(description = "정렬 기준") SortRequest sort,
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
