package com.gaekdam.gaekdambe.reservation_service.reservation.command.application.dto.request;

import com.gaekdam.gaekdambe.reservation_service.reservation.command.domain.enums.GuestType;
import com.gaekdam.gaekdambe.reservation_service.reservation.command.domain.enums.ReservationChannel;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
public class ReservationCreateRequest {

    private LocalDate checkinDate;
    private LocalDate checkoutDate;

    private int guestCount;
    private GuestType guestType;

    private ReservationChannel reservationChannel;
    private String requestNote;

    private BigDecimal reservationRoomPrice;
    private BigDecimal reservationPackagePrice;

    private Long propertyCode;
    private Long roomCode;
    private Long customerCode;
    private Long packageCode; // nullable
}
