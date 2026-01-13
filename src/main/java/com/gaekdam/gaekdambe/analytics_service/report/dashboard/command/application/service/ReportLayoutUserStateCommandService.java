package com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.application.service;

import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.domain.entity.ReportLayoutUserState;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.mapper.ReportLayoutUserStateMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ReportLayoutUserStateCommandService {
    private final ReportLayoutUserStateMapper mapper;

    public ReportLayoutUserStateCommandService(ReportLayoutUserStateMapper mapper) {
        this.mapper = mapper;
    }

    public int upsert(ReportLayoutUserState state) {
        state.setUpdatedAt(LocalDateTime.now());
        ReportLayoutUserState existing = mapper.findByLayoutAndEmployee(state.getLayoutId(), state.getEmployeeCode());
        if (existing == null) {
            return mapper.insert(state);
        } else {
            return mapper.update(state);
        }
    }
}
