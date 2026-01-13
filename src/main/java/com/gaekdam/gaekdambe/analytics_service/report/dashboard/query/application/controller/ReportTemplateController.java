package com.gaekdam.gaekdambe.analytics_service.report.dashboard.query.application.controller;

import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.domain.entity.ReportTemplate;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.mapper.ReportTemplateMapper;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.query.dto.request.ReportTemplateRequest;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.query.dto.response.ReportTemplateResponse;
import com.gaekdam.gaekdambe.global.config.model.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/report/dashboard/templates")
public class ReportTemplateController {
    private final ReportTemplateMapper mapper;

    public ReportTemplateController(ReportTemplateMapper mapper) {
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<Void> createTemplate(@RequestBody ReportTemplateRequest req) {
        ReportTemplate t = new ReportTemplate();
        t.setEmployeeCode(req.getEmployeeCode());
        t.setTemplateName(req.getTemplateName());
        t.setTemplateDesc(req.getTemplateDesc());
        t.setIsActive(req.getIsActive());
        t.setVersion(req.getVersion());
        mapper.insert(t);
        return ResponseEntity.created(URI.create("/api/v1/report/dashboard/templates/" + (t.getTemplateId() == null ? 1 : t.getTemplateId()))).build();
    }

    @GetMapping("/{id}")
    public ApiResponse<ReportTemplateResponse> getTemplate(@PathVariable Long id) {
        ReportTemplate t = mapper.findById(id);
        if (t == null) return ApiResponse.success(null);
        ReportTemplateResponse r = new ReportTemplateResponse();
        r.setTemplateId(t.getTemplateId());
        r.setEmployeeCode(t.getEmployeeCode());
        r.setTemplateName(t.getTemplateName());
        r.setTemplateDesc(t.getTemplateDesc());
        r.setIsActive(t.getIsActive());
        r.setVersion(t.getVersion());
        r.setCreatedAt(t.getCreatedAt());
        r.setUpdatedAt(t.getUpdatedAt());
        return ApiResponse.success(r);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateTemplate(@PathVariable Long id, @RequestBody ReportTemplateRequest req) {
        ReportTemplate t = new ReportTemplate();
        t.setTemplateId(id);
        t.setTemplateName(req.getTemplateName());
        t.setTemplateDesc(req.getTemplateDesc());
        t.setIsActive(req.getIsActive());
        t.setVersion(req.getVersion());
        mapper.update(t);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTemplate(@PathVariable Long id) {
        mapper.delete(id);
        return ResponseEntity.noContent().build();
    }
}
