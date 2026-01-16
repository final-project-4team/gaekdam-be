package com.gaekdam.gaekdambe.customer_service.membership.query.mapper;

import com.gaekdam.gaekdambe.customer_service.membership.query.dto.response.MembershipGradeDetailQueryResponse;
import com.gaekdam.gaekdambe.customer_service.membership.query.dto.response.MembershipGradeListQueryResponse;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface MembershipGradeMapper {

  List<MembershipGradeListQueryResponse> findMembershipGradeList(
      @Param("hotelGroupCode") Long hotelGroupCode,
      @Param("sort") SortRequest sortReq,
      @Param("status") String status);


  MembershipGradeDetailQueryResponse findMembershipGradeDetail(
      @Param("hotelGroupCode")Long hotelGroupCode,
      @Param("membershipGradeCode") Long membershipGradeCode);
}
