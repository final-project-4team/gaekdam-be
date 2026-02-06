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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name="메시지 템플릿")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/message-templates")
public class MessageTemplateQueryController {

    private final MessageTemplateQueryService service;

    /** 기존: 리스트/검색/페이징용 */
    @GetMapping
    @Operation(summary = "메시지 템플릿 리스트 조회", description = "메시지 템플릿을 리스트로 조회한다")
    public ApiResponse<PageResponse<MessageTemplateResponse>> getTemplates(
            @Parameter(description="페이징 값") PageRequest page,
            @Parameter(description="검색 키워드") MessageTemplateSearch search,
            @Parameter(description="정렬 조건") SortRequest sort,
            @AuthenticationPrincipal CustomUser user
    ) {
        search.setPropertyCode(user.getPropertyCode());
        return ApiResponse.success(service.getTemplates(page, search, sort));
    }


    @GetMapping("/{templateCode}")
    @Operation(summary = "메시지 템플릿 상세 조회", description = "메시지 템플릿을 상세 조회한다")
    public ApiResponse<MessageTemplateDetailResponse> getTemplate(
            @Parameter(description = "메시지 템플릿 코드" )@PathVariable Long templateCode
    ) {
        return ApiResponse.success(
                service.getTemplate(templateCode)
        );
    }


    /** 설정 화면 전용 (여정 기준) */
    @GetMapping("/setting")
    @Operation(summary = "메시지 설정화면(여정 기준) ", description = "메시지 설정화면에 보여질 메시지 여정 기준을 출력한다.")
    public ApiResponse<List<MessageTemplateSettingResponse>> getSettingTemplates(
            @AuthenticationPrincipal CustomUser user
    ) {
        return ApiResponse.success(
                service.getSettingTemplates(user.getPropertyCode())
        );
    }
}
