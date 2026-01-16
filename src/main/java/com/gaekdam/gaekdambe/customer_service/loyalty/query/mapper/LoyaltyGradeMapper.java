package com.gaekdam.gaekdambe.customer_service.loyalty.query.mapper;

import com.gaekdam.gaekdambe.customer_service.loyalty.query.dto.response.LoyaltyGradeDetailQueryResponse;
import com.gaekdam.gaekdambe.customer_service.loyalty.query.dto.response.LoyaltyGradeListQueryResponse;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface LoyaltyGradeMapper {

  List<LoyaltyGradeListQueryResponse> findLoyaltyGradeList(
      @Param("hotelGroupCode") Long hotelGroupCode,
      @Param("sort") SortRequest sortReq,
      @Param("status") String status);


  LoyaltyGradeDetailQueryResponse findLoyaltyGradeDetail(
      @Param("hotelGroupCode")Long hotelGroupCode,
      @Param("loyaltyGradeCode") Long loyaltyGradeCode);
}
