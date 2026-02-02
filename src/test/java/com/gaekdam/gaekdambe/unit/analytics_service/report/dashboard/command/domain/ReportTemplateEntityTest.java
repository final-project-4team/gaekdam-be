package com.gaekdam.gaekdambe.unit.analytics_service.report.dashboard.command.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.domain.ReportTemplateType;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.domain.entity.ReportTemplate;
import org.junit.jupiter.api.Test;

class ReportTemplateEntityTest {

    @Test
    void 레포트템플릿_prePersist_default값과_타임스탬프_검증() {
        // given
        ReportTemplate t = new ReportTemplate();
        t.setEmployeeCode(42L);
        t.setTemplateType(ReportTemplateType.SUMMARY_ALL);
        t.setTemplateName("기본템플릿");
        // null 상태로 두어 prePersist에서 기본값이 채워지는지 확인
        t.setIsActive(null);
        t.setVersion(null);
        t.setCreatedAt(null);

        // when
        t.prePersist();

        // then
        assertThat(t.getCreatedAt()).isNotNull();
        assertThat(t.getUpdatedAt()).isNotNull();
        // updatedAt은 createdAt 이후거나 같아야 함
        assertThat(t.getUpdatedAt()).isAfterOrEqualTo(t.getCreatedAt());
        assertThat(t.getIsActive()).isTrue();
        assertThat(t.getVersion()).isEqualTo(1);
    }

    @Test
    void 레포트템플릿_prePersist_preserve_existing_createdAt() {
        // given
        ReportTemplate t = new ReportTemplate();
        LocalDateTime fixed = LocalDateTime.of(2020, 1, 1, 0, 0);
        t.setCreatedAt(fixed);
        t.setUpdatedAt(null);

        // when
        t.prePersist();

        // then
        // 이미 설정된 createdAt은 변경되지 않아야 함
        assertThat(t.getCreatedAt()).isEqualTo(fixed);
        assertThat(t.getUpdatedAt()).isNotNull();
        assertThat(t.getUpdatedAt()).isAfterOrEqualTo(fixed);
    }

    @Test
    void 레포트템플릿_preUpdate_updates_updatedAt() throws InterruptedException {
        // given
        ReportTemplate t = new ReportTemplate();
        t.setCreatedAt(LocalDateTime.now());
        t.setUpdatedAt(null);

        // when
        t.preUpdate();

        // then
        assertThat(t.getUpdatedAt()).isNotNull();
    }
}
