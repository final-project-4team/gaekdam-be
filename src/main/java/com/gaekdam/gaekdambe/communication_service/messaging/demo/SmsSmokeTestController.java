package com.gaekdam.gaekdambe.communication_service.messaging.demo;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/test/sms")
public class SmsSmokeTestController {

    private final CoolSmsSmokeTestService service;

    @PostMapping
    public void send(@RequestParam String to) {
        service.send(to);
    }
}

