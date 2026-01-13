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
import org.springframework.web.bind.annotation.RestController;

import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.application.service.ReportLayoutTemplateCommandService;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.domain.entity.ReportLayoutTemplate;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.query.dto.request.ReportLayoutTemplateRequest;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.query.dto.response.ReportLayoutTemplateResponse;
import com.gaekdam.gaekdambe.global.config.model.ApiResponse;

@RestController
@RequestMapping("/api/v1/report/dashboard/layout-templates")
public class ReportLayoutTemplateController {
    private final ReportLayoutTemplateCommandService commandService;

    public ReportLayoutTemplateController(ReportLayoutTemplateCommandService commandService) {
        this.commandService = commandService;
    }

    @PostMapping
    public ResponseEntity<Void> create(@RequestBody ReportLayoutTemplateRequest req) {
        ReportLayoutTemplate t = new ReportLayoutTemplate();
        t.setLayoutId(req.getLayoutId());
        t.setTemplateId(req.getTemplateId());
        t.setCreatedBy(req.getCreatedBy());
        t.setDisplayName(req.getDisplayName());
        t.setSortOrder(req.getSortOrder());
        t.setIsActive(req.getIsActive());
        commandService.create(t);
        return ResponseEntity.created(URI.create("/api/v1/report/dashboard/layout-templates/" + (t.getLayoutTemplateId() == null ? "1" : t.getLayoutTemplateId()))).build();
    }

    @GetMapping("/{id}")
    public ApiResponse<ReportLayoutTemplateResponse> get(@PathVariable String id) {
        ReportLayoutTemplate t = commandService.findById(id);
        if (t == null) return ApiResponse.success(null);
        ReportLayoutTemplateResponse r = new ReportLayoutTemplateResponse();
        r.setLayoutTemplateId(t.getLayoutTemplateId());
        r.setLayoutId(t.getLayoutId());
        r.setTemplateId(t.getTemplateId());
        r.setCreatedBy(t.getCreatedBy());
        r.setDisplayName(t.getDisplayName());
        r.setSortOrder(t.getSortOrder());
        r.setIsActive(t.getIsActive());
        r.setCreatedAt(t.getCreatedAt());
        return ApiResponse.success(r);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable String id, @RequestBody ReportLayoutTemplateRequest req) {
        ReportLayoutTemplate t = new ReportLayoutTemplate();
        t.setLayoutTemplateId(id);
        t.setDisplayName(req.getDisplayName());
        t.setSortOrder(req.getSortOrder());
        t.setIsActive(req.getIsActive());
        commandService.update(t);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        commandService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
