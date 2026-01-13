package com.gaekdam.gaekdambe.operation_service.facility.query.mapper;

import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
import com.gaekdam.gaekdambe.operation_service.facility.query.dto.request.FacilitySearchRequest;
import com.gaekdam.gaekdambe.operation_service.facility.query.dto.response.FacilityResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FacilityMapper {

    List<FacilityResponse> findFacilities(
            @Param("page") PageRequest page,
            @Param("search") FacilitySearchRequest search,
            @Param("sort") SortRequest sort
    );

    long countFacilities(
            @Param("search") FacilitySearchRequest search
    );
}
