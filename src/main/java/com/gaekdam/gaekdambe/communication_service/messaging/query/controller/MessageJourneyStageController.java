package com.gaekdam.gaekdambe.communication_service.messaging.query.controller;

import com.gaekdam.gaekdambe.communication_service.messaging.query.dto.response.MessageJourneyStageResponse;
import com.gaekdam.gaekdambe.communication_service.messaging.query.service.MessageJourneyStageQueryService;
import com.gaekdam.gaekdambe.global.config.model.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/message")
public class MessageJourneyStageController {

    private final MessageJourneyStageQueryService queryService;

    /**
     * 메시지 여정 단계 목록 조회
     */
    @GetMapping("/journey-stages")
    public ApiResponse<List<MessageJourneyStageResponse>> getStages() {
        return ApiResponse.success(queryService.findAll());
    }
}
