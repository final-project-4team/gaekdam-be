package com.gaekdam.gaekdambe.customer_service.membership.query.service;

import com.gaekdam.gaekdambe.customer_service.membership.query.dto.response.MembershipGradeDetailQueryResponse;
import com.gaekdam.gaekdambe.customer_service.membership.query.dto.response.MembershipGradeListQueryResponse;
import com.gaekdam.gaekdambe.customer_service.membership.query.mapper.MembershipGradeMapper;
import com.gaekdam.gaekdambe.global.exception.CustomException;
import com.gaekdam.gaekdambe.global.exception.ErrorCode;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MembershipGradeQueryService {

  private final MembershipGradeMapper membershipGradeMapper;
  public List<MembershipGradeListQueryResponse> getMembershipGradeList(
      Long hotelGroupCode,String sortBy,String direction,String status ){
    SortRequest sortReq = new SortRequest();
    sortReq.setSortBy(sortBy);
    sortReq.setDirection(direction);

    return  membershipGradeMapper.findMembershipGradeList(hotelGroupCode,sortReq,status);

  }

  public MembershipGradeDetailQueryResponse getMembershipGradeDetail(Long hotelGroupCode, Long membershipGradeCode) {
    MembershipGradeDetailQueryResponse membershipGradeDetail= membershipGradeMapper.findMembershipGradeDetail(hotelGroupCode,membershipGradeCode);
    if(membershipGradeDetail==null){
      throw new CustomException(ErrorCode.NOT_FOUND_VALUE);
    }
    return membershipGradeDetail;
  }
}
