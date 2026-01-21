package com.gaekdam.gaekdambe.customer_service.loyalty.query.mapper;

import com.gaekdam.gaekdambe.customer_service.loyalty.query.dto.response.LoyaltyHistoryResponse;
import com.gaekdam.gaekdambe.global.paging.PageRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface LoyaltyHistoryMapper {

    List<LoyaltyHistoryResponse> findHistory(
            @Param("page") PageRequest page,
            @Param("hotelGroupCode") Long hotelGroupCode,
            @Param("customerCode") Long customerCode,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    long countHistory(
            @Param("hotelGroupCode") Long hotelGroupCode,
            @Param("customerCode") Long customerCode,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );
}
