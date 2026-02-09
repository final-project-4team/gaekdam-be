package com.gaekdam.gaekdambe.unit.reservation_service.reservation.query.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.gaekdam.gaekdambe.global.crypto.DecryptionService;
import com.gaekdam.gaekdambe.global.crypto.MaskingUtils;
import com.gaekdam.gaekdambe.global.crypto.SearchHashService;
import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
import com.gaekdam.gaekdambe.reservation_service.reservation.query.dto.response.OperationBoardCryptoRow;
import com.gaekdam.gaekdambe.reservation_service.reservation.query.dto.response.OperationBoardResponse;
import com.gaekdam.gaekdambe.reservation_service.reservation.query.mapper.TodayOperationMapper;
import com.gaekdam.gaekdambe.reservation_service.reservation.query.service.TodayOperationQueryService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

class TodayOperationQueryServiceTest {

    @Mock
    TodayOperationMapper mapper;

    @Mock
    DecryptionService decryptionService;

    @Mock
    SearchHashService searchHashService;

    private TodayOperationQueryService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new TodayOperationQueryService(
                mapper,
                decryptionService,
                searchHashService
        );
    }

    @Test
    void findTodayOperations_decrypts_and_masks() {

        // given
        PageRequest page = new PageRequest();
        page.setPage(1);
        page.setSize(10);

        SortRequest sort = new SortRequest();

        LocalDate today = LocalDate.now();
        LocalDate startDate = today;
        LocalDate endDate = today.plusDays(1);

        OperationBoardCryptoRow row = new OperationBoardCryptoRow();
        try {
            var rc = OperationBoardCryptoRow.class.getDeclaredField("reservationCode");
            rc.setAccessible(true);
            rc.set(row, 1L);

            var sc = OperationBoardCryptoRow.class.getDeclaredField("stayCode");
            sc.setAccessible(true);
            sc.set(row, null);

            var cc = OperationBoardCryptoRow.class.getDeclaredField("customerCode");
            cc.setAccessible(true);
            cc.set(row, 11L);

            var enc = OperationBoardCryptoRow.class.getDeclaredField("customerNameEnc");
            enc.setAccessible(true);
            enc.set(row, "enc".getBytes());

            var dek = OperationBoardCryptoRow.class.getDeclaredField("dekEnc");
            dek.setAccessible(true);
            dek.set(row, "dek".getBytes());

            var pn = OperationBoardCryptoRow.class.getDeclaredField("propertyName");
            pn.setAccessible(true);
            pn.set(row, "P");

            var rt = OperationBoardCryptoRow.class.getDeclaredField("roomType");
            rt.setAccessible(true);
            rt.set(row, "R");
        } catch (Exception ignored) {
        }

        when(searchHashService.nameHash(org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(new byte[]{1, 2, 3});

        when(mapper.findTodayOperations(
                org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(PageRequest.class),
                org.mockito.ArgumentMatchers.eq(startDate),
                org.mockito.ArgumentMatchers.eq(endDate),
                org.mockito.ArgumentMatchers.any(SortRequest.class)
        )).thenReturn(List.of(row));

        when(mapper.countTodayOperationsByStatus(
                org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.eq(startDate),
                org.mockito.ArgumentMatchers.eq(endDate)
        )).thenReturn(
                List.of(Map.of("operationStatus", "CHECKIN_PLANNED", "cnt", 2))
        );

        when(decryptionService.decrypt(
                11L,
                "dek".getBytes(),
                "enc".getBytes()
        )).thenReturn("Kim K");

        // when
        var res = service.findTodayOperations(
                page,
                1L,
                1L,
                null,
                null,
                null,
                sort
        );

        // then
        assertThat(res.getContent()).hasSize(1);

        OperationBoardResponse out = res.getContent().get(0);
        assertThat(out.getCustomerName())
                .isEqualTo(MaskingUtils.maskName("Kim K"));
    }
}
