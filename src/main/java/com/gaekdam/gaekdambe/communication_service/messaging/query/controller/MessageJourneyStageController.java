package com.gaekdam.gaekdambe.communication_service.messaging.query.controller;

import com.gaekdam.gaekdambe.communication_service.messaging.query.dto.response.MessageJourneyStageResponse;
import com.gaekdam.gaekdambe.communication_service.messaging.query.service.MessageJourneyStageQueryService;
import com.gaekdam.gaekdambe.global.config.model.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name="메시지")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/message")
public class MessageJourneyStageController {

    private final MessageJourneyStageQueryService queryService;

    // 메시지 여정 단계 목록 조회

    @GetMapping("/journey-stages")
    @Operation(summary = "메시지 여정 단계 리스트 조회", description = "메시지 전송에 사용될 여정 단계 리스트를 조회한다. ")
    public ApiResponse<List<MessageJourneyStageResponse>> getStages() {
        return ApiResponse.success(queryService.findAll());
    }
}
