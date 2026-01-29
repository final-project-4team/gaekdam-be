package com.gaekdam.gaekdambe.communication_service.inquiry.query.service;

import com.gaekdam.gaekdambe.communication_service.inquiry.query.dto.request.InquiryListSearchRequest;
import com.gaekdam.gaekdambe.communication_service.inquiry.query.dto.response.InquiryDetailResponse;
import com.gaekdam.gaekdambe.communication_service.inquiry.query.dto.response.InquiryListResponse;
import com.gaekdam.gaekdambe.communication_service.inquiry.query.mapper.InquiryMapper;
import com.gaekdam.gaekdambe.communication_service.inquiry.query.service.model.InquiryDetailRow;
import com.gaekdam.gaekdambe.communication_service.inquiry.query.service.model.InquiryListRow;
import com.gaekdam.gaekdambe.global.crypto.DecryptionService;
import com.gaekdam.gaekdambe.global.exception.CustomException;
import com.gaekdam.gaekdambe.global.exception.ErrorCode;
import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.PageResponse;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
import com.gaekdam.gaekdambe.iam_service.log.command.application.aop.annotation.LogPersonalInfo;
import com.gaekdam.gaekdambe.iam_service.permission_type.command.domain.seeds.PermissionTypeKey;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InquiryQueryService {

    private final InquiryMapper inquiryMapper;
    private final DecryptionService decryptionService;
    public PageResponse<InquiryListResponse> getInquiries(
            PageRequest page,
            InquiryListSearchRequest search,
            SortRequest sort
    ) {
        List<InquiryListRow> rows = inquiryMapper.findInquiries(page, search, sort);
        long total = inquiryMapper.countInquiries(search);

        List<InquiryListResponse> responses = rows.stream()
                .map(this::toListResponse)
                .toList();

        return new PageResponse<>(responses, page.getPage(), page.getSize(), total);
    }


    @LogPersonalInfo(type = PermissionTypeKey.CUSTOMER_READ, purpose = "고객 정보 조회")
    public InquiryDetailResponse getInquiryDetail(Long hotelGroupCode, Long inquiryCode) {
        InquiryDetailRow detailRow = inquiryMapper.findInquiryDetail(hotelGroupCode, inquiryCode);
        if (detailRow == null) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "존재하지 않는 문의입니다.");
        }

        String customerName = decryptCustomerName(
                detailRow.customerCode(),
                detailRow.dekEnc(),
                detailRow.customerNameEnc()
        );

        String employeeName = decryptEmployeeName(
                detailRow.employeeCode(),
                detailRow.employeeDekEnc(),
                detailRow.employeeNameEnc()
        );

        return new InquiryDetailResponse(
                detailRow.inquiryCode(),
                detailRow.inquiryStatus(),
                detailRow.inquiryTitle(),
                detailRow.inquiryContent(),
                detailRow.answerContent(),
                detailRow.createdAt(),
                detailRow.updatedAt(),
                detailRow.customerCode(),
                detailRow.employeeCode(),

                detailRow.employeeLoginId(),
                employeeName,

                detailRow.propertyCode(),
                detailRow.inquiryCategoryCode(),
                detailRow.inquiryCategoryName(),
                detailRow.linkedIncidentCode(),
                customerName
        );
    }

    private InquiryListResponse toListResponse(InquiryListRow listRow) {
        String customerName = decryptCustomerName(
                listRow.customerCode(),
                listRow.dekEnc(),
                listRow.customerNameEnc()
        );

        String employeeName = decryptEmployeeName(
                listRow.employeeCode(),
                listRow.employeeDekEnc(),
                listRow.employeeNameEnc()
        );

        return new InquiryListResponse(
                listRow.inquiryCode(),
                listRow.createdAt(),
                listRow.inquiryTitle(),
                listRow.inquiryStatus(),
                listRow.customerCode(),
                listRow.employeeCode(),
                listRow.employeeLoginId(),
                employeeName,
                listRow.propertyCode(),
                listRow.inquiryCategoryCode(),
                listRow.inquiryCategoryName(),
                listRow.linkedIncidentCode(),
                customerName
        );
    }

    private String decryptCustomerName(Long customerCode, byte[] dekEnc, byte[] customerNameEnc) {
        if (customerNameEnc == null || dekEnc == null) return null;
        return decryptionService.decrypt(customerCode, dekEnc, customerNameEnc);
    }

    private String decryptEmployeeName(Long employeeCode, byte[] employeeDekEnc, byte[] employeeNameEnc) {
        if (employeeNameEnc == null || employeeDekEnc == null) return null;
        // cacheCode = employeeCode 로 잡아도 됨(직원 DEK 캐시 키)
        return decryptionService.decrypt(employeeCode, employeeDekEnc, employeeNameEnc);
    }
}
