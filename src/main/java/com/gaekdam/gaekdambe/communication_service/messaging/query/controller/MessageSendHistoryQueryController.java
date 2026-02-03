package com.gaekdam.gaekdambe.communication_service.messaging.query.controller;


import com.gaekdam.gaekdambe.communication_service.messaging.query.dto.request.MessageSendHistorySearchRequest;
import com.gaekdam.gaekdambe.communication_service.messaging.query.dto.response.MessageSendHistoryDetailResponse;
import com.gaekdam.gaekdambe.communication_service.messaging.query.dto.response.MessageSendHistoryResponse;
import com.gaekdam.gaekdambe.communication_service.messaging.query.service.MessageSendHistoryQueryService;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name="메시지 전송 기록")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/message-send-histories")
public class MessageSendHistoryQueryController {

    private final MessageSendHistoryQueryService service;

    @GetMapping
    @Operation(summary = "메시지 전송 기록 리스트 조회", description = "메시지가 전송된 기록을 리스트로 조회한다.")
    public ApiResponse<PageResponse<MessageSendHistoryResponse>> getHistories(
            @Parameter(description="페이징 값") PageRequest page,
            @Parameter(description="검색 키워드") MessageSendHistorySearchRequest search,
            @Parameter(description="정렬 기준") SortRequest sort,
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



    /**
     * 발송 이력 상세 조회
     */
    @GetMapping("/{sendCode}")
    @Operation(summary = "메시지 전송 기록 상세 조회", description = "메시지가 전송된 기록을 상세 조회한다.")
    public ApiResponse<MessageSendHistoryDetailResponse> getDetail(
            @Parameter(description="메시지 전송 코드")@PathVariable Long sendCode
    ) {
        return ApiResponse.success(service.getDetail(sendCode));
    }
}
