package com.gaekdam.gaekdambe.unit.customer_service.customer.query.service;

import com.gaekdam.gaekdambe.customer_service.customer.query.dto.request.CustomerListSearchRequest;
import com.gaekdam.gaekdambe.customer_service.customer.query.dto.request.CustomerStatusHistoryRequest;
import com.gaekdam.gaekdambe.customer_service.customer.query.dto.response.*;
import com.gaekdam.gaekdambe.customer_service.customer.query.dto.response.item.CustomerListItem;
import com.gaekdam.gaekdambe.customer_service.customer.query.mapper.CustomerMapper;
import com.gaekdam.gaekdambe.customer_service.customer.query.service.CustomerQueryService;
import com.gaekdam.gaekdambe.customer_service.customer.query.service.assembler.CustomerResponseAssembler;
import com.gaekdam.gaekdambe.customer_service.customer.query.service.model.condition.CustomerListSearchParam;
import com.gaekdam.gaekdambe.customer_service.customer.query.service.model.row.*;
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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerQueryServiceTest {

    private static final String REASON = "UNIT_TEST_REASON";

    private CustomerMapper customerMapper;
    private CustomerResponseAssembler assembler;
    private SearchHashService searchHashService;
    private DecryptionService decryptionService;

    private CustomerQueryService service;

    @BeforeEach
    void setUp() {
        customerMapper = mock(CustomerMapper.class);
        assembler = mock(CustomerResponseAssembler.class);
        searchHashService = mock(SearchHashService.class);
        decryptionService = mock(DecryptionService.class);

        service = new CustomerQueryService(
                customerMapper,
                assembler,
                searchHashService,
                decryptionService
        );
    }

    @Test
    @DisplayName("getCustomerList: page/size 기본값 + sort 기본값/화이트리스트 + keyword(email) 분해 적용")
    void getCustomerList_defaults_and_keyword_email() {
        // given
        CustomerListSearchRequest req = new CustomerListSearchRequest();
        req.setHotelGroupCode(1L);
        req.setPage(0);          // default -> 1
        req.setSize(0);          // default -> 20
        req.setSortBy("NOT_ALLOWED");
        req.setDirection("xxx"); // default -> DESC
        req.setKeyword("test@gaekdam.com"); // email

        when(searchHashService.nameHash(any())).thenReturn(new byte[]{0x00, 0x0f}); // "000f"
        when(searchHashService.phoneHash(any())).thenReturn(new byte[]{0x01, 0x0a}); // "010a"
        when(searchHashService.emailHash(any())).thenReturn(new byte[]{0x0a, 0x0b}); // "0a0b"

        List<CustomerListRow> rows = List.of(mock(CustomerListRow.class));
        when(customerMapper.findCustomers(any(PageRequest.class), any(CustomerListSearchParam.class), any(SortRequest.class)))
                .thenReturn(rows);
        when(customerMapper.countCustomers(any(CustomerListSearchParam.class))).thenReturn(1L);

        when(assembler.toCustomerListItem(any(CustomerListRow.class))).thenReturn(mock(CustomerListItem.class));

        ArgumentCaptor<PageRequest> pageCap = ArgumentCaptor.forClass(PageRequest.class);
        ArgumentCaptor<SortRequest> sortCap = ArgumentCaptor.forClass(SortRequest.class);
        ArgumentCaptor<CustomerListSearchParam> searchCap = ArgumentCaptor.forClass(CustomerListSearchParam.class);

        // when
        PageResponse<CustomerListItem> res = service.getCustomerList(req);

        // then
        assertThat(res.getContent()).hasSize(1);
        assertThat(res.getPage()).isEqualTo(1);
        assertThat(res.getSize()).isEqualTo(20);
        assertThat(res.getTotalElements()).isEqualTo(1L);

        verify(customerMapper).findCustomers(pageCap.capture(), searchCap.capture(), sortCap.capture());

        assertThat(pageCap.getValue().getPage()).isEqualTo(1);
        assertThat(pageCap.getValue().getSize()).isEqualTo(20);

        assertThat(sortCap.getValue().getSortBy()).isEqualTo("created_at");
        assertThat(sortCap.getValue().getDirection()).isEqualTo("DESC");

        CustomerListSearchParam search = searchCap.getValue();
        assertThat(search.getHotelGroupCode()).isEqualTo(1L);
        assertThat(search.getCustomerCode()).isNull();
        assertThat(search.getEmailHash()).isEqualTo("0a0b");
    }

    @Test
    @DisplayName("getCustomerList: 상세조건이 하나라도 있으면 keyword는 무시된다(hasAnyDetailCondition 브랜치)")
    void getCustomerList_keyword_ignored_when_detail_condition_exists() {
        // given
        CustomerListSearchRequest req = new CustomerListSearchRequest();
        req.setHotelGroupCode(1L);
        req.setCustomerName("홍길동"); // 상세조건 존재
        req.setKeyword("010-1234-5678"); // phone로 분해될 수 있지만 무시되어야 함

        when(searchHashService.nameHash(any())).thenReturn(new byte[]{0x00, 0x01}); // "0001"
        when(searchHashService.phoneHash(any())).thenReturn(new byte[]{0x00, 0x02}); // "0002"
        when(searchHashService.emailHash(any())).thenReturn(new byte[]{0x00, 0x03}); // "0003"

        when(customerMapper.findCustomers(any(), any(), any())).thenReturn(List.of());
        when(customerMapper.countCustomers(any())).thenReturn(0L);

        ArgumentCaptor<CustomerListSearchParam> searchCap = ArgumentCaptor.forClass(CustomerListSearchParam.class);

        // when
        service.getCustomerList(req);

        // then
        verify(customerMapper).countCustomers(searchCap.capture());
        CustomerListSearchParam search = searchCap.getValue();

        assertThat(search.getCustomerCode()).isNull();
    }

    @Test
    @DisplayName("getCustomerList: keyword(phone) 분해 (숫자 8자리 이상) 브랜치")
    void getCustomerList_keyword_phone() {
        // given
        CustomerListSearchRequest req = new CustomerListSearchRequest();
        req.setHotelGroupCode(1L);
        req.setKeyword("010-1234-5678"); // phone

        when(searchHashService.nameHash(any())).thenReturn(new byte[]{0x00});
        when(searchHashService.phoneHash(any())).thenReturn(new byte[]{0x12, 0x0a}); // "120a"
        when(searchHashService.emailHash(any())).thenReturn(new byte[]{0x00});

        when(customerMapper.findCustomers(any(), any(), any())).thenReturn(List.of());
        when(customerMapper.countCustomers(any())).thenReturn(0L);

        ArgumentCaptor<CustomerListSearchParam> searchCap = ArgumentCaptor.forClass(CustomerListSearchParam.class);

        // when
        service.getCustomerList(req);

        // then
        verify(customerMapper).countCustomers(searchCap.capture());
        assertThat(searchCap.getValue().getPhoneHash()).isEqualTo("120a");
    }

    @Test
    @DisplayName("getCustomerList: keyword(customerCode) 분해 (숫자만) 브랜치")
    void getCustomerList_keyword_customerCode() {
        // given
        CustomerListSearchRequest req = new CustomerListSearchRequest();
        req.setHotelGroupCode(1L);
        req.setKeyword("12345"); // customerCode

        when(searchHashService.nameHash(any())).thenReturn(new byte[]{0x00});
        when(searchHashService.phoneHash(any())).thenReturn(new byte[]{0x00});
        when(searchHashService.emailHash(any())).thenReturn(new byte[]{0x00});

        when(customerMapper.findCustomers(any(), any(), any())).thenReturn(List.of());
        when(customerMapper.countCustomers(any())).thenReturn(0L);

        ArgumentCaptor<CustomerListSearchParam> searchCap = ArgumentCaptor.forClass(CustomerListSearchParam.class);

        // when
        service.getCustomerList(req);

        // then
        verify(customerMapper).countCustomers(searchCap.capture());
        assertThat(searchCap.getValue().getCustomerCode()).isEqualTo(12345L);
    }

    @Test
    @DisplayName("getCustomerList: keyword(name) 분해 (그 외) 브랜치")
    void getCustomerList_keyword_name() {
        // given
        CustomerListSearchRequest req = new CustomerListSearchRequest();
        req.setHotelGroupCode(1L);
        req.setKeyword("홍길동"); // name

        when(searchHashService.nameHash(any())).thenReturn(new byte[]{0x01, 0x02}); // "0102"
        when(searchHashService.phoneHash(any())).thenReturn(new byte[]{0x00});
        when(searchHashService.emailHash(any())).thenReturn(new byte[]{0x00});

        when(customerMapper.findCustomers(any(), any(), any())).thenReturn(List.of());
        when(customerMapper.countCustomers(any())).thenReturn(0L);

        ArgumentCaptor<CustomerListSearchParam> searchCap = ArgumentCaptor.forClass(CustomerListSearchParam.class);

        // when
        service.getCustomerList(req);

        // then
        verify(customerMapper).countCustomers(searchCap.capture());
        assertThat(searchCap.getValue().getCustomerNameHash()).isEqualTo("0102");
    }

    @Test
    @DisplayName("getCustomerDetail: row 없으면 INVALID_REQUEST")
    void getCustomerDetail_notFound_thenThrow() {
        // given
        when(customerMapper.findCustomerDetail(1L, 100L)).thenReturn(null);

        // when
        CustomException ex = catchThrowableOfType(
                () -> service.getCustomerDetail(1L, 100L, REASON),
                CustomException.class
        );

        // then
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.INVALID_REQUEST);
        assertThat(ex.getMessage()).contains("존재하지 않는 고객");
    }

    @Test
    @DisplayName("getCustomerDetail: row 있으면 assembler 호출")
    void getCustomerDetail_success() {
        // given
        CustomerDetailRow row = mock(CustomerDetailRow.class);
        when(customerMapper.findCustomerDetail(1L, 100L)).thenReturn(row);
        when(customerMapper.findCustomerContacts(1L, 100L))
                .thenReturn(List.of(mock(CustomerContactRow.class)));

        CustomerDetailResponse expected = mock(CustomerDetailResponse.class);
        when(assembler.toCustomerDetailResponse(any(), any())).thenReturn(expected);

        // when
        CustomerDetailResponse res = service.getCustomerDetail(1L, 100L, REASON);

        // then
        assertThat(res).isSameAs(expected);
        verify(assembler).toCustomerDetailResponse(eq(row), anyList());
    }

    @Test
    @DisplayName("getCustomerStatus: row 없으면 INVALID_REQUEST")
    void getCustomerStatus_notFound_thenThrow() {
        // given
        when(customerMapper.findCustomerStatus(1L, 100L)).thenReturn(null);

        // when
        CustomException ex = catchThrowableOfType(
                () -> service.getCustomerStatus(1L, 100L),
                CustomException.class
        );

        // then
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.INVALID_REQUEST);
        assertThat(ex.getMessage()).contains("존재하지 않는 고객");
    }

    @Test
    @DisplayName("getCustomerStatus: row 있으면 assembler 호출")
    void getCustomerStatus_success() {
        // given
        CustomerStatusRow row = mock(CustomerStatusRow.class);
        when(customerMapper.findCustomerStatus(1L, 100L)).thenReturn(row);

        CustomerStatusResponse expected = mock(CustomerStatusResponse.class);
        when(assembler.toCustomerStatusResponse(row)).thenReturn(expected);

        // when
        CustomerStatusResponse res = service.getCustomerStatus(1L, 100L);

        // then
        assertThat(res).isSameAs(expected);
    }

    @Test
    @DisplayName("getCustomerStatusHistories: page/size default + sort default/화이트리스트")
    void getCustomerStatusHistories_defaults_and_sort_whitelist() {
        // given
        CustomerStatusHistoryRequest req = new CustomerStatusHistoryRequest();
        req.setPage(0);
        req.setSize(0);
        req.setSortBy("NOT_ALLOWED");
        req.setDirection("aaa");

        when(customerMapper.findCustomerStatusHistories(anyLong(), anyLong(), any(PageRequest.class), any(SortRequest.class)))
                .thenReturn(List.of(mock(CustomerStatusHistoryRow.class)));
        when(customerMapper.countCustomerStatusHistories(1L, 100L)).thenReturn(1L);

        CustomerStatusHistoryResponse expected = mock(CustomerStatusHistoryResponse.class);
        when(assembler.toCustomerStatusHistoryResponse(anyList(), anyInt(), anyInt(), anyLong()))
                .thenReturn(expected);

        ArgumentCaptor<PageRequest> pageCap = ArgumentCaptor.forClass(PageRequest.class);
        ArgumentCaptor<SortRequest> sortCap = ArgumentCaptor.forClass(SortRequest.class);

        // when
        CustomerStatusHistoryResponse res = service.getCustomerStatusHistories(1L, 100L, req);

        // then
        assertThat(res).isSameAs(expected);

        verify(customerMapper).findCustomerStatusHistories(eq(1L), eq(100L), pageCap.capture(), sortCap.capture());

        assertThat(pageCap.getValue().getPage()).isEqualTo(1);
        assertThat(pageCap.getValue().getSize()).isEqualTo(20);

        assertThat(sortCap.getValue().getSortBy()).isEqualTo("changed_at");
        assertThat(sortCap.getValue().getDirection()).isEqualTo("DESC");
    }

    @Test
    @DisplayName("getCustomerMarketingConsents: mapper rows -> assembler 호출")
    void getCustomerMarketingConsents_success() {
        // given
        List<CustomerContactRow> rows = List.of(mock(CustomerContactRow.class));
        when(customerMapper.findCustomerMarketingConsents(1L, 100L)).thenReturn(rows);

        CustomerMarketingConsentResponse expected = mock(CustomerMarketingConsentResponse.class);
        when(assembler.toCustomerMarketingConsentResponse(100L, rows)).thenReturn(expected);

        // when
        CustomerMarketingConsentResponse res = service.getCustomerMarketingConsents(1L, 100L);

        // then
        assertThat(res).isSameAs(expected);
        verify(assembler).toCustomerMarketingConsentResponse(100L, rows);
    }

    @Test
    @DisplayName("getCustomerBasic: row 없으면 INVALID_REQUEST")
    void getCustomerBasic_notFound_thenThrow() {
        // given
        when(customerMapper.findCustomerBasic(1L, 100L)).thenReturn(null);

        // when
        CustomException ex = catchThrowableOfType(
                () -> service.getCustomerBasic(1L, 100L, REASON),
                CustomException.class
        );

        // then
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.INVALID_REQUEST);
        assertThat(ex.getMessage()).contains("존재하지 않는 고객");
    }

    @Test
    @DisplayName("getCustomerBasic: enc+dek 있으면 복호화 호출")
    void getCustomerBasic_decrypt_name_and_phone() {
        // given
        CustomerBasicRow row = mock(CustomerBasicRow.class);

        byte[] dek = "dek".getBytes();
        byte[] nameEnc = "nameEnc".getBytes();
        byte[] phoneEnc = "phoneEnc".getBytes();

        when(row.getCustomerCode()).thenReturn(100L);
        when(row.getDekEnc()).thenReturn(dek);
        when(row.getCustomerNameEnc()).thenReturn(nameEnc);
        when(row.getPhoneEnc()).thenReturn(phoneEnc);

        when(customerMapper.findCustomerBasic(1L, 100L)).thenReturn(row);

        when(decryptionService.decrypt(100L, dek, nameEnc)).thenReturn("홍길동");
        when(decryptionService.decrypt(100L, dek, phoneEnc)).thenReturn("01012345678");

        // when
        CustomerBasicResponse res = service.getCustomerBasic(1L, 100L, REASON);

        // then
        assertThat(res.getCustomerCode()).isEqualTo(100L);
        assertThat(res.getCustomerName()).isEqualTo("홍길동");
        assertThat(res.getPhoneNumber()).isEqualTo("01012345678");

        verify(decryptionService).decrypt(100L, dek, nameEnc);
        verify(decryptionService).decrypt(100L, dek, phoneEnc);
    }

    @Test
    @DisplayName("getCustomerBasic: enc 또는 dek 없으면 복호화 스킵(null 반환) 브랜치")
    void getCustomerBasic_decrypt_skip_when_missing() {
        // given
        CustomerBasicRow row = mock(CustomerBasicRow.class);

        when(row.getCustomerCode()).thenReturn(100L);
        when(row.getDekEnc()).thenReturn(null);
        when(row.getCustomerNameEnc()).thenReturn("nameEnc".getBytes());
        when(row.getPhoneEnc()).thenReturn(null);

        when(customerMapper.findCustomerBasic(1L, 100L)).thenReturn(row);

        // when
        CustomerBasicResponse res = service.getCustomerBasic(1L, 100L, REASON);

        // then
        assertThat(res.getCustomerName()).isNull();
        assertThat(res.getPhoneNumber()).isNull();
        verifyNoInteractions(decryptionService);
    }
}
