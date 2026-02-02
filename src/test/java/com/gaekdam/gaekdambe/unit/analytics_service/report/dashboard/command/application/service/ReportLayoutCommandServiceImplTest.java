package com.gaekdam.gaekdambe.unit.analytics_service.report.dashboard.command.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.application.dto.ReportLayoutCreateDto;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.application.dto.ReportLayoutUpdateDto;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.application.service.ReportLayoutCommandServiceImpl;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.domain.entity.ReportLayout;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.infrastructure.repository.ReportLayoutRepository;
import com.gaekdam.gaekdambe.global.exception.CustomException;
import com.gaekdam.gaekdambe.global.exception.ErrorCode;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

class ReportLayoutCommandServiceImplTest {

    @Mock
    ReportLayoutRepository repository;
    @Mock
    ObjectMapper objectMapper;

    private ReportLayoutCommandServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new ReportLayoutCommandServiceImpl(repository, objectMapper);
    }

    @Test
    void create_serializesDefaultFilterJson_and_returnsId() throws Exception {
        ReportLayoutCreateDto dto = new ReportLayoutCreateDto();
        dto.setEmployeeCode(1L);
        dto.setName("Name");
        dto.setDescription("Desc");
        dto.setIsDefault(false);

        dto.setDefaultFilterJson(null);
        when(repository.save(org.mockito.ArgumentMatchers.any(ReportLayout.class)))
                .thenAnswer(invocation -> {
                    ReportLayout rl = invocation.getArgument(0);
                    rl.setLayoutId(123L);
                    return rl;
                });

        Long id = service.create(dto);
        assertThat(id).isEqualTo(123L);
    }

    @Test
    void create_throwsCustomException_whenDefaultFilterJson_notSerializable() throws Exception {
        ReportLayoutCreateDto dto = new ReportLayoutCreateDto();
        dto.setEmployeeCode(1L);
        dto.setName("Name");
        dto.setIsDefault(false);

        Object badObj = new Object();
        dto.setDefaultFilterJson(badObj);

        when(objectMapper.writeValueAsString(badObj)).thenThrow(JsonProcessingException.class);

        assertThrows(CustomException.class, () -> service.create(dto));
    }

    @Test
    void update_updatesFields_and_serializesDefaultFilterJson() throws Exception {
        ReportLayoutUpdateDto dto = new ReportLayoutUpdateDto();
        dto.setLayoutId(9L);
        dto.setName("NewName");

        ReportLayout existing = new ReportLayout();
        existing.setLayoutId(9L);
        existing.setName("Old");

        when(repository.findById(9L)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        service.update(dto);
        assertThat(existing.getName()).isEqualTo("NewName");
    }

    @Test
    void delete_throwsCustomException_whenNotExist() {
        when(repository.existsById(5L)).thenReturn(false);
        assertThrows(CustomException.class, () -> service.delete(5L));
    }
}
