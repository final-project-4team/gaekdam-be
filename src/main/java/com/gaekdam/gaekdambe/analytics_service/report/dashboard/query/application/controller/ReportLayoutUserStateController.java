package com.gaekdam.gaekdambe.analytics_service.report.dashboard.query.application.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.application.service.ReportLayoutUserStateCommandService;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.domain.entity.ReportLayoutUserState;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.query.dto.request.ReportLayoutUserStateRequest;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.query.dto.response.ReportLayoutUserStateResponse;
import com.gaekdam.gaekdambe.global.config.model.ApiResponse;

@RestController
@RequestMapping("/api/v1/report/dashboard/layout-user-states")
public class ReportLayoutUserStateController {
    private final ReportLayoutUserStateCommandService commandService;

    public ReportLayoutUserStateController(ReportLayoutUserStateCommandService commandService) {
        this.commandService = commandService;
    }

    @PostMapping
    public ResponseEntity<Void> upsert(@RequestBody ReportLayoutUserStateRequest req) {
        ReportLayoutUserState s = new ReportLayoutUserState();
        s.setLayoutId(req.getLayoutId());
        s.setEmployeeCode(req.getEmployeeCode());
        s.setStateJson(req.getStateJson());
        commandService.upsert(s);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{layoutId}/{employeeCode}")
    public ApiResponse<ReportLayoutUserStateResponse> get(@PathVariable Long layoutId, @PathVariable Long employeeCode) {
        // Query path not implemented in command service; return empty for now.
        return ApiResponse.success(null);
    }
}
