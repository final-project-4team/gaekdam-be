package com.gaekdam.gaekdambe.reservation_service.reservation.query.mapper;

import com.gaekdam.gaekdambe.reservation_service.reservation.query.dto.response.detail.*;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ReservationDetailMapper {

    ReservationInfo findReservationInfo(Long reservationCode);

    CustomerCryptoRow findCustomerCrypto(Long reservationCode);

    RoomInfo findRoomInfo(Long reservationCode);

    StayInfo findStayInfo(Long reservationCode);

    CheckInOutInfo findCheckInOutInfo(Long reservationCode);

    List<FacilityUsageInfo> findFacilityUsageSummary(Long reservationCode);

    PackageInfo findPackageInfo(Long reservationCode);

    List<PackageFacilityInfo> findPackageFacilities(Long reservationCode);

    boolean existsMember(Long customerCode);

    CustomerContactCryptoRow findPrimaryPhone(Long customerCode);
}
