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

import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.application.service.ReportTemplateWidgetCommandService;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.domain.entity.ReportTemplateWidget;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.query.dto.request.ReportTemplateWidgetRequest;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.query.dto.response.ReportTemplateWidgetResponse;
import com.gaekdam.gaekdambe.global.config.model.ApiResponse;

@RestController
@RequestMapping("/api/v1/report/dashboard/template-widgets")
public class ReportTemplateWidgetController {
    private final ReportTemplateWidgetCommandService commandService;

    public ReportTemplateWidgetController(ReportTemplateWidgetCommandService commandService) {
        this.commandService = commandService;
    }

    @PostMapping
    public ResponseEntity<Void> create(@RequestBody ReportTemplateWidgetRequest req) {
        ReportTemplateWidget w = new ReportTemplateWidget();
        w.setTemplateId(req.getTemplateId());
        w.setWidgetType(req.getWidgetType());
        w.setTitle(req.getTitle());
        w.setDatasetType(req.getDatasetType());
        w.setMetricKey(req.getMetricKey());
        w.setDimensionKey(req.getDimensionKey());
        w.setDefaultPeriod(req.getDefaultPeriod());
        w.setDefaultSortOrder(req.getDefaultSortOrder());
        w.setOptionsJson(req.getOptionsJson());
        w.setDefaultFilterJson(req.getDefaultFilterJson());
        commandService.create(w);
        return ResponseEntity.created(URI.create("/api/v1/report/dashboard/template-widgets/" + (w.getTemplateWidgetId() == null ? "1" : w.getTemplateWidgetId()))).build();
    }

    @GetMapping("/{id}")
    public ApiResponse<ReportTemplateWidgetResponse> get(@PathVariable String id) {
        ReportTemplateWidget w = commandService.findById(id);
        if (w == null) return ApiResponse.success(null);
        ReportTemplateWidgetResponse r = new ReportTemplateWidgetResponse();
        r.setTemplateWidgetId(w.getTemplateWidgetId());
        r.setTemplateId(w.getTemplateId());
        r.setWidgetType(w.getWidgetType());
        r.setTitle(w.getTitle());
        r.setDatasetType(w.getDatasetType());
        r.setMetricKey(w.getMetricKey());
        r.setDimensionKey(w.getDimensionKey());
        r.setDefaultPeriod(w.getDefaultPeriod());
        r.setDefaultSortOrder(w.getDefaultSortOrder());
        r.setOptionsJson(w.getOptionsJson());
        r.setDefaultFilterJson(w.getDefaultFilterJson());
        r.setCreatedAt(w.getCreatedAt());
        r.setUpdatedAt(w.getUpdatedAt());
        return ApiResponse.success(r);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable String id, @RequestBody ReportTemplateWidgetRequest req) {
        ReportTemplateWidget w = new ReportTemplateWidget();
        w.setTemplateWidgetId(id);
        w.setTemplateId(req.getTemplateId());
        w.setWidgetType(req.getWidgetType());
        w.setTitle(req.getTitle());
        w.setOptionsJson(req.getOptionsJson());
        w.setDefaultFilterJson(req.getDefaultFilterJson());
        commandService.update(w);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        commandService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
