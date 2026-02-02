package com.gaekdam.gaekdambe.unit.reservation_service.reservation.query.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

import com.gaekdam.gaekdambe.global.crypto.DecryptionService;
import com.gaekdam.gaekdambe.global.crypto.SearchHashService;
import com.gaekdam.gaekdambe.global.crypto.HexUtils;
import com.gaekdam.gaekdambe.global.crypto.MaskingUtils;
import com.gaekdam.gaekdambe.reservation_service.reservation.query.mapper.OperationBoardMapper;
import com.gaekdam.gaekdambe.reservation_service.reservation.query.dto.request.OperationBoardSearchRequest;
import com.gaekdam.gaekdambe.reservation_service.reservation.query.dto.response.OperationBoardCryptoRow;
import com.gaekdam.gaekdambe.reservation_service.reservation.query.dto.response.OperationBoardResponse;
import com.gaekdam.gaekdambe.reservation_service.reservation.query.service.OperationBoardQueryService;
import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
import com.gaekdam.gaekdambe.global.paging.PageResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

class OperationBoardQueryServiceTest {

    @Mock
    OperationBoardMapper mapper;
    @Mock
    DecryptionService decryptionService;
    @Mock
    SearchHashService searchHashService;

    private OperationBoardQueryService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new OperationBoardQueryService(mapper, decryptionService, searchHashService);
    }

    @Test
    void findOperationBoard_appliesNameHash_and_decryptsName_and_masks() {
        PageRequest page = new PageRequest(); page.setPage(1); page.setSize(10);
        OperationBoardSearchRequest search = new OperationBoardSearchRequest();
        search.setCustomerName("Kim Sunghyun");

        OperationBoardCryptoRow row = new OperationBoardCryptoRow();
        try {
            java.lang.reflect.Field rc = OperationBoardCryptoRow.class.getDeclaredField("reservationCode"); rc.setAccessible(true); rc.set(row, 1L);
            java.lang.reflect.Field cc = OperationBoardCryptoRow.class.getDeclaredField("customerCode"); cc.setAccessible(true); cc.set(row, 11L);
            java.lang.reflect.Field enc = OperationBoardCryptoRow.class.getDeclaredField("customerNameEnc"); enc.setAccessible(true); enc.set(row, "enc".getBytes());
            java.lang.reflect.Field dek = OperationBoardCryptoRow.class.getDeclaredField("dekEnc"); dek.setAccessible(true); dek.set(row, "dek".getBytes());
            java.lang.reflect.Field pn = OperationBoardCryptoRow.class.getDeclaredField("propertyName"); pn.setAccessible(true); pn.set(row, "P");
            java.lang.reflect.Field rt = OperationBoardCryptoRow.class.getDeclaredField("roomType"); rt.setAccessible(true); rt.set(row, "R");
        } catch (Exception e) {
            // ignore
        }

        when(searchHashService.nameHash(org.mockito.ArgumentMatchers.anyString())).thenReturn(new byte[]{1,2,3});
        when(mapper.findOperationBoard(org.mockito.ArgumentMatchers.any(PageRequest.class), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(SortRequest.class))).thenReturn(List.of(row));
        when(mapper.countOperationBoard(org.mockito.ArgumentMatchers.any())).thenReturn(1L);
        when(decryptionService.decrypt(11L, "dek".getBytes(), "enc".getBytes())).thenReturn("Kim Sunghyun");

        PageResponse<OperationBoardResponse> res = service.findOperationBoard(page, search, new SortRequest());

        assertThat(res).isNotNull();
        assertThat(res.getContent()).hasSize(1);
        OperationBoardResponse out = res.getContent().get(0);
        assertThat(out.getCustomerName()).isEqualTo(MaskingUtils.maskName("Kim Sunghyun"));
    }
}
