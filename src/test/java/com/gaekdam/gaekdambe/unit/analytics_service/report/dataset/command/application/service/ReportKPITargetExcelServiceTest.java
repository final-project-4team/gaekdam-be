package com.gaekdam.gaekdambe.unit.analytics_service.report.dataset.command.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.Optional;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.gaekdam.gaekdambe.analytics_service.report.dataset.command.application.service.ReportKPITargetExcelService;
import com.gaekdam.gaekdambe.analytics_service.report.dataset.command.application.service.ReportKPITargetSaveService;
import com.gaekdam.gaekdambe.analytics_service.report.dataset.command.domain.entity.ReportKPICodeDim;
import com.gaekdam.gaekdambe.analytics_service.report.dataset.command.domain.entity.ReportKPITarget;
import com.gaekdam.gaekdambe.analytics_service.report.dataset.command.domain.entity.ReportKPITargetId;
import com.gaekdam.gaekdambe.analytics_service.report.dataset.command.domain.infrastructure.repository.ReportKPICodeDimRepository;
import com.gaekdam.gaekdambe.analytics_service.report.dataset.command.domain.infrastructure.repository.ReportKPITargetRepository;

class ReportKPITargetExcelServiceTest {

    @Mock
    ReportKPICodeDimRepository kpiRepo;

    @Mock
    ReportKPITargetRepository targetRepo;

    @Mock
    ReportKPITargetSaveService saveService;

    private ReportKPITargetExcelService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new ReportKPITargetExcelService(kpiRepo, targetRepo, saveService);
    }

    @Test
    void generateTemplateExcel_withYear_returnsWorkbookBytes() throws Exception {
        ReportKPICodeDim k1 = new ReportKPICodeDim();
        k1.setKpiCode("K1"); k1.setKpiName("Name1"); k1.setUnit("unit1");
        when(kpiRepo.findByIsActiveTrueOrderByKpiCodeAsc()).thenReturn(java.util.List.of(k1));

        byte[] bytes = service.generateTemplateExcel(1L, "MONTH", "2025");
        assertThat(bytes).isNotNull();
        assertThat(bytes.length).isGreaterThan(0);

        try (XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            XSSFSheet sheet = wb.getSheet("2025");
            assertThat(sheet).isNotNull();
            // header row first cell should be hotelGroupCode
            var header = sheet.getRow(0);
            assertThat(header.getCell(0).getStringCellValue()).isEqualTo("hotelGroupCode");
            // first data row should contain the hotelGroupCode value cell blank but kpiCode present in cell 1
            var firstData = sheet.getRow(1);
            assertThat(firstData.getCell(1).getStringCellValue()).isEqualTo("K1");
        }
    }

    @Test
    void importFromExcel_createsAndUpdatesTargets() throws Exception {
        // prepare workbook in memory
        try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            XSSFSheet sheet = wb.createSheet("2025");
            // header
            var header = sheet.createRow(0);
            header.createCell(0).setCellValue("hotelGroupCode");
            header.createCell(1).setCellValue("kpicode");
            header.createCell(2).setCellValue("kpiName");
            header.createCell(3).setCellValue("unit");
            header.createCell(4).setCellValue("Annual");
            header.createCell(5).setCellValue("01");
            header.createCell(6).setCellValue("02");

            // row: existing kpi K1, annual + month01 + month02
            var row1 = sheet.createRow(1);
            row1.createCell(0).setCellValue(1L);
            row1.createCell(1).setCellValue("K1");
            row1.createCell(2).setCellValue("Name1");
            row1.createCell(3).setCellValue("unit");
            row1.createCell(4).setCellValue("100"); // Annual
            row1.createCell(5).setCellValue("10"); // Jan
            row1.createCell(6).setCellValue("20"); // Feb

            wb.write(bos);
            byte[] bytes = bos.toByteArray();

            MultipartFile mf = new MockMultipartFile("file", "targets.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", bytes);

            // mocks: kpi exists
            ReportKPICodeDim k1 = new ReportKPICodeDim(); k1.setKpiCode("K1");
            when(kpiRepo.findByKpiCode("K1")).thenReturn(Optional.of(k1));

            // targetRepo: for annual 2025 -> none (create), for 2025-01 -> existing (update), for 2025-02 -> none (create)
            ReportKPITarget existingJan = new ReportKPITarget();
            existingJan.setId(new ReportKPITargetId("K1_2025-01", 1L));
            when(targetRepo.findById_HotelGroupCodeAndKpiCodeAndPeriodTypeAndPeriodValue(1L, "K1", "MONTH", "2025-01")).thenReturn(Optional.of(existingJan));

            when(targetRepo.findById_HotelGroupCodeAndKpiCodeAndPeriodTypeAndPeriodValue(1L, "K1", "YEAR", "2025")).thenReturn(Optional.empty());
            when(targetRepo.findById_HotelGroupCodeAndKpiCodeAndPeriodTypeAndPeriodValue(1L, "K1", "MONTH", "2025-02")).thenReturn(Optional.empty());

            var result = service.importFromExcel(1L, null, null, mf);

            assertThat(result).isNotNull();
            // created: annual + Feb = 2, updated: Jan =1
            assertThat(result.getCreated()).isEqualTo(2);
            assertThat(result.getUpdated()).isEqualTo(1);
            assertThat(result.getSkipped()).isEqualTo(0);
            assertThat(result.getErrors()).isEmpty();

            // verify saveService invoked three times (two creates + one update)
            verify(saveService, times(3)).saveSingleTarget(org.mockito.ArgumentMatchers.any(ReportKPITarget.class));
        }
    }
}
