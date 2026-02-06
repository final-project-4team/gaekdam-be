package com.gaekdam.gaekdambe.communication_service.messaging.command.application.controller;


import com.gaekdam.gaekdambe.communication_service.messaging.command.application.dto.request.MessageTemplateUpdateRequest;
import com.gaekdam.gaekdambe.communication_service.messaging.command.application.service.MessageTemplateCommandService;
import com.gaekdam.gaekdambe.global.config.model.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;



@Tag(name="메시지 템플릿")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/message-templates")
public class MessageTemplateCommandController {

    private final MessageTemplateCommandService service;

//    @PostMapping
//    public ApiResponse<Long> create(
//            @RequestBody MessageTemplateCreateRequest req,
//            @AuthenticationPrincipal CustomUser customUser
//    ) {
//        Long hotelGroupCode = customUser.getHotelGroupCode();
//        return ApiResponse.success(service.createTemplate(req, hotelGroupCode));
//    }

    @PutMapping("/{templateCode}")
    @Operation(summary = "메시지 템플릿 업데이트", description = "메시지 전송시에 사용 할 템플릿을 수정한다.")
    public ApiResponse<Void> update(
            @Parameter(description = "메시지 템플릿 코드") @PathVariable Long templateCode,
            @Parameter(description = "메시지 템플릿 수정 내용")@RequestBody MessageTemplateUpdateRequest req
    ) {
        service.update(templateCode, req);
        return ApiResponse.success();
    }

}
