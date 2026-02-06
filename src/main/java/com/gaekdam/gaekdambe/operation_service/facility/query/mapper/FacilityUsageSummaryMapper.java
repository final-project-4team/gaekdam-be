package com.gaekdam.gaekdambe.operation_service.facility.query.mapper;

import com.gaekdam.gaekdambe.operation_service.facility.query.dto.response.FacilityUsageSummaryResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface FacilityUsageSummaryMapper {

    List<FacilityUsageSummaryResponse> findTodayUsageSummary(
            @Param("hotelGroupCode") Long hotelGroupCode,
            @Param("propertyCode") Long propertyCode,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt
    );
}

