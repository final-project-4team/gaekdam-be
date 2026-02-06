package com.gaekdam.gaekdambe.analytics_service.report.dataset.command.domain.entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

/**
 * IdClass for ReportOPSKPIFact composite primary key
 */
public class ReportOPSKPIFactId implements Serializable {
    private static final long serialVersionUID = 1L;

    private LocalDate kpiDate;
    private Long hotelGroupCode;

    public ReportOPSKPIFactId() {
    }

    public ReportOPSKPIFactId(LocalDate kpiDate, Long hotelGroupCode) {
        this.kpiDate = kpiDate;
        this.hotelGroupCode = hotelGroupCode;
    }

    public LocalDate getKpiDate() {
        return kpiDate;
    }

    public void setKpiDate(LocalDate kpiDate) {
        this.kpiDate = kpiDate;
    }

    public Long getHotelGroupCode() {
        return hotelGroupCode;
    }

    public void setHotelGroupCode(Long hotelGroupCode) {
        this.hotelGroupCode = hotelGroupCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReportOPSKPIFactId that = (ReportOPSKPIFactId) o;
        return Objects.equals(kpiDate, that.kpiDate) && Objects.equals(hotelGroupCode, that.hotelGroupCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(kpiDate, hotelGroupCode);
    }
}
