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
      Long hotelGroupCode,SortRequest sort,String status ){

    return  membershipGradeMapper.findMembershipGradeList(hotelGroupCode,sort,status);

  }

  public MembershipGradeDetailQueryResponse getMembershipGradeDetail(Long hotelGroupCode, Long membershipGradeCode) {
    MembershipGradeDetailQueryResponse membershipGradeDetail= membershipGradeMapper.findMembershipGradeDetail(hotelGroupCode,membershipGradeCode);


      if (membershipGradeDetail == null) {
          throw new CustomException(ErrorCode.MEMBERSHIP_GRADE_NOT_FOUND);
      }
      return membershipGradeDetail;
  }
}
