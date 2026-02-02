package com.gaekdam.gaekdambe.unit.analytics_service.report.dashboard.command.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.domain.entity.ReportTemplateWidget;
import org.junit.jupiter.api.Test;

class ReportTemplateWidgetEntityTest {

    @Test
    void 템플릿위젯_prePersist_기본값_및_타임스탬프_검증() throws Exception {
        // given
        ReportTemplateWidget w = new ReportTemplateWidget();
        w.setTemplateId(1L);
        w.setWidgetType("LINE");
        w.setTitle("매출추이");
        w.setDatasetType("REV");
        w.setMetricKey("revenue_total");
        // 기본값을 null로 두어 prePersist에서 채워지는지 확인
        w.setDefaultPeriod(null);
        w.setDefaultSortOrder(null);
        w.setCreatedAt(null);

        // when - prePersist is package-private, invoke via reflection
        Method prePersist = ReportTemplateWidget.class.getDeclaredMethod("prePersist");
        prePersist.setAccessible(true);
        prePersist.invoke(w);

        // then
        assertThat(w.getCreatedAt()).isNotNull();
        assertThat(w.getUpdatedAt()).isNotNull();
        assertThat(w.getUpdatedAt()).isAfterOrEqualTo(w.getCreatedAt());
        assertThat(w.getDefaultPeriod()).isEqualTo("MONTH");
        assertThat(w.getDefaultSortOrder()).isEqualTo(0);
    }

    @Test
    void 템플릿위젯_preUpdate_업데이트타임스탬프변경() throws Exception {
        // given
        ReportTemplateWidget w = new ReportTemplateWidget();
        w.setCreatedAt(LocalDateTime.of(2021, 5, 5, 5, 5));
        w.setUpdatedAt(null);

        // when - preUpdate is package-private, invoke via reflection
        Method preUpdate = ReportTemplateWidget.class.getDeclaredMethod("preUpdate");
        preUpdate.setAccessible(true);
        preUpdate.invoke(w);

        // then
        assertThat(w.getUpdatedAt()).isNotNull();
        assertThat(w.getUpdatedAt()).isAfterOrEqualTo(w.getCreatedAt());
    }
}
