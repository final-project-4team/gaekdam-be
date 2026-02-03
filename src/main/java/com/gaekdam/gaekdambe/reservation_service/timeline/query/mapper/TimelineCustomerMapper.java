package com.gaekdam.gaekdambe.reservation_service.timeline.query.mapper;

import com.gaekdam.gaekdambe.reservation_service.timeline.query.dto.response.TimelineCustomerRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TimelineCustomerMapper {

    List<TimelineCustomerRow> findTimelineCustomers(
            @Param("hotelGroupCode") Long hotelGroupCode,
            @Param("customerCodeKeyword") String customerCodeKeyword,
            @Param("nameHash") String nameHash,
            @Param("phoneHash") String phoneHash
    );
}
