package com.gaekdam.gaekdambe.unit.analytics_service.report.dashboard.query.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.gaekdam.gaekdambe.analytics_service.report.dashboard.query.dto.ReportLayoutListQueryDto;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.query.dto.ReportLayoutResponseDto;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.query.dto.ReportLayoutTemplateListResponseDto;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.query.mapper.ReportLayoutQueryMapper;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.query.service.ReportLayoutQueryServiceImpl;

class ReportLayoutQueryServiceImplTest {

    @Mock
    ReportLayoutQueryMapper mapper;

    private ReportLayoutQueryServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new ReportLayoutQueryServiceImpl(mapper);
    }

    @Test
    void getById_delegatesToMapper() {
        ReportLayoutResponseDto dto = new ReportLayoutResponseDto();
        when(mapper.findById(10L)).thenReturn(dto);

        var res = service.getById(10L);
        assertThat(res).isSameAs(dto);
    }

    @Test
    void list_and_count_delegateToMapper() {
        ReportLayoutListQueryDto q = ReportLayoutListQueryDto.builder().employeeCode(null).name(null).offset(0).limit(10).build();
        when(mapper.findByQuery(q)).thenReturn(List.of(new ReportLayoutResponseDto(), new ReportLayoutResponseDto()));
        when(mapper.countByQuery(q)).thenReturn(7);

        var list = service.list(q);
        var count = service.count(q);
        assertThat(list).hasSize(2);
        assertThat(count).isEqualTo(7);
    }

    @Test
    void getTemplatesByLayoutId_returnsDtoWithTemplatesAndInitialId() {
        ReportLayoutTemplateListResponseDto tplDto = ReportLayoutTemplateListResponseDto.builder().layoutId(5L).build();
        when(mapper.selectTemplatesByLayoutId(5L)).thenReturn(List.of());
        when(mapper.selectInitialTemplateId(5L)).thenReturn(123L);

        var res = service.getTemplatesByLayoutId(5L);
        assertThat(res).isNotNull();
        assertThat(res.getLayoutId()).isEqualTo(5L);
        assertThat(res.getInitialTemplateId()).isEqualTo(123L);
    }
}
