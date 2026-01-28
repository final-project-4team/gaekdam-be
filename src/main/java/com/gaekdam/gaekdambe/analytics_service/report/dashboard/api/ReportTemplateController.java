package com.gaekdam.gaekdambe.analytics_service.report.dashboard.api;

import com.gaekdam.gaekdambe.iam_service.log.command.application.aop.annotation.AuditLog;
import com.gaekdam.gaekdambe.iam_service.permission_type.command.domain.seeds.PermissionTypeKey;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gaekdam.gaekdambe.analytics_service.report.dashboard.query.dto.ReportTemplateWidgetResponseDto;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.query.service.ReportTemplateWidgetQueryService;
import com.gaekdam.gaekdambe.global.config.model.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/report/templates")
public class ReportTemplateController {

    private final ReportTemplateWidgetQueryService queryService;

    // 템플릿 상세(=위젯 현황)
    @GetMapping("/{templateId}/widgets")
    @AuditLog(details = "", type = PermissionTypeKey.REPORT_LAYOUT_TEMPLATE_READ)
    public ResponseEntity<ApiResponse<List<ReportTemplateWidgetResponseDto>>> listWidgets(
        @PathVariable Long templateId
    ) {
        return ResponseEntity.ok(ApiResponse.success(queryService.listByTemplateId(templateId)));
    }
}
