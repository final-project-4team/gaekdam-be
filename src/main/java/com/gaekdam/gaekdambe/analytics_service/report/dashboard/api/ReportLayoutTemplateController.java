package com.gaekdam.gaekdambe.analytics_service.report.dashboard.api;

import com.gaekdam.gaekdambe.iam_service.log.command.application.aop.annotation.AuditLog;
import com.gaekdam.gaekdambe.iam_service.permission_type.command.domain.seeds.PermissionTypeKey;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.application.dto.ReportLayoutTemplateCreateDto;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.application.dto.ReportLayoutTemplateUpdateDto;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.application.service.ReportLayoutTemplateCommandService;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.query.dto.ReportLayoutTemplateListResponseDto;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.query.service.ReportLayoutQueryService;
import com.gaekdam.gaekdambe.global.config.model.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/report/dashboard/layouts/{layoutId}/templates")
public class ReportLayoutTemplateController {
    
    private final ReportLayoutTemplateCommandService commandService;
    private final ReportLayoutQueryService queryService; // MyBatis 조회 재사용 가능

    @GetMapping
    @AuditLog(details = "", type = PermissionTypeKey.REPORT_LAYOUT_TEMPLATE_READ)
    public ResponseEntity<ApiResponse<ReportLayoutTemplateListResponseDto>> list(@PathVariable Long layoutId) {
        // queryService.getTemplatesByLayoutId(layoutId) 이런 식으로 붙이면 됨
        return ResponseEntity.ok(ApiResponse.success(queryService.getTemplatesByLayoutId(layoutId)));
    }

    // 특정 레이아웃에 템플릿 추가 하기
    @PostMapping
    public ResponseEntity<ApiResponse<Long>> add(
            @PathVariable Long layoutId,
            @RequestParam Long employeeCode, // 임시(나중에 인증에서 꺼내기)
            @RequestBody @Valid ReportLayoutTemplateCreateDto dto) {

        Long id = commandService.addTemplate(layoutId, employeeCode, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(id));
    }

    // 특정 레이아웃에 템플릿 수정 하기
    @PatchMapping("/{layoutTemplateId}")
    public ResponseEntity<ApiResponse<Void>> update(
            @PathVariable Long layoutId,
            @PathVariable Long layoutTemplateId,
            @RequestBody ReportLayoutTemplateUpdateDto dto) {

        commandService.update(layoutId, layoutTemplateId, dto);
        return ResponseEntity.ok(ApiResponse.success());
    }

    // 특정 레이아웃에 템플릿 삭제 하기
    @DeleteMapping("/{templateId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long layoutId,
            @PathVariable Long templateId) {

        commandService.delete(layoutId, templateId);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
