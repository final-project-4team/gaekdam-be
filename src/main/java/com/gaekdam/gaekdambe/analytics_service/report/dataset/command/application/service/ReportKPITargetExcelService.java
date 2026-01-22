package com.gaekdam.gaekdambe.analytics_service.report.dataset.command.application.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.NumberToTextConverter;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.gaekdam.gaekdambe.analytics_service.report.dataset.command.application.dto.ImportResultDto;
import com.gaekdam.gaekdambe.analytics_service.report.dataset.command.application.dto.RowError;
import com.gaekdam.gaekdambe.analytics_service.report.dataset.command.domain.entity.ReportKPICodeDim;
import com.gaekdam.gaekdambe.analytics_service.report.dataset.command.domain.entity.ReportKPITarget;
import com.gaekdam.gaekdambe.analytics_service.report.dataset.command.domain.entity.ReportKPITargetId;
import com.gaekdam.gaekdambe.analytics_service.report.dataset.command.domain.infrastructure.repository.ReportKPICodeDimRepository;
import com.gaekdam.gaekdambe.analytics_service.report.dataset.command.domain.infrastructure.repository.ReportKPITargetRepository;

@Service
public class ReportKPITargetExcelService {
    
    private final ReportKPICodeDimRepository kpiRepo;
    private final ReportKPITargetRepository targetRepo;

    public ReportKPITargetExcelService(ReportKPICodeDimRepository kpiRepo,
                                        ReportKPITargetRepository targetRepo) {
        this.kpiRepo = kpiRepo;
        this.targetRepo = targetRepo;
    }

    public byte[] generateTemplateExcel(Long hotelGroupCode, String periodType, String period) throws IOException {
        List<ReportKPICodeDim> kpis = kpiRepo.findByIsActiveTrueOrderByKpiCodeAsc();

        try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
        XSSFSheet sheet = wb.createSheet("KPI_Template");
        String[] headers = {"hotelGroupCode","kpiCode","kpiName","unit","periodType","periodValue","targetValue"};
        Row h = sheet.createRow(0);
        for (int i=0;i<headers.length;i++) h.createCell(i).setCellValue(headers[i]);

        int r = 1;
        for (ReportKPICodeDim k : kpis) {
            Row row = sheet.createRow(r++);
            row.createCell(0).setCellValue(hotelGroupCode);
            row.createCell(1).setCellValue(k.getKpiCode());
            row.createCell(2).setCellValue(k.getKpiName());
            row.createCell(3).setCellValue(k.getUnit());
            row.createCell(4).setCellValue(periodType);
            row.createCell(5).setCellValue(period);
            row.createCell(6).setCellValue(""); // targetValue 빈칸
        }

        for (int i=0;i<headers.length;i++) sheet.autoSizeColumn(i);
        wb.write(bos);
        return bos.toByteArray();
        }
    }

    @Transactional
    public ImportResultDto importFromExcel(Long hotelGroupCode, String periodType, String periodValue, MultipartFile file) {
        
        ImportResultDto result = new ImportResultDto();
        List<RowError> errors = new ArrayList<>();
        int created = 0, updated = 0, skipped = 0;

        try (Workbook wb = WorkbookFactory.create(file.getInputStream())) {
        Sheet sheet = wb.getSheetAt(0);
        if (sheet.getPhysicalNumberOfRows() <= 1) {
            result.setCreated(created); result.setUpdated(updated); result.setSkipped(skipped); result.setErrors(errors);
            return result;
        }

        // 헤더 매핑(유연하게 컬럼 인덱스 찾기)
        Map<String,Integer> idx = parseHeader(sheet.getRow(0));
        for (int r = 1; r <= sheet.getLastRowNum(); r++) {
            Row row = sheet.getRow(r);
            if (row == null) continue;

            String kpiCode = getCellString(row, idx.get("kpiCode"));
            String tvRaw = getCellString(row, idx.get("targetValue"));

            if (kpiCode == null || kpiCode.isBlank()) { skipped++; continue; }

            Optional<ReportKPICodeDim> kpiOpt = kpiRepo.findByKpiCode(kpiCode);
            if (kpiOpt.isEmpty()) {
            errors.add(new RowError(r+1, "Unknown kpiCode: " + kpiCode));
            continue;
            }

            BigDecimal tv = parseNumberOrNull(tvRaw);
            Optional<ReportKPITarget> existing = targetRepo.findById_HotelGroupCodeAndKpiCodeAndPeriodTypeAndPeriodValue(
                hotelGroupCode, kpiCode, periodType, periodValue);

            if (existing.isPresent()) {
            ReportKPITarget t = existing.get();
            t.setTargetValue(tv);
            targetRepo.save(t);
            updated++;
            } else {
            ReportKPITarget t = new ReportKPITarget();
            t.setId(new ReportKPITargetId(generateTargetId(kpiCode, periodValue), hotelGroupCode));
            t.setKpiCode(kpiCode);
            t.setPeriodType(periodType);
            t.setPeriodValue(periodValue);
            // allow null targetValue per upload; entity column may be nullable if desired
            t.setTargetValue(tv == null ? BigDecimal.ZERO : tv);
            targetRepo.save(t);
            created++;
            }
        }
        } catch (Exception ex) {
        // 심각 오류면 롤백과 함께 예외 던질 수 있음
        throw new RuntimeException("Import failed: " + ex.getMessage(), ex);
        }

        result.setCreated(created);
        result.setUpdated(updated);
        result.setSkipped(skipped);
        result.setErrors(errors);
        return result;
    }

    // --- 유틸들 (간단 구현) ---
    private Map<String,Integer> parseHeader(Row headerRow) {
        Map<String,Integer> idx = new HashMap<>();
        for (Cell c : headerRow) {
        String key = c.getStringCellValue().trim().toLowerCase();
        idx.put(key, c.getColumnIndex());
        }
        return idx;
    }

    private String getCellString(Row row, Integer colIdx) {
        if (colIdx == null) return null;
        Cell c = row.getCell(colIdx);
        if (c == null) return null;
        if (c.getCellType() == CellType.STRING) return c.getStringCellValue().trim();
        if (c.getCellType() == CellType.NUMERIC) return NumberToTextConverter.toText(c.getNumericCellValue());
        return c.toString().trim();
    }

    private BigDecimal parseNumberOrNull(String raw) {
        if (raw == null) return null;
        String clean = raw.replaceAll("[,\\s]", "");
        if (clean.isEmpty()) return null;
        try { return new BigDecimal(clean); } catch (NumberFormatException e) { return null; }
    }

    private String generateTargetId(String kpiCode, String period) {
        return kpiCode + "_" + period;
    }
}
