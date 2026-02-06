package com.gaekdam.gaekdambe.reservation_service.reservation.query.dto.response.detail;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class FacilityUsageInfo {
    private String facilityName;
    private long usageCount;
    private LocalDateTime lastUsedAt;
}
