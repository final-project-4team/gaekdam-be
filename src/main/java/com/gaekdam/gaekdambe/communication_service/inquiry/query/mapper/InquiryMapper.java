package com.gaekdam.gaekdambe.communication_service.inquiry.query.mapper;

import com.gaekdam.gaekdambe.communication_service.inquiry.query.dto.request.InquiryListSearchRequest;
import com.gaekdam.gaekdambe.communication_service.inquiry.query.dto.response.InquiryDetailResponse;
import com.gaekdam.gaekdambe.communication_service.inquiry.query.dto.response.InquiryListResponse;
import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface InquiryMapper {

    List<InquiryListResponse> findInquiries(
            @Param("page") PageRequest page,
            @Param("search") InquiryListSearchRequest search,
            @Param("sort") SortRequest sort
    );

    long countInquiries(
            @Param("search") InquiryListSearchRequest search
    );

    InquiryDetailResponse findInquiryDetail(
            @Param("hotelGroupCode") Long hotelGroupCode,
            @Param("inquiryCode") Long inquiryCode
    );
}
