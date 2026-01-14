package com.gaekdam.gaekdambe.communication_service.messaging.command.application.controller;

import com.gaekdam.gaekdambe.communication_service.messaging.command.application.dto.request.MessageRuleCreateRequest;
import com.gaekdam.gaekdambe.communication_service.messaging.command.application.dto.request.MessageRuleUpdateRequest;
import com.gaekdam.gaekdambe.communication_service.messaging.command.application.service.MessageRuleCommandService;
import com.gaekdam.gaekdambe.global.config.model.ApiResponse;
import com.gaekdam.gaekdambe.global.config.security.CustomUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/message-rules")
public class MessageRuleCommandController {

    private final MessageRuleCommandService service;

    @PostMapping
    public ApiResponse<Long> create(@RequestBody MessageRuleCreateRequest req) {


        return ApiResponse.success(service.createRule(req));
    }

    @PutMapping("/{ruleCode}")
    public ApiResponse<Void> update(
            @PathVariable Long ruleCode,
            @RequestBody MessageRuleUpdateRequest req
    ) {

        service.update(ruleCode, req);

        return ApiResponse.success();
    }

    @PatchMapping("/{ruleCode}/disable")
    public ApiResponse<Void> disable(@PathVariable Long ruleCode) {

        service.disableRule(ruleCode);
        return ApiResponse.success();
    }
}
