package com.gaekdam.gaekdambe.communication_service.messaging.demo;

import com.gaekdam.gaekdambe.global.config.security.CustomUser;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@ConditionalOnProperty(name = "messaging.sms.enabled", havingValue = "true")
@RequestMapping("/api/v1/demo/sms")
public class DemoSmsController {

    private final DemoSmsService demoSmsService;

    @PostMapping("/send")
    public ResponseEntity<Void> send(
            @RequestBody DemoSmsRequest request,
            @AuthenticationPrincipal CustomUser loginUser
    ) {
        demoSmsService.sendOne(request, loginUser.getHotelGroupCode());
        return ResponseEntity.ok().build();
    }
}
