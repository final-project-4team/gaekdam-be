package com.gaekdam.gaekdambe.communication_service.messaging.command.application.controller;

import com.gaekdam.gaekdambe.communication_service.messaging.command.application.dto.request.MessageRuleUpdateRequest;
import com.gaekdam.gaekdambe.communication_service.messaging.command.application.service.MessageRuleCommandService;
import com.gaekdam.gaekdambe.global.config.model.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name="메시지 규칙")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/message-rules")
public class MessageRuleCommandController {

    private final MessageRuleCommandService service;
//    룰 자체도 우선은 공통으로 강제화 추후 확장가능
//    @PostMapping
//    public ApiResponse<Long> create(@RequestBody MessageRuleCreateRequest req,
//                                    @AuthenticationPrincipal CustomUser customUser) {
//
//        req.setHotelGroupCode(customUser.getHotelGroupCode());
//
//        return ApiResponse.success(service.createRule(req));
//    }

    @PutMapping("/{ruleCode}")
    @Operation(summary = "메시지 규칙 업데이트", description = "메시지 전송 규칙을 업데이트 한다.")
    public ApiResponse<Void> update(
            @Parameter(description = "규칙 코드") @PathVariable Long ruleCode,
            @RequestBody MessageRuleUpdateRequest req
    ) {

        service.update(ruleCode, req);

        return ApiResponse.success();
    }

    @PatchMapping("/{ruleCode}/disable")
    @Operation(summary = "메시지 규정 비활성화", description = "특정 메시지 전송 규칙을 비활성화 한다.")
    public ApiResponse<Void> disable(@PathVariable Long ruleCode) {

        service.disableRule(ruleCode);
        return ApiResponse.success();
    }
}
