package com.gaekdam.gaekdambe.communication_service.inquiry.query.service;

import com.gaekdam.gaekdambe.communication_service.inquiry.query.dto.request.InquiryListSearchRequest;
import com.gaekdam.gaekdambe.communication_service.inquiry.query.dto.response.InquiryDetailResponse;
import com.gaekdam.gaekdambe.communication_service.inquiry.query.dto.response.InquiryListResponse;
import com.gaekdam.gaekdambe.communication_service.inquiry.query.mapper.InquiryMapper;
import com.gaekdam.gaekdambe.communication_service.inquiry.query.service.model.InquiryDetailRow;
import com.gaekdam.gaekdambe.communication_service.inquiry.query.service.model.InquiryListRow;
import com.gaekdam.gaekdambe.global.crypto.DecryptionService;
import com.gaekdam.gaekdambe.global.crypto.MaskingUtils;
import com.gaekdam.gaekdambe.global.crypto.SearchHashService;
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

import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InquiryQueryService {

    private static final String UNASSIGNED = "미지정";

    private final InquiryMapper inquiryMapper;
    private final DecryptionService decryptionService;
    private final SearchHashService searchHashService;

    public PageResponse<InquiryListResponse> getInquiries(
            PageRequest page,
            InquiryListSearchRequest search,
            SortRequest sort
    ) {
        normalizeSearch(search);

        List<InquiryListRow> rows = inquiryMapper.findInquiries(page, search, sort);
        long total = inquiryMapper.countInquiries(search);

        List<InquiryListResponse> responses = rows.stream()
                .map(this::toListResponse)
                .toList();

        return new PageResponse<>(responses, page.getPage(), page.getSize(), total);
    }

    private void normalizeSearch(InquiryListSearchRequest search) {
        if (search == null) return;

        String type = trim(search.getSearchType());
        String keyword = trim(search.getKeyword());

        // keyword 없으면 파생필드 전부 비움
        if (keyword == null) {
            clearSearchDerivedFields(search);
            return;
        }

        // 기본은 ALL
        if (type == null) type = "ALL";
        search.setSearchType(type);

        // 먼저 파생필드 클리어
        clearSearchDerivedFields(search);

        switch (type) {
            case "CUSTOMER_NAME" -> {
                // customer.customer_name_hash 가 String 컬럼이라 String으로 만들어서 세팅해야 함
                search.setCustomerNameHash(toCustomerNameHashString(keyword));
            }
            case "EMPLOYEE_NAME" -> {
                // employee.employee_name_hash 가 VARBINARY(byte[]) 라면 byte[] 그대로
                search.setEmployeeNameHash(searchHashService.nameHash(keyword));
            }
            case "EMPLOYEE_ID" -> {
                search.setEmployeeLoginId(keyword);
            }
            case "TITLE", "ALL" -> {
                // keyword는 mapper에서 LIKE로 사용
            }
            default -> search.setSearchType("ALL");
        }
    }

    private void clearSearchDerivedFields(InquiryListSearchRequest search) {
        search.setCustomerNameHash(null);
        search.setEmployeeNameHash(null);
        search.setEmployeeLoginId(null);
    }

    private String trim(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    /**
     * customer_name_hash 저장 포맷과 반드시 동일해야 함.
     * 지금은 "HMAC 결과(byte[])"를 HEX 문자열로 변환하는 방식으로 작성.
     * (customer_name_hash가 base64면 여기만 base64로 바꾸면 됨)
     */
    private String toCustomerNameHashString(String plainName) {
        byte[] h = searchHashService.nameHash(plainName);
        return bytesToHex(h);
    }

    private String bytesToHex(byte[] bytes) {
        if (bytes == null) return null;
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
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

    private InquiryListResponse toListResponse(InquiryListRow r) {
        String customerName = decryptCustomerName(r.customerCode(), r.dekEnc(), r.customerNameEnc());
        customerName = MaskingUtils.maskName(customerName);

        // 담당자 미지정 처리
        String employeeName;
        String employeeLoginId = r.employeeLoginId();

        if (r.employeeCode() == null) {
            employeeName = UNASSIGNED;
            employeeLoginId = null; // 원하면 "-" 로 바꿔도 됨
        } else {
            employeeName = decryptEmployeeName(r.employeeCode(), r.employeeDekEnc(), r.employeeNameEnc());
            employeeName = MaskingUtils.maskName(employeeName);
        }

        return new InquiryListResponse(
                r.inquiryCode(),
                r.createdAt(),
                r.inquiryTitle(),
                r.inquiryStatus(),
                r.customerCode(),
                r.employeeCode(),
                employeeLoginId,
                employeeName,
                r.propertyCode(),
                r.inquiryCategoryCode(),
                r.inquiryCategoryName(),
                r.linkedIncidentCode(),
                customerName
        );
    }

    private String decryptCustomerName(Long customerCode, byte[] dekEnc, byte[] customerNameEnc) {
        if (customerCode == null || dekEnc == null || customerNameEnc == null) return null;
        return decryptionService.decrypt(customerCode, dekEnc, customerNameEnc);
    }

    private String decryptEmployeeName(Long employeeCode, byte[] employeeDekEnc, byte[] employeeNameEnc) {
        if (employeeCode == null || employeeDekEnc == null || employeeNameEnc == null) return null;
        return decryptionService.decrypt(employeeCode, employeeDekEnc, employeeNameEnc);
    }
}
