package com.gaekdam.gaekdambe.operation_service.facility.query.mapper;

import com.gaekdam.gaekdambe.operation_service.facility.query.dto.response.FacilityUsageSummaryResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface FacilityUsageSummaryMapper {

    List<FacilityUsageSummaryResponse> findTodayUsageSummary(
            @Param("date") LocalDate date,
            @Param("hotelGroupCode") Long hotelGroupCode
    );

}
