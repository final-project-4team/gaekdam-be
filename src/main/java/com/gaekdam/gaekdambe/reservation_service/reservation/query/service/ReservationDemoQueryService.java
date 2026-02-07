package com.gaekdam.gaekdambe.reservation_service.reservation.query.service;


import com.gaekdam.gaekdambe.global.crypto.DecryptionService;
import com.gaekdam.gaekdambe.reservation_service.reservation.query.dto.response.DemoReservationCryptoRow;
import com.gaekdam.gaekdambe.reservation_service.reservation.query.dto.response.DemoReservationResponse;
import com.gaekdam.gaekdambe.reservation_service.reservation.query.mapper.ReservationQueryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReservationDemoQueryService {

    private final ReservationQueryMapper reservationQueryMapper;
    private final DecryptionService decryptionService;

    public DemoReservationResponse getDemoReservation(Long hotelGroupCode) {

        DemoReservationCryptoRow row =
                reservationQueryMapper.findOneReservationWithoutStay(hotelGroupCode);

        if (row == null) {
            return null;
        }

        String phone =
                decryptionService.decrypt(
                        row.getCustomerCode(),   // cache key
                        row.getDekEnc(),          // encrypted DEK
                        row.getPhoneEnc()         // encrypted phone
                );

        return new DemoReservationResponse(
                row.getReservationCode(),
                row.getReservationStatus(),
                phone
        );
    }

}
