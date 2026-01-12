package com.gaekdam.gaekdambe.operation_service.facility.query.service;

import com.gaekdam.gaekdambe.operation_service.facility.query.dto.response.FacilityUsageSummaryResponse;
import com.gaekdam.gaekdambe.operation_service.facility.query.mapper.FacilityUsageSummaryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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
        return mapper.findTodayUsageSummary(date, hotelGroupCode, propertyCode);
    }
}
