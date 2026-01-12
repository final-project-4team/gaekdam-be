package com.gaekdam.gaekdambe.reservation_service.reservation.query.mapper;

import com.gaekdam.gaekdambe.reservation_service.reservation.query.dto.response.detail.*;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ReservationDetailMapper {

    ReservationInfo findReservationInfo(Long reservationCode);

    CustomerInfo findCustomerInfo(Long reservationCode);

    RoomInfo findRoomInfo(Long reservationCode);

    StayInfo findStayInfo(Long reservationCode);

    CheckInOutInfo findCheckInOutInfo(Long reservationCode);

    List<FacilityUsageInfo> findFacilityUsageSummary(Long reservationCode);
}
