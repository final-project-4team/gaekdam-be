package com.gaekdam.gaekdambe.unit.communication_service.inquiry.query.service;

import com.gaekdam.gaekdambe.communication_service.inquiry.query.dto.request.InquiryListSearchRequest;
import com.gaekdam.gaekdambe.communication_service.inquiry.query.dto.response.InquiryDetailResponse;
import com.gaekdam.gaekdambe.communication_service.inquiry.query.dto.response.InquiryListResponse;
import com.gaekdam.gaekdambe.communication_service.inquiry.query.mapper.InquiryMapper;
import com.gaekdam.gaekdambe.communication_service.inquiry.query.service.InquiryQueryService;
import com.gaekdam.gaekdambe.communication_service.inquiry.query.service.model.InquiryDetailRow;
import com.gaekdam.gaekdambe.communication_service.inquiry.query.service.model.InquiryListRow;
import com.gaekdam.gaekdambe.global.crypto.DecryptionService;
import com.gaekdam.gaekdambe.global.crypto.SearchHashService;
import com.gaekdam.gaekdambe.global.exception.CustomException;
import com.gaekdam.gaekdambe.global.exception.ErrorCode;
import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.PageResponse;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class InquiryQueryServiceTest {

    private InquiryMapper inquiryMapper;
    private DecryptionService decryptionService;
    private SearchHashService searchHashService;
    private InquiryQueryService service;

    @BeforeEach
    void setUp() {
        inquiryMapper = mock(InquiryMapper.class);
        decryptionService = mock(DecryptionService.class);
        searchHashService = mock(SearchHashService.class);
        service = new InquiryQueryService(inquiryMapper, decryptionService, searchHashService);
    }

    @Test
    @DisplayName("getInquiries: mapper rows를 decrypt해서 PageResponse로 반환")
    void getInquiries_success() {
        // given
        PageRequest page = new PageRequest();
        page.setPage(2);
        page.setSize(10);

        InquiryListSearchRequest search = new InquiryListSearchRequest();

        SortRequest sort = new SortRequest();
        sort.setSortBy("created_at");
        sort.setDirection("DESC");

        byte[] customerDekEnc = new byte[]{1};
        byte[] customerNameEnc = new byte[]{2};
        byte[] employeeDekEnc = new byte[]{3};
        byte[] employeeNameEnc = new byte[]{4};

        InquiryListRow row = new InquiryListRow(
                101L,
                LocalDateTime.of(2026, 1, 1, 10, 0),
                "문의제목",
                "IN_PROGRESS",
                9001L,
                7001L,
                "emp01",
                employeeNameEnc,
                employeeDekEnc,
                3001L,
                4001L,
                "카테고리",
                5001L,
                customerNameEnc,
                customerDekEnc
        );

        when(inquiryMapper.findInquiries(page, search, sort)).thenReturn(List.of(row));
        when(inquiryMapper.countInquiries(search)).thenReturn(11L);

        when(decryptionService.decrypt(9001L, customerDekEnc, customerNameEnc)).thenReturn("고객명복호화");
        when(decryptionService.decrypt(7001L, employeeDekEnc, employeeNameEnc)).thenReturn("직원명복호화");

        // when
        PageResponse<InquiryListResponse> res = service.getInquiries(page, search, sort);

        // then
        assertThat(res).isNotNull();
        assertThat(res.getPage()).isEqualTo(2);
        assertThat(res.getSize()).isEqualTo(10);
        assertThat(res.getTotalElements()).isEqualTo(11L);
        assertThat(res.getContent()).hasSize(1);

        InquiryListResponse dto = res.getContent().get(0);
        assertThat(dto.getInquiryCode()).isEqualTo(101L);
        assertThat(dto.getInquiryTitle()).isEqualTo("문의제목");
        assertThat(dto.getInquiryStatus()).isEqualTo("IN_PROGRESS");
        assertThat(dto.getCustomerCode()).isEqualTo(9001L);
        assertThat(dto.getEmployeeCode()).isEqualTo(7001L);
        assertThat(dto.getEmployeeLoginId()).isEqualTo("emp01");

        assertThat(dto.getEmployeeName())
                .isNotNull()
                .startsWith("직")
                .endsWith("화")
                .contains("****");

        assertThat(dto.getPropertyCode()).isEqualTo(3001L);
        assertThat(dto.getInquiryCategoryCode()).isEqualTo(4001L);
        assertThat(dto.getInquiryCategoryName()).isEqualTo("카테고리");
        assertThat(dto.getLinkedIncidentCode()).isEqualTo(5001L);

        assertThat(dto.getCustomerName())
                .isNotNull()
                .startsWith("고")
                .endsWith("화")
                .contains("****");

        verify(inquiryMapper).findInquiries(page, search, sort);
        verify(inquiryMapper).countInquiries(search);
        verify(decryptionService).decrypt(9001L, customerDekEnc, customerNameEnc);
        verify(decryptionService).decrypt(7001L, employeeDekEnc, employeeNameEnc);
        verifyNoMoreInteractions(inquiryMapper, decryptionService, searchHashService);
    }

    @Test
    @DisplayName("getInquiryDetail: row가 null이면 INVALID_REQUEST")
    void getInquiryDetail_notFound_thenThrow() {
        // given
        Long hotelGroupCode = 1L;
        Long inquiryCode = 10L;

        when(inquiryMapper.findInquiryDetail(hotelGroupCode, inquiryCode)).thenReturn(null);

        // when
        CustomException ex = catchThrowableOfType(
                () -> service.getInquiryDetail(hotelGroupCode, inquiryCode),
                CustomException.class
        );

        // then
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.INVALID_REQUEST);
        assertThat(ex.getMessage()).contains("존재하지 않는 문의입니다.");

        verify(inquiryMapper).findInquiryDetail(hotelGroupCode, inquiryCode);
        verifyNoMoreInteractions(inquiryMapper);
        verifyNoInteractions(decryptionService, searchHashService);
    }

    @Test
    @DisplayName("getInquiryDetail: 정상 -> customer/employee 복호화 후 DetailResponse 반환")
    void getInquiryDetail_success() {
        // given
        Long hotelGroupCode = 1L;
        Long inquiryCode = 10L;

        byte[] customerDekEnc = new byte[]{1};
        byte[] customerNameEnc = new byte[]{2};
        byte[] employeeDekEnc = new byte[]{3};
        byte[] employeeNameEnc = new byte[]{4};

        InquiryDetailRow row = new InquiryDetailRow(
                inquiryCode,
                "ANSWERED",
                "상세제목",
                "상세내용",
                "답변내용",
                LocalDateTime.of(2026, 1, 2, 10, 0),
                LocalDateTime.of(2026, 1, 3, 11, 0),
                9001L,
                7001L,
                "emp01",
                employeeNameEnc,
                employeeDekEnc,
                3001L,
                4001L,
                "카테고리",
                5001L,
                customerNameEnc,
                customerDekEnc
        );

        when(inquiryMapper.findInquiryDetail(hotelGroupCode, inquiryCode)).thenReturn(row);

        when(decryptionService.decrypt(9001L, customerDekEnc, customerNameEnc)).thenReturn("고객명복호화");
        when(decryptionService.decrypt(7001L, employeeDekEnc, employeeNameEnc)).thenReturn("직원명복호화");

        // when
        InquiryDetailResponse res = service.getInquiryDetail(hotelGroupCode, inquiryCode);

        // then
        assertThat(res).isNotNull();
        assertThat(res.getInquiryCode()).isEqualTo(inquiryCode);
        assertThat(res.getInquiryStatus()).isEqualTo("ANSWERED");
        assertThat(res.getInquiryTitle()).isEqualTo("상세제목");
        assertThat(res.getInquiryContent()).isEqualTo("상세내용");
        assertThat(res.getAnswerContent()).isEqualTo("답변내용");
        assertThat(res.getCustomerCode()).isEqualTo(9001L);
        assertThat(res.getEmployeeCode()).isEqualTo(7001L);
        assertThat(res.getEmployeeLoginId()).isEqualTo("emp01");
        assertThat(res.getEmployeeName()).isEqualTo("직원명복호화");
        assertThat(res.getPropertyCode()).isEqualTo(3001L);
        assertThat(res.getInquiryCategoryCode()).isEqualTo(4001L);
        assertThat(res.getInquiryCategoryName()).isEqualTo("카테고리");
        assertThat(res.getLinkedIncidentCode()).isEqualTo(5001L);
        assertThat(res.getCustomerName()).isEqualTo("고객명복호화");

        verify(inquiryMapper).findInquiryDetail(hotelGroupCode, inquiryCode);
        verify(decryptionService).decrypt(9001L, customerDekEnc, customerNameEnc);
        verify(decryptionService).decrypt(7001L, employeeDekEnc, employeeNameEnc);
        verifyNoMoreInteractions(inquiryMapper, decryptionService, searchHashService);
    }

    @Test
    @DisplayName("getInquiryDetail: customerNameEnc 또는 dekEnc null이면 고객명 decrypt 호출 안 함")
    void getInquiryDetail_customerDecryptSkipped_whenNull() {
        // given
        Long hotelGroupCode = 1L;
        Long inquiryCode = 10L;

        byte[] employeeDekEnc = new byte[]{3};
        byte[] employeeNameEnc = new byte[]{4};

        InquiryDetailRow row = new InquiryDetailRow(
                inquiryCode,
                "IN_PROGRESS",
                "상세제목",
                "상세내용",
                null,
                LocalDateTime.of(2026, 1, 2, 10, 0),
                null,
                9001L,
                7001L,
                "emp01",
                employeeNameEnc,
                employeeDekEnc,
                3001L,
                4001L,
                "카테고리",
                null,
                null,
                null
        );

        when(inquiryMapper.findInquiryDetail(hotelGroupCode, inquiryCode)).thenReturn(row);
        when(decryptionService.decrypt(7001L, employeeDekEnc, employeeNameEnc)).thenReturn("직원명복호화");

        // when
        InquiryDetailResponse res = service.getInquiryDetail(hotelGroupCode, inquiryCode);

        // then
        assertThat(res.getCustomerName()).isNull();
        assertThat(res.getEmployeeName()).isEqualTo("직원명복호화");

        verify(inquiryMapper).findInquiryDetail(hotelGroupCode, inquiryCode);
        verify(decryptionService).decrypt(7001L, employeeDekEnc, employeeNameEnc);
        verifyNoMoreInteractions(inquiryMapper, decryptionService, searchHashService);
    }
}
