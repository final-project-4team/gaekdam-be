package com.gaekdam.gaekdambe.analytics_service.report.dashboard.query.application.controller;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.application.service.ReportExportCommandService;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.domain.entity.ReportExport;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.query.dto.request.ReportExportRequest;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.query.dto.response.ReportExportResponse;
import com.gaekdam.gaekdambe.global.config.model.ApiResponse;

@RestController
@RequestMapping("/api/v1/report/dashboard/exports")
public class ReportExportController {
    private final ReportExportCommandService commandService;

    public ReportExportController(ReportExportCommandService commandService) {
        this.commandService = commandService;
    }

    @PostMapping
    public ResponseEntity<Void> create(@RequestBody ReportExportRequest req) {
        ReportExport e = new ReportExport();
        e.setLayoutId(req.getLayoutId());
        e.setLayoutTemplateId(req.getLayoutTemplateId());
        e.setLayoutWidgetId(req.getLayoutWidgetId());
        e.setRequestedBy(req.getRequestedBy());
        e.setExportFormat(req.getExportFormat());
        e.setExportScope(req.getExportScope());
        e.setParamsJson(req.getParamsJson());
        commandService.create(e);
        return ResponseEntity.created(URI.create("/api/v1/report/dashboard/exports/" + (e.getExportId() == null ? "1" : e.getExportId()))).build();
    }

    @GetMapping("/{id}")
    public ApiResponse<ReportExportResponse> get(@PathVariable String id) {
        ReportExport e = commandService.findById(id);
        if (e == null) return ApiResponse.success(null);
        ReportExportResponse r = new ReportExportResponse();
        r.setExportId(e.getExportId());
        r.setLayoutId(e.getLayoutId());
        r.setLayoutTemplateId(e.getLayoutTemplateId());
        r.setLayoutWidgetId(e.getLayoutWidgetId());
        r.setRequestedBy(e.getRequestedBy());
        r.setExportFormat(e.getExportFormat());
        r.setExportScope(e.getExportScope());
        r.setParamsJson(e.getParamsJson());
        r.setSuccessYn(e.getSuccessYn());
        r.setFailureReason(e.getFailureReason());
        r.setCreatedAt(e.getCreatedAt());
        return ApiResponse.success(r);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Void> updateStatus(@PathVariable String id, @RequestParam boolean success, @RequestParam(required = false) String failureReason) {
        commandService.updateStatus(id, success, failureReason);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        commandService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
