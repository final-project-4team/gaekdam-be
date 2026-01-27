package com.gaekdam.gaekdambe.communication_service.messaging.query.controller;


import com.gaekdam.gaekdambe.communication_service.messaging.query.dto.request.MessageSendHistorySearchRequest;
import com.gaekdam.gaekdambe.communication_service.messaging.query.dto.response.MessageSendHistoryResponse;
import com.gaekdam.gaekdambe.communication_service.messaging.query.service.MessageSendHistoryQueryService;
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
@RequestMapping("/api/v1/message-send-histories")
public class MessageSendHistoryQueryController {

    private final MessageSendHistoryQueryService service;

    @GetMapping
    public ApiResponse<PageResponse<MessageSendHistoryResponse>> getHistories(
            PageRequest page,
            MessageSendHistorySearchRequest search,
            SortRequest sort,
            @AuthenticationPrincipal CustomUser customUser
    ) {
        //  SaaS 스코프 주입
        search.setHotelGroupCode(customUser.getHotelGroupCode());

        if (sort == null || sort.getSortBy() == null) {
            sort = new SortRequest();
            sort.setSortBy("sentAt");
            sort.setDirection("DESC");
        }

        return ApiResponse.success(
                service.getHistories(page, search, sort)
        );
    }
}
