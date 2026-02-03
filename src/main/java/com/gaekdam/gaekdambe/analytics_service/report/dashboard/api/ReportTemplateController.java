package com.gaekdam.gaekdambe.analytics_service.report.dashboard.api;

import com.gaekdam.gaekdambe.global.config.swagger.SpecResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gaekdam.gaekdambe.analytics_service.report.dashboard.query.dto.ReportTemplateWidgetResponseDto;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.query.service.ReportTemplateWidgetQueryService;
import com.gaekdam.gaekdambe.global.config.model.ApiResponse;
import com.gaekdam.gaekdambe.iam_service.log.command.application.aop.annotation.AuditLog;
import com.gaekdam.gaekdambe.iam_service.permission_type.command.domain.seeds.PermissionTypeKey;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Tag(name = "리포트 템플릿")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/report/templates")
public class ReportTemplateController {

    private static final Logger logger = LoggerFactory.getLogger(ReportTemplateController.class);

    private final ReportTemplateWidgetQueryService queryService;

    // 템플릿 상세(=위젯 현황)
    @GetMapping("/{templateId}/widgets")
    @PreAuthorize("hasAuthority('REPORT_LAYOUT_TEMPLATE_READ')")
    @AuditLog(details = "", type = PermissionTypeKey.REPORT_LAYOUT_TEMPLATE_READ)
    @Operation(summary = "리포트 템플릿 조회 ", description = "리포트 템플릿을 조회합니다.")
    @SpecResponse( description = "수정 성공")
    public ResponseEntity<ApiResponse<List<ReportTemplateWidgetResponseDto>>> listWidgets(
        @Parameter(description = "템플릿 ID")@PathVariable Long templateId,
        @Parameter(description = "period")@RequestParam(required = false) String period,
        HttpServletRequest request
    ) {
        logger.debug("[Controller] listWidgets called. templateId={}, periodParam={}, rawQuery={}",
                     templateId, period, request.getQueryString());
        return ResponseEntity.ok(ApiResponse.success(queryService.listByTemplateId(templateId, period)));
    }
}
