package com.gaekdam.gaekdambe.unit.reservation_service.reservation.query.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.gaekdam.gaekdambe.reservation_service.reservation.query.service.ReservationDetailQueryService;
import com.gaekdam.gaekdambe.reservation_service.reservation.query.mapper.ReservationDetailMapper;
import com.gaekdam.gaekdambe.global.crypto.DecryptionService;
import com.gaekdam.gaekdambe.reservation_service.reservation.query.dto.response.detail.CustomerCryptoRow;
import com.gaekdam.gaekdambe.reservation_service.reservation.query.dto.response.detail.CustomerContactCryptoRow;
import com.gaekdam.gaekdambe.reservation_service.reservation.query.dto.response.detail.CustomerInfo;
import com.gaekdam.gaekdambe.reservation_service.reservation.query.dto.response.detail.ReservationDetailResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Collections;

class ReservationDetailQueryServiceTest {

    @Mock
    ReservationDetailMapper mapper;
    @Mock
    DecryptionService decryptionService;

    private ReservationDetailQueryService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new ReservationDetailQueryService(mapper, decryptionService);
    }

    @Test
    void getReservationDetail_decryptsNames_and_buildsResponse() {
        Long reservationCode = 101L;

        CustomerCryptoRow row = new CustomerCryptoRow();
        // reflection으로 private 필드 채우기
        try {
            java.lang.reflect.Field f = CustomerCryptoRow.class.getDeclaredField("customerCode");
            f.setAccessible(true);
            f.set(row, 55L);

            java.lang.reflect.Field enc = CustomerCryptoRow.class.getDeclaredField("customerNameEnc");
            enc.setAccessible(true);
            enc.set(row, "encname".getBytes());

            java.lang.reflect.Field dek = CustomerCryptoRow.class.getDeclaredField("dekEnc");
            dek.setAccessible(true);
            dek.set(row, "dek".getBytes());

            java.lang.reflect.Field hash = CustomerCryptoRow.class.getDeclaredField("customerNameHash");
            hash.setAccessible(true);
            hash.set(row, "h");

            java.lang.reflect.Field nat = CustomerCryptoRow.class.getDeclaredField("nationalityType");
            nat.setAccessible(true);
            nat.set(row, "KOR");

            java.lang.reflect.Field contract = CustomerCryptoRow.class.getDeclaredField("contractType");
            contract.setAccessible(true);
            contract.set(row, "CONTRACT");

            java.lang.reflect.Field status = CustomerCryptoRow.class.getDeclaredField("customerStatus");
            status.setAccessible(true);
            status.set(row, "ACTIVE");
        } catch (Exception e) {
            // ignore
        }

        when(mapper.findCustomerCrypto(reservationCode)).thenReturn(row);
        when(decryptionService.decrypt(55L, "dek".getBytes(), "encname".getBytes())).thenReturn("John Doe");
        when(mapper.existsMember(55L)).thenReturn(true);

        CustomerContactCryptoRow phone = new CustomerContactCryptoRow();
        try {
            java.lang.reflect.Field fv = CustomerContactCryptoRow.class.getDeclaredField("contactValueEnc");
            fv.setAccessible(true);
            fv.set(phone, "0101234".getBytes());

            java.lang.reflect.Field fc = CustomerContactCryptoRow.class.getDeclaredField("customerCode");
            fc.setAccessible(true);
            fc.set(phone, 55L);
        } catch (Exception e) {}

        when(mapper.findPrimaryPhone(55L)).thenReturn(phone);
        when(decryptionService.decrypt(55L, "dek".getBytes(), "0101234".getBytes())).thenReturn("010-1234-5678");

        when(mapper.findPackageInfo(reservationCode)).thenReturn(null);
        when(mapper.findReservationInfo(reservationCode)).thenReturn(null);
        when(mapper.findRoomInfo(reservationCode)).thenReturn(null);
        when(mapper.findStayInfo(reservationCode)).thenReturn(null);
        when(mapper.findCheckInOutInfo(reservationCode)).thenReturn(null);
        when(mapper.findFacilityUsageSummary(reservationCode)).thenReturn(Collections.emptyList());

        ReservationDetailResponse res = service.getReservationDetail(reservationCode, "test");

        assertThat(res.getCustomer()).isNotNull();
        CustomerInfo ci = res.getCustomer();
        assertThat(ci.getCustomerName()).isEqualTo("John Doe");
        assertThat(ci.getIsMember()).isTrue();
        assertThat(ci.getPhoneNumber()).isEqualTo("010-1234-5678");
    }
}
