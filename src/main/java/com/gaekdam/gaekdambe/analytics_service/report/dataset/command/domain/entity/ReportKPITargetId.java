package com.gaekdam.gaekdambe.analytics_service.report.dataset.command.domain.entity;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode
public class ReportKPITargetId implements Serializable {

    @Column(name = "target_id", nullable = false, length = 255)
    private String targetId;

    @Column(name = "hotel_group_code", nullable = false)
    private Long hotelGroupCode;
}
