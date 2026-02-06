package com.gaekdam.gaekdambe.analytics_service.report.dataset.api;

import java.io.IOException;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.gaekdam.gaekdambe.analytics_service.report.dataset.command.application.dto.ImportResultDto;
import com.gaekdam.gaekdambe.analytics_service.report.dataset.command.application.service.ReportKPITargetExcelService;
import com.gaekdam.gaekdambe.global.config.model.ApiResponse;

import lombok.RequiredArgsConstructor;

import com.gaekdam.gaekdambe.global.config.swagger.SpecResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "KPI 엑셀 업로드")
@RestController
@RequestMapping("/api/v1/setting")
@RequiredArgsConstructor
public class ReportKPIExcelController {

    private final ReportKPITargetExcelService excelService;

    @GetMapping("/objective/template")
    @Operation(summary = "KPI 업로드 템플릿 다운로드", description = "KPI 목표 설정을 위한 엑셀 템플릿을 다운로드합니다.")
    public ResponseEntity<ByteArrayResource> downloadTemplate(
            @Parameter(description = "호텔 그룹 코드") @RequestParam Long hotelGroupCode,
            @Parameter(description = "기간 유형") @RequestParam String periodType, // "YEAR" or "MONTH"
            @Parameter(description = "기간 값 ") @RequestParam String periodValue // "2024" or "2024-03"
    ) throws IOException {
        byte[] bytes = excelService.generateTemplateExcel(hotelGroupCode, periodType, periodValue);
        ByteArrayResource resource = new ByteArrayResource(bytes);

        String filename = String.format("KPI_Template.xlsx");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(
                        MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .contentLength(bytes.length)
                .body(resource);
    }

    @PostMapping(value = "/objective/template/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('SETTING_OBJECTIVE_UPDATE')")
    @Operation(summary = "KPI 목표 엑셀 업로드", description = "엑셀 파일을 통해 KPI 목표를 일괄 등록합니다.")
    @SpecResponse(description = "업로드 성공")
    public ApiResponse<ImportResultDto> importTargets(
            @Parameter(description = "호텔 그룹 코드") @RequestParam Long hotelGroupCode,
            @Parameter(description = "기간 유형") @RequestParam(required = false) String periodType,
            @Parameter(description = "기간 값") @RequestParam(required = false) String periodValue,
            @Parameter(description = "업로드할 엑셀 파일") @RequestParam("file") MultipartFile file) {
        ImportResultDto result = excelService.importFromExcel(hotelGroupCode, periodType, periodValue, file);
        return ApiResponse.success(result);
    }
}
