package com.gaekdam.gaekdambe.communication_service.inquiry.query.service;

import com.gaekdam.gaekdambe.communication_service.inquiry.query.dto.request.InquiryListSearchRequest;
import com.gaekdam.gaekdambe.communication_service.inquiry.query.dto.response.InquiryDetailResponse;
import com.gaekdam.gaekdambe.communication_service.inquiry.query.dto.response.InquiryListResponse;
import com.gaekdam.gaekdambe.communication_service.inquiry.query.mapper.InquiryMapper;
import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.PageResponse;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InquiryQueryService {

    private final InquiryMapper inquiryMapper;

    public PageResponse<InquiryListResponse> getInquiries(
            PageRequest page,
            InquiryListSearchRequest search,
            SortRequest sort
    ) {
        List<InquiryListResponse> list = inquiryMapper.findInquiries(page, search, sort);
        long total = inquiryMapper.countInquiries(search);

        return new PageResponse<>(
                list,
                page.getPage(),
                page.getSize(),
                total
        );
    }

    public InquiryDetailResponse getInquiryDetail(Long hotelGroupCode, Long inquiryCode) {
        return inquiryMapper.findInquiryDetail(hotelGroupCode, inquiryCode);
    }
}
