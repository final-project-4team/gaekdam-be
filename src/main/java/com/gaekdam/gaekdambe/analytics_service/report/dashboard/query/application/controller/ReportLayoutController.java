package com.gaekdam.gaekdambe.analytics_service.report.dashboard.query.application.controller;

import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.application.service.ReportLayoutCommandService;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.domain.entity.ReportLayout;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.query.dto.request.ReportLayoutTemplateRequest;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.query.dto.request.ReportLayoutUserStateRequest;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.query.dto.request.ReportLayoutWidgetRequest;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.query.dto.response.LayoutResponse;
import com.gaekdam.gaekdambe.global.config.model.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/report/dashboard/layouts")
public class ReportLayoutController {
    private final ReportLayoutCommandService commandService;

    public ReportLayoutController(ReportLayoutCommandService commandService) {
        this.commandService = commandService;
    }

    @PostMapping
    public ResponseEntity<Void> createLayout(@RequestBody ReportLayoutWidgetRequest req) {
        // simple mapping - assume client sends layoutJson and metadata via request
        ReportLayout layout = new ReportLayout();
        layout.setName(req.getTitle());
        layout.setLayoutJson(req.getFilterJson());
        commandService.create(layout);
        return ResponseEntity.created(URI.create("/api/v1/report/dashboard/layouts/1")).build();
    }

    @GetMapping("/{id}")
    public ApiResponse<LayoutResponse> getLayout(@PathVariable Long id) {
        // ...existing code...
        return ApiResponse.success(new LayoutResponse());
    }

    @GetMapping
    public ApiResponse<List<LayoutResponse>> listLayouts(@RequestParam(required = false) Long employeeCode) {
        // ...existing code...
        return ApiResponse.success(List.of());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateLayout(@PathVariable Long id, @RequestBody ReportLayoutTemplateRequest req) {
        // ...existing code...
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLayout(@PathVariable Long id) {
        commandService.delete(id);
        return ResponseEntity.noContent().build();
    }
}