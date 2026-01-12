package com.gaekdam.gaekdambe.reservation_service.reservation.query.service;

import com.gaekdam.gaekdambe.global.crypto.DecryptionService;
import com.gaekdam.gaekdambe.reservation_service.reservation.query.dto.response.detail.CustomerInfo;
import com.gaekdam.gaekdambe.reservation_service.reservation.query.dto.response.detail.ReservationDetailResponse;
import com.gaekdam.gaekdambe.reservation_service.reservation.query.mapper.ReservationDetailMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReservationDetailQueryService {

    private final ReservationDetailMapper mapper;
    private final DecryptionService decryptionService;

    public ReservationDetailResponse getReservationDetail(Long reservationCode) {

        CustomerInfo customer = mapper.findCustomerInfo(reservationCode);

//    // KMS / CryptoService 등으로 복호화
//        String decryptedName =decryptionService.decrypt(
//                customer.getCustomerNameEnc(),
//                customer.getCustomerNameHash()
//        );
//
//        customer.setCustomerName(decryptedName);

        return ReservationDetailResponse.builder()
                .reservation(mapper.findReservationInfo(reservationCode))
                .customer(customer)
                .room(mapper.findRoomInfo(reservationCode))
                .stay(mapper.findStayInfo(reservationCode))
                .checkInOut(mapper.findCheckInOutInfo(reservationCode))
                .facilityUsages(mapper.findFacilityUsageSummary(reservationCode))
                .build();
    }
}
