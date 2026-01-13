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

import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.application.service.ReportLayoutWidgetCommandService;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.domain.entity.ReportLayoutWidget;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.query.dto.request.ReportLayoutWidgetRequest;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.query.dto.response.ReportLayoutWidgetResponse;
import com.gaekdam.gaekdambe.global.config.model.ApiResponse;

@RestController
@RequestMapping("/api/v1/report/dashboard/layout-widgets")
public class ReportLayoutWidgetController {
    private final ReportLayoutWidgetCommandService commandService;

    public ReportLayoutWidgetController(ReportLayoutWidgetCommandService commandService) {
        this.commandService = commandService;
    }

    @PostMapping
    public ResponseEntity<Void> create(@RequestBody ReportLayoutWidgetRequest req) {
        ReportLayoutWidget w = new ReportLayoutWidget();
        w.setLayoutId(req.getLayoutId());
        w.setLayoutTemplateId(req.getLayoutTemplateId());
        w.setTemplateWidgetId(req.getTemplateWidgetId());
        w.setWidgetType(req.getWidgetType());
        w.setTitle(req.getTitle());
        w.setX(req.getX());
        w.setY(req.getY());
        w.setW(req.getW());
        w.setH(req.getH());
        w.setOptionsJson(req.getOptionsJson());
        w.setFilterJson(req.getFilterJson());
        commandService.create(w);
        return ResponseEntity.created(URI.create("/api/v1/report/dashboard/layout-widgets/" + (w.getLayoutWidgetId() == null ? 1 : w.getLayoutWidgetId()))).build();
    }

    @GetMapping("/{id}")
    public ApiResponse<ReportLayoutWidgetResponse> get(@PathVariable Long id) {
        ReportLayoutWidget w = commandService.findById(id);
        if (w == null) return ApiResponse.success(null);
        ReportLayoutWidgetResponse r = new ReportLayoutWidgetResponse();
        r.setLayoutWidgetId(w.getLayoutWidgetId());
        r.setLayoutId(w.getLayoutId());
        r.setLayoutTemplateId(w.getLayoutTemplateId());
        r.setTemplateWidgetId(w.getTemplateWidgetId());
        r.setWidgetType(w.getWidgetType());
        r.setTitle(w.getTitle());
        r.setX(w.getX());
        r.setY(w.getY());
        r.setW(w.getW());
        r.setH(w.getH());
        r.setOptionsJson(w.getOptionsJson());
        r.setFilterJson(w.getFilterJson());
        r.setCreatedAt(w.getCreatedAt());
        r.setUpdatedAt(w.getUpdatedAt());
        return ApiResponse.success(r);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable Long id, @RequestBody ReportLayoutWidgetRequest req) {
        ReportLayoutWidget w = new ReportLayoutWidget();
        w.setLayoutWidgetId(id);
        w.setTitle(req.getTitle());
        w.setOptionsJson(req.getOptionsJson());
        w.setFilterJson(req.getFilterJson());
        commandService.update(w);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        commandService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
