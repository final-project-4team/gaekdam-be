package com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.application.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.domain.entity.ReportExport;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.mapper.ReportExportMapper;

@Service
public class ReportExportCommandService {
    private final ReportExportMapper mapper;

    public ReportExportCommandService(ReportExportMapper mapper) {
        this.mapper = mapper;
    }

    public int create(ReportExport export) {
        export.setCreatedAt(LocalDateTime.now());
        return mapper.insert(export);
    }

    public ReportExport findById(String id) {
        return mapper.findById(id);
    }

    public int updateStatus(String id, boolean success, String failureReason) {
        ReportExport exp = new ReportExport();
        exp.setExportId(id);
        exp.setSuccessYn(success);
        exp.setFailureReason(failureReason);
        return mapper.update(exp);
    }

    public int delete(String id) {
        return mapper.delete(id);
    }
}
