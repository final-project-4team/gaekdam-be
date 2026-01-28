package com.gaekdam.gaekdambe.communication_service.messaging.command.application.controller;

import com.gaekdam.gaekdambe.communication_service.messaging.command.application.dto.request.MessageTemplateCreateRequest;
import com.gaekdam.gaekdambe.communication_service.messaging.command.application.dto.request.MessageTemplateUpdateRequest;
import com.gaekdam.gaekdambe.communication_service.messaging.command.application.service.MessageTemplateCommandService;
import com.gaekdam.gaekdambe.global.config.model.ApiResponse;
import com.gaekdam.gaekdambe.global.config.security.CustomUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/message-templates")
public class MessageTemplateCommandController {

    private final MessageTemplateCommandService service;

    @PostMapping
    public ApiResponse<Long> create(
            @RequestBody MessageTemplateCreateRequest req,
            @AuthenticationPrincipal CustomUser customUser
    ) {
        Long hotelGroupCode = customUser.getHotelGroupCode();
        return ApiResponse.success(service.createTemplate(req, hotelGroupCode));
    }

    @PutMapping("/{templateCode}")
    public ApiResponse<Void> update(
            @PathVariable Long templateCode,
            @RequestBody MessageTemplateUpdateRequest req
    ) {
        service.update(templateCode, req);
        return ApiResponse.success();
    }

}
