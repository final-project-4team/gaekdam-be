package com.gaekdam.gaekdambe.analytics_service.report.dashboard.api;

import java.util.List;

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

import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.application.dto.ReportLayoutCreateDto;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.application.dto.ReportLayoutUpdateDto;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.application.service.ReportLayoutCommandService;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.query.dto.ReportLayoutListQueryDto;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.query.dto.ReportLayoutResponseDto;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.query.service.ReportLayoutQueryService;
import com.gaekdam.gaekdambe.global.config.model.ApiResponse;

import jakarta.validation.Valid;


@RestController
@RequestMapping("/api/v1/report/dashboard/layouts")
public class ReportLayoutController {

  private final ReportLayoutCommandService commandService;
  private final ReportLayoutQueryService queryService;

  public ReportLayoutController(ReportLayoutCommandService commandService,
                                ReportLayoutQueryService queryService) {
    this.commandService = commandService;
    this.queryService = queryService;
  }

  // 레이아웃 생성
  @PostMapping
  @PreAuthorize("hasAuthority('REPORT_LAYOUT_CREATE')")
  public ResponseEntity<ApiResponse<Long>> create(@RequestBody @Valid ReportLayoutCreateDto dto) {
    
    Long id = commandService.create(dto);
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(id));

    }

    // 레이아웃 수정(부분수정)
  @PatchMapping("/{id}")
  @PreAuthorize("hasAuthority('REPORT_LAYOUT_UPDATE')")
  public ResponseEntity<ApiResponse<Void>> update(@PathVariable Long id,
                                                  @RequestBody @Valid ReportLayoutUpdateDto dto) {
    dto.setLayoutId(id);
    commandService.update(dto);
    return ResponseEntity.ok(ApiResponse.success(null));
  }

  // 레이아웃 삭제
  @DeleteMapping("/{id}")
  @PreAuthorize("hasAuthority('REPORT_LAYOUT_DELETE')")
  public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
    commandService.delete(id);
    return ResponseEntity.ok(ApiResponse.success(null));
  }

  // 레이아웃 리스트 조회 (사용자별)
  @GetMapping
  @PreAuthorize("hasAuthority('REPORT_LAYOUT_LIST')")
  public ResponseEntity<ApiResponse<List<ReportLayoutResponseDto>>> list(
    @RequestParam Long employeeCode
  ) {
    ReportLayoutListQueryDto q = ReportLayoutListQueryDto.builder()
      .employeeCode(employeeCode)
      .build();

    List<ReportLayoutResponseDto> data = queryService.list(q);
    return ResponseEntity.ok(ApiResponse.success(data));
  }
  /*
  // 레이아웃 템플릿 조회
  @GetMapping("/{id}/templates")
  public ResponseEntity<ApiResponse<ReportLayoutTemplateListResponseDto>> getTemplates(@PathVariable("id") Long layoutId) {
    ReportLayoutTemplateListResponseDto data = queryService.getTemplatesByLayoutId(layoutId);
    return ResponseEntity.ok(ApiResponse.success(data));
    }
  */


}