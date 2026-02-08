package com.gaekdam.gaekdambe.communication_service.messaging.command.application.controller;

import com.gaekdam.gaekdambe.communication_service.messaging.command.application.dto.request.MessageSenderPhoneCreateRequest;
import com.gaekdam.gaekdambe.communication_service.messaging.command.application.service.MessageSenderPhoneService;
import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.entity.MessageSenderPhone;
import com.gaekdam.gaekdambe.global.config.security.CustomUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/message/sender-phones")
public class MessageSenderPhoneController {

    private final MessageSenderPhoneService senderPhoneService;

    /**
     * 발신번호 목록 조회 (호텔그룹 기준)
     * - 토글 UI용
     */
    @GetMapping
    public List<MessageSenderPhone> list(
            @AuthenticationPrincipal CustomUser loginUser
    ) {
        return senderPhoneService.findAll(loginUser.getHotelGroupCode());
    }

    /**
     * 발신번호 활성화 (토글)
     * - 같은 호텔그룹 내 기존 active → false
     * - 선택한 번호 → true
     */
    @PostMapping("/{senderPhoneCode}/activate")
    public void activate(
            @PathVariable Long senderPhoneCode,
            @AuthenticationPrincipal CustomUser loginUser
    ) {
        senderPhoneService.activate(
                loginUser.getHotelGroupCode(),
                senderPhoneCode
        );
    }

    /**
     * 발신번호 등록(저장)
     * 사용자가 메세지를 보낼 발신번호를 저장한다
     * */
    @PostMapping
    public void create(
            @RequestBody MessageSenderPhoneCreateRequest request,
            @AuthenticationPrincipal CustomUser loginUser
    ) {
        senderPhoneService.create(
                loginUser.getHotelGroupCode(),
                request
        );
    }


}
