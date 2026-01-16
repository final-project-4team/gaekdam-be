// src/main/java/com/gaekdam/gaekdambe/customer_service/customer/query/mapper/CustomerTimelineMapper.java
package com.gaekdam.gaekdambe.customer_service.customer.query.mapper;

import com.gaekdam.gaekdambe.customer_service.customer.query.service.model.row.CustomerTimelineRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CustomerTimelineMapper {

    List<CustomerTimelineRow> findCustomerTimeline(
            @Param("hotelGroupCode") Long hotelGroupCode,
            @Param("customerCode") Long customerCode,
            @Param("limit") int limit
    );
}
