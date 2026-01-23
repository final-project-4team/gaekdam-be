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
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
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
    private final ReportKPITargetSaveService saveService;

    public ReportKPITargetExcelService(ReportKPICodeDimRepository kpiRepo,
                                        ReportKPITargetRepository targetRepo,
                                        ReportKPITargetSaveService saveService) {
        this.kpiRepo = kpiRepo;
        this.targetRepo = targetRepo;
        this.saveService = saveService;
    }

    public byte[] generateTemplateExcel(Long hotelGroupCode, String periodType, String period) throws IOException {
        // period 파라미터를 연도로 해석: 만약 user가 '2024' 넘겼다면 그 해만, 아니면 자동으로 모든 연도 생성
        List<String> years;
        if (period != null && period.matches("^\\d{4}$")) {
            years = List.of(period);
        } else {
            years = targetRepo.findDistinctYearsByHotelGroupCode(hotelGroupCode); // 추가 메서드
        }

        List<ReportKPICodeDim> kpis = kpiRepo.findByIsActiveTrueOrderByKpiCodeAsc();

        try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            for (String year : years) {
                XSSFSheet sheet = wb.createSheet(year);
                // 헤더: hotelGroupCode,kpiCode,kpiName,unit,Annual,Jan,...,Dec
                Row header = sheet.createRow(0);
                String[] cols = new String[4 + 1 + 12]; // 4 기본 + Annual + 12 months
                cols[0] = "hotelGroupCode"; cols[1] = "kpiCode"; cols[2] = "kpiName"; cols[3] = "unit";
                cols[4] = "Annual";
                for (int m=1;m<=12;m++) cols[4+m] = String.format("%02d", m);
                for (int i=0;i<cols.length;i++) header.createCell(i).setCellValue(cols[i]);

                // TEMPLATE MODE: don't fetch or populate existing target values. Leave annual/month cells empty so user
                // can fill them when uploading. Previously we fetched targets and populated values here; remove that.

                int r = 1;
                for (ReportKPICodeDim kpi : kpis) {
                    Row row = sheet.createRow(r++);
                    row.createCell(0).setCellValue(hotelGroupCode);
                    row.createCell(1).setCellValue(kpi.getKpiCode());
                    row.createCell(2).setCellValue(kpi.getKpiName());
                    row.createCell(3).setCellValue(kpi.getUnit());
                    // Leave Annual and monthly value cells blank intentionally for template download
                }

                for (int i=0;i<cols.length;i++) sheet.autoSizeColumn(i);
            }

            wb.write(bos);
            return bos.toByteArray();
        }
    }

    public ImportResultDto importFromExcel(Long hotelGroupCode, String unusedPeriodType, String unusedPeriodValue, MultipartFile file) {
        ImportResultDto result = new ImportResultDto();
        List<RowError> errors = new ArrayList<>();
        int created = 0, updated = 0, skipped = 0;

        try (Workbook wb = WorkbookFactory.create(file.getInputStream())) {
            for (int s = 0; s < wb.getNumberOfSheets(); s++) {
                Sheet sheet = wb.getSheetAt(s);
                if (sheet == null || sheet.getPhysicalNumberOfRows() <= 1) continue;

                String sheetName = sheet.getSheetName().trim();
                String year = null;
                if (sheetName.matches("^\\d{4}$")) year = sheetName;
                // else we could try to derive year from filename/headers — keep strict for now
                if (year == null) {
                    // skip sheet if not a year
                    continue;
                }

                Row headerRow = sheet.getRow(0);
                Map<String,Integer> idx = parseHeader(headerRow); // existing helper

                Integer idxKpi = idx.get("kpicode") != null ? idx.get("kpicode") : idx.get("kpicode"); // tolerant
                for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                    Row row = sheet.getRow(r);
                    if (row == null) continue;
                    try {
                        String kpiCode = getCellString(row, idxKpi);
                        if (kpiCode == null || kpiCode.isBlank()) { skipped++; continue; }

                        Optional<ReportKPICodeDim> kpiOpt = kpiRepo.findByKpiCode(kpiCode);
                        if (kpiOpt.isEmpty()) {
                            errors.add(new RowError(r+1, "Unknown kpiCode: " + kpiCode));
                            continue;
                        }

                        // Annual
                        Integer aIdx = idx.get("annual");
                        if (aIdx != null) {
                            String raw = getCellString(row, aIdx);
                            BigDecimal val = parseNumberOrNull(raw);
                            if (val != null) {
                                String periodType = "YEAR";
                                String periodValue = year;
                                Optional<ReportKPITarget> existing = targetRepo.findById_HotelGroupCodeAndKpiCodeAndPeriodTypeAndPeriodValue(
                                    hotelGroupCode, kpiCode, periodType, periodValue);
                                if (existing.isPresent()) {
                                    ReportKPITarget t = existing.get();
                                    t.setTargetValue(val);
                                    try {
                                        saveService.saveSingleTarget(t);
                                        updated++;
                                    } catch (DataAccessException dae) {
                                        errors.add(new RowError(r+1, "DB error: " + dae.getMessage()));
                                    }
                                } else {
                                    ReportKPITarget t = new ReportKPITarget();
                                    t.setId(new ReportKPITargetId(generateTargetId(kpiCode, periodValue), hotelGroupCode));
                                    t.setKpiCode(kpiCode);
                                    t.setPeriodType(periodType);
                                    t.setPeriodValue(periodValue);
                                    t.setTargetValue(val);
                                    try {
                                        saveService.saveSingleTarget(t);
                                        created++;
                                    } catch (DataAccessException dae) {
                                        errors.add(new RowError(r+1, "DB error: " + dae.getMessage()));
                                    }
                                }
                            }
                        }

                        // Months 01..12 - header keys may be "01","1","jan" etc. try numeric keys
                        for (int m = 1; m <= 12; m++) {
                            String key1 = String.format("%02d", m);
                            String key2 = Integer.toString(m);
                            Integer midx = idx.get(key1);
                            if (midx == null) midx = idx.get(key2);
                            if (midx == null) continue;
                            String raw = getCellString(row, midx);
                            BigDecimal val = parseNumberOrNull(raw);
                            if (val == null) continue;
                            String periodType = "MONTH";
                            String periodValue = String.format("%s-%02d", year, m);
                            Optional<ReportKPITarget> existing = targetRepo.findById_HotelGroupCodeAndKpiCodeAndPeriodTypeAndPeriodValue(
                                hotelGroupCode, kpiCode, periodType, periodValue);
                            if (existing.isPresent()) {
                                ReportKPITarget t = existing.get();
                                t.setTargetValue(val);
                                try {
                                    saveService.saveSingleTarget(t);
                                    updated++;
                                } catch (DataAccessException dae) {
                                    errors.add(new RowError(r+1, "DB error: " + dae.getMessage()));
                                }
                            } else {
                                ReportKPITarget t = new ReportKPITarget();
                                t.setId(new ReportKPITargetId(generateTargetId(kpiCode, periodValue), hotelGroupCode));
                                t.setKpiCode(kpiCode);
                                t.setPeriodType(periodType);
                                t.setPeriodValue(periodValue);
                                t.setTargetValue(val);
                                try {
                                    saveService.saveSingleTarget(t);
                                    created++;
                                } catch (DataAccessException dae) {
                                    errors.add(new RowError(r+1, "DB error: " + dae.getMessage()));
                                }
                            }
                        }

                    } catch (Exception rowEx) {
                        errors.add(new RowError(r+1, "Row processing error: " + rowEx.getMessage()));
                        // continue processing other rows
                    }
                } // rows
            } // sheets
        } catch (Exception ex) {
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
