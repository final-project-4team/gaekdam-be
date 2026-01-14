package com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.domain.entity;

import java.io.Serializable;
import java.util.Objects;

/**
 * IdClass for ReportLayoutUserState composite primary key
 */
public class ReportLayoutUserStateId implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long layoutId;
    private Long employeeCode;

    public ReportLayoutUserStateId() {
    }

    public ReportLayoutUserStateId(Long layoutId, Long employeeCode) {
        this.layoutId = layoutId;
        this.employeeCode = employeeCode;
    }

    public Long getLayoutId() {
        return layoutId;
    }

    public void setLayoutId(Long layoutId) {
        this.layoutId = layoutId;
    }

    public Long getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(Long employeeCode) {
        this.employeeCode = employeeCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReportLayoutUserStateId that = (ReportLayoutUserStateId) o;
        return Objects.equals(layoutId, that.layoutId) && Objects.equals(employeeCode, that.employeeCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(layoutId, employeeCode);
    }
}
