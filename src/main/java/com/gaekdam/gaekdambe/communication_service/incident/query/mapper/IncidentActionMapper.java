package com.gaekdam.gaekdambe.communication_service.incident.query.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface IncidentActionMapper {

    Long findEmployeeCodeByLoginId(
            @Param("hotelGroupCode") Long hotelGroupCode,
            @Param("loginId") String loginId
    );
}
