package com.gaekdam.gaekdambe.communication_service.incident.query.mapper;

import com.gaekdam.gaekdambe.communication_service.incident.query.dto.request.IncidentListSearchRequest;
import com.gaekdam.gaekdambe.communication_service.incident.query.dto.response.IncidentActionHistoryEncResponse;
import com.gaekdam.gaekdambe.communication_service.incident.query.dto.response.IncidentDetailEncResponse;
import com.gaekdam.gaekdambe.communication_service.incident.query.dto.response.IncidentListEncResponse;
import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IncidentMapper {

    List<IncidentListEncResponse> findIncidents(
            @Param("page") PageRequest page,
            @Param("search") IncidentListSearchRequest search,
            @Param("sort") SortRequest sort
    );


    long countIncidents(@Param("search") IncidentListSearchRequest search);

    IncidentDetailEncResponse findIncidentDetail(
            @Param("hotelGroupCode") Long hotelGroupCode,
            @Param("incidentCode") Long incidentCode
    );

    // 조치 이력 조회 (Enc로 받아서 Service에서 복호화)
    List<IncidentActionHistoryEncResponse> findIncidentActionHistories(
            @Param("hotelGroupCode") Long hotelGroupCode,
            @Param("incidentCode") Long incidentCode
    );
}
