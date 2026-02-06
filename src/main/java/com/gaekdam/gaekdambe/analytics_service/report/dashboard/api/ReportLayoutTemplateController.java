package com.gaekdam.gaekdambe.analytics_service.report.dashboard.api;

import com.gaekdam.gaekdambe.global.config.swagger.SpecResponse;
import com.gaekdam.gaekdambe.iam_service.log.command.application.aop.annotation.AuditLog;
import com.gaekdam.gaekdambe.iam_service.permission_type.command.domain.seeds.PermissionTypeKey;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

@Tag(name = "리포트 레이아웃 템플릿")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/report/dashboard/layouts/{layoutId}/templates")
public class ReportLayoutTemplateController {

    private final ReportLayoutTemplateCommandService commandService;
    private final ReportLayoutQueryService queryService; // MyBatis 조회 재사용 가능

    @GetMapping
    @PreAuthorize("hasAuthority('REPORT_LAYOUT_TEMPLATE_READ')")
    @AuditLog(details = "", type = PermissionTypeKey.REPORT_LAYOUT_TEMPLATE_READ)
    @Operation(summary = "리포트 레이아웃 템플릿 조회", description = "특정 레이아웃의 템플릿 을 조회합니다.")
    @SpecResponse( description = "조회 성공")
    public ResponseEntity<ApiResponse<ReportLayoutTemplateListResponseDto>> list(
            @Parameter(description = "레이아웃 ID") @PathVariable Long layoutId) {

        return ResponseEntity.ok(ApiResponse.success(queryService.getTemplatesByLayoutId(layoutId)));
    }

    // 특정 레이아웃에 템플릿 추가 하기
    @PostMapping
    @PreAuthorize("hasAuthority('REPORT_LAYOUT_TEMPLATE_CREATE')")
    @Operation(summary = "리포트 레이아웃 템플릿 추가", description = "특정 레이아웃에 새로운 템플릿을 추가합니다.")
    @SpecResponse(responseCode = "201", description = "추가 성공")
    public ResponseEntity<ApiResponse<Long>> add(
            @Parameter(description = "레이아웃 ID") @PathVariable Long layoutId,
            @Parameter(description = "직원 코드") @RequestParam Long employeeCode, // 임시(나중에
                                                                                                                               // 인증에서
                                                                                                                               // 꺼내기)
            @RequestBody @Valid ReportLayoutTemplateCreateDto dto) {

        Long id = commandService.addTemplate(layoutId, employeeCode, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(id));
    }

    // 특정 레이아웃에 템플릿 수정 하기
    @PatchMapping("/{layoutTemplateId}")
    @PreAuthorize("hasAuthority('REPORT_LAYOUT_TEMPLATE_UPDATE')")
    @Operation(summary = "리포트 레이아웃 템플릿 수정", description = "특정 레이아웃의 템플릿을 수정합니다.")
    @SpecResponse( description = "수정 성공")
    public ResponseEntity<ApiResponse<Void>> update(
            @Parameter(description = "레이아웃 ID") @PathVariable Long layoutId,
            @Parameter(description = "템플릿 ID") @PathVariable Long layoutTemplateId,
            @RequestBody ReportLayoutTemplateUpdateDto dto) {

        commandService.update(layoutId, layoutTemplateId, dto);
        return ResponseEntity.ok(ApiResponse.success());
    }

    // 특정 레이아웃에 템플릿 삭제 하기
    @DeleteMapping("/{templateId}")
    @PreAuthorize("hasAuthority('REPORT_LAYOUT_TEMPLATE_DELETE')")
    @Operation(summary = "리포트 레이아웃 템플릿 삭제", description = "특정 레이아웃의 템플릿을 삭제합니다.")
    @SpecResponse( description = "삭제 성공")
    public ResponseEntity<ApiResponse<Void>> delete(
            @Parameter(description = "레이아웃 ID") @PathVariable Long layoutId,
            @Parameter(description = "템플릿 ID") @PathVariable Long templateId) {

        commandService.delete(layoutId, templateId);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
