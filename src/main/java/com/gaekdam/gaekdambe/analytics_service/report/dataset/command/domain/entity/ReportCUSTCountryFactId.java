package com.gaekdam.gaekdambe.analytics_service.report.dataset.command.domain.entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

/**
 * IdClass for ReportCUSTCountryFact composite primary key
 */
public class ReportCUSTCountryFactId implements Serializable {
    private static final long serialVersionUID = 1L;

    private LocalDate kpiDate;
    private Long hotelGroupCode;
    private String countryCode;

    public ReportCUSTCountryFactId() {
    }

    public ReportCUSTCountryFactId(LocalDate kpiDate, Long hotelGroupCode, String countryCode) {
        this.kpiDate = kpiDate;
        this.hotelGroupCode = hotelGroupCode;
        this.countryCode = countryCode;
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

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReportCUSTCountryFactId that = (ReportCUSTCountryFactId) o;
        return Objects.equals(kpiDate, that.kpiDate) && Objects.equals(hotelGroupCode, that.hotelGroupCode) && Objects.equals(countryCode, that.countryCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(kpiDate, hotelGroupCode, countryCode);
    }
}
