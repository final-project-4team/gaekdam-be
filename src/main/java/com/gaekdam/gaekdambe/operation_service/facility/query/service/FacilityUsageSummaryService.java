package com.gaekdam.gaekdambe.operation_service.facility.query.service;


import com.gaekdam.gaekdambe.operation_service.facility.query.dto.response.FacilityUsageSummaryResponse;
import com.gaekdam.gaekdambe.operation_service.facility.query.mapper.FacilityUsageSummaryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FacilityUsageSummaryService {

    private final FacilityUsageSummaryMapper mapper;
    public List<FacilityUsageSummaryResponse> getTodaySummary(
            LocalDate date,
            Long hotelGroupCode,
            Long propertyCode
    ) {
        LocalDateTime startAt = date.atStartOfDay();
        LocalDateTime endAt = date.plusDays(1).atStartOfDay();

        return mapper.findTodayUsageSummary(
                hotelGroupCode,
                propertyCode,
                startAt,
                endAt
        );
    }
}
