package com.gaekdam.gaekdambe.analytics_service.report.dashboard.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.application.dto.ReportLayoutCreateDto;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.application.dto.ReportLayoutResponseDto;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.application.dto.ReportLayoutUpdateDto;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.application.service.ReportLayoutCommandService;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.query.service.ReportLayoutQueryService;
import com.gaekdam.gaekdambe.global.config.model.ApiResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/report/dashboard/layout")
public class ReportLayoutController {

  private final ReportLayoutCommandService commandService;
  private final ReportLayoutQueryService queryService;

  public ReportLayoutController(ReportLayoutCommandService commandService,
                                ReportLayoutQueryService queryService) {
    this.commandService = commandService;
    this.queryService = queryService;
  }

  @PostMapping
  public ResponseEntity<ApiResponse<Long>> create(@RequestBody @Valid ReportLayoutCreateDto dto) {
    Long id = commandService.create(dto);
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(id));
  }

  @PutMapping("/{id}")
  public ResponseEntity<ApiResponse<Void>> update(@PathVariable Long id,
                                                  @RequestBody @Valid ReportLayoutUpdateDto dto) {
    dto.setLayoutId(id);
    commandService.update(dto);
    return ResponseEntity.ok(ApiResponse.success(null));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
    commandService.delete(id);
    return ResponseEntity.ok(ApiResponse.success(null));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<ReportLayoutResponseDto>> get(@PathVariable Long id) {
    return ResponseEntity.ok(ApiResponse.success(queryService.getById(id)));
  }

}