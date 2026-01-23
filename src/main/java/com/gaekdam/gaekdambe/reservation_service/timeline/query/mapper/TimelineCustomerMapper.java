package com.gaekdam.gaekdambe.reservation_service.timeline.query.mapper;

import com.gaekdam.gaekdambe.reservation_service.timeline.query.dto.response.TimelineCustomerRow;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TimelineCustomerMapper {

    List<TimelineCustomerRow> findTimelineCustomers(
            Long hotelGroupCode,
            String keyword
    );
}
