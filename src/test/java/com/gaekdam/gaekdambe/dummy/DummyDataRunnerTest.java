package com.gaekdam.gaekdambe.dummy;

import com.gaekdam.gaekdambe.dummy.generate.customer_service.customer.DummyCustomerDataTest;
import com.gaekdam.gaekdambe.dummy.generate.customer_service.loyalty.DummyLoyaltyDataTest;
import com.gaekdam.gaekdambe.dummy.generate.customer_service.membership.DummyMembershipDataTest;
import com.gaekdam.gaekdambe.dummy.generate.hotel_service.department.DummyDepartmentDataTest;
import com.gaekdam.gaekdambe.dummy.generate.hotel_service.hotel.hotel_group.DummyHotelGroupDataTest;
import com.gaekdam.gaekdambe.dummy.generate.hotel_service.hotel.property.DummyPropertyDataTest;
import com.gaekdam.gaekdambe.dummy.generate.hotel_service.position.DummyPositionDataTest;
import com.gaekdam.gaekdambe.dummy.generate.iam_service.permission.DummyPermissionDataTest;
import com.gaekdam.gaekdambe.dummy.generate.iam_service.permissionType.DummyPermissionTypeDataTest;
import com.gaekdam.gaekdambe.dummy.generate.operation_service.facility.DummyFacilityDataTest;
import com.gaekdam.gaekdambe.dummy.generate.operation_service.facility.DummyFacilityUsageDataTest;
import com.gaekdam.gaekdambe.dummy.generate.operation_service.facility.DummyReservationPackageDataTest;
import com.gaekdam.gaekdambe.dummy.generate.operation_service.room.DummyRoomDataTest;
import com.gaekdam.gaekdambe.dummy.generate.operation_service.room.DummyRoomTypeDataTest;
import com.gaekdam.gaekdambe.dummy.generate.reservation_service.reservation.DummyReservationDataTest;
import com.gaekdam.gaekdambe.dummy.generate.reservation_service.stay.DummyCheckInOutDataTest;
import com.gaekdam.gaekdambe.dummy.generate.reservation_service.stay.DummyStayDataTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@Rollback(false)
class DummyDataRunnerTest {

    // 호텔서비스
    @Autowired DummyHotelGroupDataTest hotelGroupDataTest;
    @Autowired DummyDepartmentDataTest departmentDataTest;
    @Autowired DummyPositionDataTest positionDataTest;
    @Autowired DummyPropertyDataTest propertyDataTest;

    // iam서비스
    @Autowired DummyPermissionTypeDataTest permissionTypeDataTest;
    @Autowired DummyPermissionDataTest permissionDataTest;

    // 오퍼레이션 서비스
    @Autowired DummyFacilityDataTest facilityDataTest;
    @Autowired DummyFacilityUsageDataTest facilityUsageDataTest;
    @Autowired DummyReservationPackageDataTest reservationPackageDataTest;
    @Autowired DummyRoomDataTest roomDataTest;
    @Autowired DummyRoomTypeDataTest roomTypeDataTest;

    // 예약 서비스
    @Autowired DummyReservationDataTest reservationDataTest;
    @Autowired DummyStayDataTest stayDataTest;
    @Autowired DummyCheckInOutDataTest checkInOutDataTest;

    // 고객 서비스
    @Autowired DummyCustomerDataTest customerDataTest;
    @Autowired DummyMembershipDataTest membershipDataTest;
    @Autowired DummyLoyaltyDataTest loyaltyDataTest;


    @Test
    void generateAll() {
        // 호텔서비스
        hotelGroupDataTest.generate();
        departmentDataTest.generate();
        positionDataTest.generate();
        propertyDataTest.generate();

        // iam서비스
        permissionTypeDataTest.generate();
        permissionDataTest.generate();

        // 오퍼레이션 서비스
        facilityDataTest.generate();
        facilityUsageDataTest.generate();
        reservationPackageDataTest.generate();
        roomDataTest.generate();
        roomTypeDataTest.generate();

        // 예약 서비스
        reservationDataTest.generate();
        stayDataTest.generate();
        checkInOutDataTest.generate();

        // customer_service (순서 중요: customer -> membership -> loyalty)
        customerDataTest.generate();
        membershipDataTest.generate();
        loyaltyDataTest.generate();

    }
}
