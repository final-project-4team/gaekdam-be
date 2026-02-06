package com.gaekdam.gaekdambe.analytics_service.report.dataset.query.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gaekdam.gaekdambe.analytics_service.report.dataset.command.domain.infrastructure.repository.ReportKPICodeDimRepository;
import com.gaekdam.gaekdambe.analytics_service.report.dataset.query.dto.KpiCodeDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ReportKPICodeDimService {
    private final ReportKPICodeDimRepository repo;

    public List<KpiCodeDto> listActive() {
        return repo.findByIsActiveTrueOrderByKpiCodeAsc().stream()
        .map(e -> new KpiCodeDto(e.getKpiCode(), e.getKpiName(), e.getUnit(), e.getDescription()))
        .collect(Collectors.toList());
    }
}
