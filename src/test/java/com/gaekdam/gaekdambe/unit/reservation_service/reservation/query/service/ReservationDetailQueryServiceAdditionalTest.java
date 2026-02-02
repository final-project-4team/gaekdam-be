package com.gaekdam.gaekdambe.unit.reservation_service.reservation.query.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.gaekdam.gaekdambe.reservation_service.reservation.query.service.ReservationDetailQueryService;
import com.gaekdam.gaekdambe.reservation_service.reservation.query.mapper.ReservationDetailMapper;
import com.gaekdam.gaekdambe.global.crypto.DecryptionService;
import com.gaekdam.gaekdambe.reservation_service.reservation.query.dto.response.detail.CustomerCryptoRow;
import com.gaekdam.gaekdambe.reservation_service.reservation.query.dto.response.detail.CustomerContactCryptoRow;
import com.gaekdam.gaekdambe.reservation_service.reservation.query.dto.response.detail.PackageFacilityInfo;
import com.gaekdam.gaekdambe.reservation_service.reservation.query.dto.response.detail.PackageInfo;
import com.gaekdam.gaekdambe.reservation_service.reservation.query.dto.response.detail.ReservationDetailResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.List;

class ReservationDetailQueryServiceAdditionalTest {

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
    void getReservationDetail_handles_package_with_facilities_and_no_phone() {
        Long reservationCode = 200L;

        CustomerCryptoRow row = new CustomerCryptoRow();
        try {
            java.lang.reflect.Field f = CustomerCryptoRow.class.getDeclaredField("customerCode");
            f.setAccessible(true);
            f.set(row, 77L);

            java.lang.reflect.Field dek = CustomerCryptoRow.class.getDeclaredField("dekEnc");
            dek.setAccessible(true);
            dek.set(row, "dekval".getBytes());

            java.lang.reflect.Field enc = CustomerCryptoRow.class.getDeclaredField("customerNameEnc");
            enc.setAccessible(true);
            enc.set(row, "abc".getBytes());

            java.lang.reflect.Field nat = CustomerCryptoRow.class.getDeclaredField("nationalityType");
            nat.setAccessible(true);
            nat.set(row, "KOR");

            java.lang.reflect.Field contract = CustomerCryptoRow.class.getDeclaredField("contractType");
            contract.setAccessible(true);
            contract.set(row, "CONTRACT");

        } catch (Exception e) { }

        when(mapper.findCustomerCrypto(reservationCode)).thenReturn(row);
        when(decryptionService.decrypt(77L, "dekval".getBytes(), "abc".getBytes())).thenReturn("Kim");
        when(mapper.existsMember(77L)).thenReturn(false);

        // primary phone 없음 (null)
        when(mapper.findPrimaryPhone(77L)).thenReturn((CustomerContactCryptoRow) null);

        // package info present with facilities
        PackageInfo pkg = new PackageInfo();
        try {
            java.lang.reflect.Field name = PackageInfo.class.getDeclaredField("packageName");
            name.setAccessible(true);
            name.set(pkg, "StayPack");

            java.lang.reflect.Field content = PackageInfo.class.getDeclaredField("packageContent");
            content.setAccessible(true);
            content.set(pkg, "desc");

            java.lang.reflect.Field price = PackageInfo.class.getDeclaredField("packagePrice");
            price.setAccessible(true);
            price.set(pkg, new BigDecimal("999.99"));
        } catch (Exception e) { }

        when(mapper.findPackageInfo(reservationCode)).thenReturn(pkg);
        when(mapper.findPackageFacilities(reservationCode)).thenReturn(List.of(new PackageFacilityInfo("Gym", 1)));

        when(mapper.findReservationInfo(reservationCode)).thenReturn(null);
        when(mapper.findRoomInfo(reservationCode)).thenReturn(null);
        when(mapper.findStayInfo(reservationCode)).thenReturn(null);
        when(mapper.findCheckInOutInfo(reservationCode)).thenReturn(null);
        when(mapper.findFacilityUsageSummary(reservationCode)).thenReturn(List.of());

        ReservationDetailResponse res = service.getReservationDetail(reservationCode, "x");

        assertThat(res.getCustomer().getCustomerName()).isEqualTo("Kim");
        assertThat(res.getCustomer().getPhoneNumber()).isNull();
        assertThat(res.getPackageInfo()).isNotNull();
        assertThat(res.getPackageInfo().getFacilities()).hasSize(1);
    }

    @Test
    void getReservationDetail_handles_missing_customer_row_gracefully() {
        Long reservationCode = 300L;
        // mapper.findCustomerCrypto가 null을 반환하는 상황 처리 검증
        when(mapper.findCustomerCrypto(reservationCode)).thenReturn(null);

        // IllegalArgumentException이 발생해야 함
        try {
            service.getReservationDetail(reservationCode, "x");
        } catch (Exception e) {
            assertThat(e).isInstanceOf(IllegalArgumentException.class);
            return;
        }
        throw new AssertionError("Expected IllegalArgumentException when customer crypto row is missing");
    }
}
