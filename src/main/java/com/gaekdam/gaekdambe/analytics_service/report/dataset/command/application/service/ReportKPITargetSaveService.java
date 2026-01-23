package com.gaekdam.gaekdambe.analytics_service.report.dataset.command.application.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.gaekdam.gaekdambe.analytics_service.report.dataset.command.domain.entity.ReportKPITarget;
import com.gaekdam.gaekdambe.analytics_service.report.dataset.command.domain.infrastructure.repository.ReportKPITargetRepository;

@Service
public class ReportKPITargetSaveService {

    private final ReportKPITargetRepository targetRepo;

    public ReportKPITargetSaveService(ReportKPITargetRepository targetRepo) {
        this.targetRepo = targetRepo;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveSingleTarget(ReportKPITarget t) {
        LocalDateTime now = LocalDateTime.now();

        // If an id exists, load fresh entity in this new transaction and update it.
        if (t.getId() != null) {
            Optional<ReportKPITarget> opt = targetRepo.findById(t.getId());
            if (opt.isPresent()) {
                ReportKPITarget existing = opt.get();
                // copy relevant updatable fields
                existing.setTargetValue(t.getTargetValue());
                existing.setKpiCode(t.getKpiCode());
                existing.setPeriodType(t.getPeriodType());
                existing.setPeriodValue(t.getPeriodValue());
                existing.setEffectiveFrom(t.getEffectiveFrom());
                existing.setEffectiveTo(t.getEffectiveTo());
                existing.setWarningThreshold(t.getWarningThreshold());
                existing.setDangerThreshold(t.getDangerThreshold());
                existing.setSeasonType(t.getSeasonType());
                if (existing.getCreatedAt() == null) existing.setCreatedAt(now);
                existing.setUpdatedAt(now);
                targetRepo.save(existing);
                return;
            }
        }

        // Otherwise treat as new entity
        if (t.getCreatedAt() == null) t.setCreatedAt(now);
        t.setUpdatedAt(now);
        targetRepo.save(t);
    }
}
