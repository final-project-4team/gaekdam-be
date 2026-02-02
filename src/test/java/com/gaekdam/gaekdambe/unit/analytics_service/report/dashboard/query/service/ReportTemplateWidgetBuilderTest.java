package com.gaekdam.gaekdambe.unit.analytics_service.report.dashboard.query.service;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

import com.gaekdam.gaekdambe.analytics_service.report.dashboard.query.dto.ReportTemplateWidgetResponseDto;

class ReportTemplateWidgetBuilderTest {

    private final com.gaekdam.gaekdambe.analytics_service.report.dashboard.query.service.ReportTemplateWidgetBuilder builder = new com.gaekdam.gaekdambe.analytics_service.report.dashboard.query.service.ReportTemplateWidgetBuilder();

    @Test
    void buildCustomerTypeWidget_populatesLabelsSeriesMeta() {
        // when
        ReportTemplateWidgetResponseDto dto = builder.buildCustomerTypeWidget(123L);

        // then
        assertThat(dto).isNotNull();
        assertThat(dto.getTemplateWidgetId()).isEqualTo(123L);
        assertThat(dto.getWidgetKey()).isEqualTo("CUSTOMER_TYPE_RATIO");
        assertThat(dto.getLabels()).containsExactly("개인", "단체", "법인");
        assertThat(dto.getSeries()).hasSize(1);
        assertThat(dto.getSeries().get(0).getName()).isEqualTo("건수");
        assertThat(dto.getSeries().get(0).getData()).containsExactly(BigDecimal.valueOf(570), BigDecimal.valueOf(270), BigDecimal.valueOf(160));
        assertThat(dto.getMeta()).containsEntry("valueType", "count");
        assertThat(dto.getMeta()).containsEntry("chartKind", "doughnut");
    }

    @Test
    void buildForeignTop3Widget_populatesLabelsSeriesMeta() {
        // when
        ReportTemplateWidgetResponseDto dto = builder.buildForeignTop3Widget(999L);

        // then
        assertThat(dto).isNotNull();
        assertThat(dto.getTemplateWidgetId()).isEqualTo(999L);
        assertThat(dto.getWidgetKey()).isEqualTo("FOREIGN_TOP_COUNTRY");
        assertThat(dto.getLabels()).containsExactly("중국", "일본", "대만");
        assertThat(dto.getSeries()).hasSize(1);
        assertThat(dto.getSeries().get(0).getData()).containsExactly(BigDecimal.valueOf(340), BigDecimal.valueOf(200), BigDecimal.valueOf(70));
        assertThat(dto.getMeta()).containsEntry("chartKind", "bar");
    }
}
