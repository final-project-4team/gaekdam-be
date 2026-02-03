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

@RestController
@RequestMapping("/api/v1/setting")
@RequiredArgsConstructor
public class ReportKPIExcelController {

    private final ReportKPITargetExcelService excelService;

    @GetMapping("/objective/template")
    public ResponseEntity<ByteArrayResource> downloadTemplate(
        @RequestParam Long hotelGroupCode,
        @RequestParam String periodType,   // "YEAR" or "MONTH"
        @RequestParam String periodValue       // "2024" or "2024-03"
    ) throws IOException {
        byte[] bytes = excelService.generateTemplateExcel(hotelGroupCode, periodType, periodValue);
        ByteArrayResource resource = new ByteArrayResource(bytes);

        String filename = String.format("KPI_Template.xlsx");
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
            .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
            .contentLength(bytes.length)
            .body(resource);
    }

    @PostMapping(value = "/objective/template/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('SETTING_OBJECTIVE_UPDATE')")
    public ApiResponse<ImportResultDto> importTargets(
        @RequestParam Long hotelGroupCode,
        @RequestParam(required = false) String periodType,
        @RequestParam(required = false) String periodValue,
        @RequestParam("file") MultipartFile file
    ) {
        ImportResultDto result = excelService.importFromExcel(hotelGroupCode, periodType, periodValue, file);
        return ApiResponse.success(result);
    }
}
