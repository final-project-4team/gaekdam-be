package com.gaekdam.gaekdambe.communication_service.inquiry.query.mapper;

import com.gaekdam.gaekdambe.communication_service.inquiry.query.dto.request.InquiryListSearchRequest;
import com.gaekdam.gaekdambe.communication_service.inquiry.query.service.model.InquiryDetailRow;
import com.gaekdam.gaekdambe.communication_service.inquiry.query.service.model.InquiryListRow;
import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface InquiryMapper {

    List<InquiryListRow> findInquiries(
            @Param("page") PageRequest page,
            @Param("search") InquiryListSearchRequest search,
            @Param("sort") SortRequest sort
    );

    long countInquiries(@Param("search") InquiryListSearchRequest search);

    InquiryDetailRow findInquiryDetail(
            @Param("hotelGroupCode") Long hotelGroupCode,
            @Param("inquiryCode") Long inquiryCode
    );
}
