package com.gaekdam.gaekdambe.operation_service.facility.query.mapper;

import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
import com.gaekdam.gaekdambe.operation_service.facility.query.dto.request.FacilityUsageSearchRequest;
import com.gaekdam.gaekdambe.operation_service.facility.query.dto.response.FacilityUsageCryptoRow;
import com.gaekdam.gaekdambe.operation_service.facility.query.dto.response.FacilityUsageResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FacilityUsageMapper {

    List<FacilityUsageCryptoRow> findFacilityUsages(
            @Param("page") PageRequest page,
            @Param("search") FacilityUsageSearchRequest search,
            @Param("sort") SortRequest sort
    );

    long countFacilityUsages(
            @Param("search") FacilityUsageSearchRequest search
    );
}