package com.gaekdam.gaekdambe.unit.analytics_service.report.dashboard.command.application.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.application.dto.ReportLayoutTemplateCreateDto;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.application.dto.ReportLayoutTemplateUpdateDto;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.application.dto.ReportLayoutUpdateDto;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.domain.VisibilityScope;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ReportLayoutDtoTest {

    @Test
    void createDto_builder_and_getters() {
        ReportLayoutTemplateCreateDto dto = ReportLayoutTemplateCreateDto.builder()
            .templateId(10L)
            .displayName("Template A")
            .build();

        assertThat(dto.getTemplateId()).isEqualTo(10L);
        assertThat(dto.getDisplayName()).isEqualTo("Template A");
        // sortOrder was not set -> null
        assertThat(dto.getSortOrder()).isNull();
    }

    @Test
    void updateDto_setters_and_nulls() {
        ReportLayoutTemplateUpdateDto dto = new ReportLayoutTemplateUpdateDto();
        dto.setDisplayName("New Name");
        dto.setSortOrder(5);
        dto.setIsActive(false);

        assertThat(dto.getDisplayName()).isEqualTo("New Name");
        assertThat(dto.getSortOrder()).isEqualTo(5);
        assertThat(dto.getIsActive()).isFalse();

        // ensure nulls are allowed for partial updates
        ReportLayoutTemplateUpdateDto empty = new ReportLayoutTemplateUpdateDto();
        assertThat(empty.getDisplayName()).isNull();
        assertThat(empty.getSortOrder()).isNull();
        assertThat(empty.getIsActive()).isNull();
    }

    @Test
    void reportLayoutUpdateDto_full_fields_and_object_json() {
        Map<String, Object> filter = Map.of("key", "value");
        ReportLayoutUpdateDto dto = ReportLayoutUpdateDto.builder()
            .layoutId(123L)
            .name("Layout 1")
            .description("desc")
            .isDefault(true)
            .isArchived(false)
            .visibilityScope(VisibilityScope.TENANT)
            .dateRangePreset("LAST_7_DAYS")
            .defaultFilterJson(filter)
            .build();

        assertThat(dto.getLayoutId()).isEqualTo(123L);
        assertThat(dto.getName()).isEqualTo("Layout 1");
        assertThat(dto.getDefaultFilterJson()).isSameAs(filter);
        assertThat(dto.getVisibilityScope()).isEqualTo(VisibilityScope.TENANT);
    }
}
