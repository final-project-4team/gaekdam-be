package com.gaekdam.gaekdambe.unit.analytics_service.report.dashboard.command.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.application.dto.ReportLayoutTemplateCreateDto;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.application.dto.ReportLayoutTemplateUpdateDto;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.application.service.ReportLayoutTemplateCommandService;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.domain.entity.ReportLayoutTemplate;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.infrastructure.repository.ReportLayoutTemplateRepository;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.infrastructure.repository.ReportTemplateRepository;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.infrastructure.repository.ReportLayoutRepository;
import com.gaekdam.gaekdambe.global.exception.CustomException;
import com.gaekdam.gaekdambe.global.exception.ErrorCode;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

class ReportLayoutTemplateCommandServiceTest {

    @Mock
    ReportLayoutTemplateRepository repo;
    @Mock
    ReportTemplateRepository templateRepo;
    @Mock
    ReportLayoutRepository layoutRepo;

    private ReportLayoutTemplateCommandService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new ReportLayoutTemplateCommandService(repo, templateRepo, layoutRepo);
    }

    @Test
    void addTemplate_validates_and_saves() {
        ReportLayoutTemplateCreateDto dto = new ReportLayoutTemplateCreateDto();
        dto.setTemplateId(5L);
        dto.setDisplayName("X");

        when(layoutRepo.existsById(1L)).thenReturn(true);
        when(templateRepo.existsById(5L)).thenReturn(true);
        when(repo.existsByLayoutIdAndTemplateId(1L, 5L)).thenReturn(false);
        when(repo.save(org.mockito.ArgumentMatchers.any(ReportLayoutTemplate.class)))
                .thenAnswer(invocation -> {
                    ReportLayoutTemplate e = invocation.getArgument(0);
                    e.setLayoutTemplateId(77L);
                    return e;
                });

        Long id = service.addTemplate(1L, 99L, dto);
        assertThat(id).isEqualTo(77L);
    }

    @Test
    void addTemplate_throws_when_layout_missing() {
        ReportLayoutTemplateCreateDto dto = new ReportLayoutTemplateCreateDto();
        dto.setTemplateId(5L);
        when(layoutRepo.existsById(1L)).thenReturn(false);
        assertThrows(CustomException.class, () -> service.addTemplate(1L, 99L, dto));
    }

    @Test
    void addTemplate_throws_when_template_missing() {
        ReportLayoutTemplateCreateDto dto = new ReportLayoutTemplateCreateDto();
        dto.setTemplateId(5L);
        when(layoutRepo.existsById(1L)).thenReturn(true);
        when(templateRepo.existsById(5L)).thenReturn(false);
        CustomException ex = assertThrows(CustomException.class, () -> service.addTemplate(1L, 99L, dto));
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.REPORT_TEMPLATE_NOT_FOUND);
    }

    @Test
    void addTemplate_throws_when_duplicate() {
        ReportLayoutTemplateCreateDto dto = new ReportLayoutTemplateCreateDto();
        dto.setTemplateId(5L);
        when(layoutRepo.existsById(1L)).thenReturn(true);
        when(templateRepo.existsById(5L)).thenReturn(true);
        when(repo.existsByLayoutIdAndTemplateId(1L, 5L)).thenReturn(true);
        CustomException ex = assertThrows(CustomException.class, () -> service.addTemplate(1L, 99L, dto));
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.REPORT_LAYOUT_TEMPLATE_DUPLICATE);
    }

    @Test
    void update_checks_layoutId_and_updates() {
        ReportLayoutTemplateUpdateDto dto = new ReportLayoutTemplateUpdateDto();
        dto.setDisplayName("New");

        ReportLayoutTemplate e = new ReportLayoutTemplate();
        e.setLayoutTemplateId(3L);
        e.setLayoutId(10L);

        when(repo.findById(3L)).thenReturn(Optional.of(e));

        service.update(10L, 3L, dto);
        assertThat(e.getDisplayName()).isEqualTo("New");

        // invalid layout id
        ReportLayoutTemplate f = new ReportLayoutTemplate();
        f.setLayoutTemplateId(4L);
        f.setLayoutId(999L);
        when(repo.findById(4L)).thenReturn(Optional.of(f));
        assertThrows(CustomException.class, () -> service.update(10L, 4L, dto));
    }

    @Test
    void delete_finds_and_deletes() {
        ReportLayoutTemplate e = new ReportLayoutTemplate();
        e.setLayoutId(2L);
        e.setTemplateId(4L);
        when(repo.findByLayoutIdAndTemplateId(2L, 4L)).thenReturn(Optional.of(e));

        service.delete(2L, 4L);
        verify(repo).delete(e);
    }
}
