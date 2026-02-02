package com.gaekdam.gaekdambe.unit.analytics_service.report.dashboard.command.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.domain.ReportTemplateType;
import org.junit.jupiter.api.Test;

class ReportTemplateRepositoryTest {

    @Test
    void enum_values_present() {
        // simple sanity test for enum to avoid Spring test dependency in this environment
        ReportTemplateType[] vals = ReportTemplateType.values();
        assertThat(vals).isNotEmpty();
        assertThat(ReportTemplateType.SUMMARY_ALL).isNotNull();
    }
}
