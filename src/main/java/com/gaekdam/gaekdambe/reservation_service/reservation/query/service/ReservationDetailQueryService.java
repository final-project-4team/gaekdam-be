package com.gaekdam.gaekdambe.reservation_service.reservation.query.service;

import com.gaekdam.gaekdambe.global.crypto.DecryptionService;
import com.gaekdam.gaekdambe.reservation_service.reservation.query.dto.response.detail.CustomerCryptoRow;
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

        CustomerCryptoRow row = mapper.findCustomerCrypto(reservationCode);

        String decryptedName = decryptionService.decrypt(
                row.getCustomerCode(),
                row.getDekEnc(),
                row.getCustomerNameEnc()
        );

        CustomerInfo customer = CustomerInfo.builder()
                .customerCode(row.getCustomerCode())
                .customerName(decryptedName)
                .nationalityType(row.getNationalityType())
                .contractType(row.getContractType())
                .customerStatus(row.getCustomerStatus())
                .build();

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
