package com.gaekdam.gaekdambe.customer_service.loyalty.query.service;

import com.gaekdam.gaekdambe.customer_service.loyalty.query.dto.response.LoyaltyGradeDetailQueryResponse;
import com.gaekdam.gaekdambe.customer_service.loyalty.query.dto.response.LoyaltyGradeListQueryResponse;
import com.gaekdam.gaekdambe.customer_service.loyalty.query.mapper.LoyaltyGradeMapper;
import com.gaekdam.gaekdambe.global.exception.CustomException;
import com.gaekdam.gaekdambe.global.exception.ErrorCode;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoyaltyGradeQueryService {

  private final LoyaltyGradeMapper loyaltyGradeMapper;
  public List<LoyaltyGradeListQueryResponse> getLoyaltyGradeList(
      Long hotelGroupCode,String sortBy,String direction,String status ){
    SortRequest sortReq = new SortRequest();
    sortReq.setSortBy(sortBy);
    sortReq.setDirection(direction);

    return  loyaltyGradeMapper.findLoyaltyGradeList(hotelGroupCode,sortReq,status);

  }

  public LoyaltyGradeDetailQueryResponse getLoyaltyGradeDetail(Long hotelGroupCode, Long loyaltyGradeCode) {
    LoyaltyGradeDetailQueryResponse loyaltyGradeDetail= loyaltyGradeMapper.findLoyaltyGradeDetail(hotelGroupCode,loyaltyGradeCode);
    if(loyaltyGradeDetail==null){
      throw new CustomException(ErrorCode.LOYALTY_GRADE_NOT_FOUND);
    }
    return loyaltyGradeDetail;
  }
}
