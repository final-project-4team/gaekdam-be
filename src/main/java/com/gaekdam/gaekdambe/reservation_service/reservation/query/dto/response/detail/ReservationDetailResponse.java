package com.gaekdam.gaekdambe.reservation_service.reservation.query.dto.response.detail;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ReservationDetailResponse {

    private ReservationInfo reservation;
    private CustomerInfo customer;
    private RoomInfo room;
    private StayInfo stay;
    private CheckInOutInfo checkInOut;
    private List<FacilityUsageInfo> facilityUsages;
    private PackageInfo packageInfo;
}
