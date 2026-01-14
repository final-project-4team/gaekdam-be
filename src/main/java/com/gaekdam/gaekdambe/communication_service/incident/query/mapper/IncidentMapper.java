package com.gaekdam.gaekdambe.communication_service.incident.query.mapper;

import com.gaekdam.gaekdambe.communication_service.incident.query.dto.request.IncidentListSearchRequest;
import com.gaekdam.gaekdambe.communication_service.incident.query.dto.response.IncidentDetailResponse;
import com.gaekdam.gaekdambe.communication_service.incident.query.dto.response.IncidentListResponse;
import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IncidentMapper {

    List<IncidentListResponse> findIncidents(
            @Param("page") PageRequest page,
            @Param("search") IncidentListSearchRequest search,
            @Param("sort") SortRequest sort
    );

    long countIncidents(
            @Param("search") IncidentListSearchRequest search
    );

    IncidentDetailResponse findIncidentDetail(
            @Param("hotelGroupCode") Long hotelGroupCode,
            @Param("incidentCode") Long incidentCode
    );
}
