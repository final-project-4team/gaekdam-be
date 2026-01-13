package com.gaekdam.gaekdambe.dummy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;

import com.gaekdam.gaekdambe.dummy.generate.analytics_service.report.dataset.ReportKpiDatasetGenerator;
import com.gaekdam.gaekdambe.dummy.generate.communication_service.incident.DummyIncidentDataTest;
import com.gaekdam.gaekdambe.dummy.generate.communication_service.inquiry.DummyInquiryDataTest;
import com.gaekdam.gaekdambe.dummy.generate.communication_service.messaging.DummyMessageJourneyStageSetupTest;
import com.gaekdam.gaekdambe.dummy.generate.communication_service.messaging.DummyMessageRuleSetupTest;
import com.gaekdam.gaekdambe.dummy.generate.communication_service.messaging.DummyMessageTemplateSetupTest;
import com.gaekdam.gaekdambe.dummy.generate.customer_service.customer.DummyCustomerDataTest;
import com.gaekdam.gaekdambe.dummy.generate.customer_service.loyalty.DummyLoyaltyDataTest;
import com.gaekdam.gaekdambe.dummy.generate.customer_service.membership.DummyMembershipDataTest;
import com.gaekdam.gaekdambe.dummy.generate.hotel_service.department.DummyDepartmentDataTest;
import com.gaekdam.gaekdambe.dummy.generate.hotel_service.hotel.hotel_group.DummyHotelGroupDataTest;
import com.gaekdam.gaekdambe.dummy.generate.hotel_service.hotel.property.DummyPropertyDataTest;
import com.gaekdam.gaekdambe.dummy.generate.hotel_service.position.DummyPositionDataTest;
import com.gaekdam.gaekdambe.dummy.generate.iam_service.employee.EmployeeEncryptedRegistrationTest;
import com.gaekdam.gaekdambe.dummy.generate.iam_service.permission.DummyPermissionDataTest;
import com.gaekdam.gaekdambe.dummy.generate.iam_service.permissionMapping.DummyPermissionMappingDataTest;
import com.gaekdam.gaekdambe.dummy.generate.iam_service.permissionType.DummyPermissionTypeDataTest;
import com.gaekdam.gaekdambe.dummy.generate.operation_service.facility.DummyFacilityDataTest;
import com.gaekdam.gaekdambe.dummy.generate.operation_service.facility.DummyFacilityUsageDataTest;
import com.gaekdam.gaekdambe.dummy.generate.operation_service.facility.DummyReservationPackageDataTest;
import com.gaekdam.gaekdambe.dummy.generate.operation_service.room.DummyRoomDataTest;
import com.gaekdam.gaekdambe.dummy.generate.operation_service.room.DummyRoomTypeDataTest;
import com.gaekdam.gaekdambe.dummy.generate.reservation_service.reservation.DummyReservationDataTest;
import com.gaekdam.gaekdambe.dummy.generate.reservation_service.stay.DummyCheckInOutDataTest;
import com.gaekdam.gaekdambe.dummy.generate.reservation_service.stay.DummyStayDataTest;

@SpringBootTest
@Rollback(value = false)
class DummyDataRunnerTest {

    // 호텔서비스
    @Autowired DummyHotelGroupDataTest hotelGroupDataTest;
    @Autowired DummyDepartmentDataTest departmentDataTest;
    @Autowired DummyPositionDataTest positionDataTest;
    @Autowired DummyPropertyDataTest propertyDataTest;

    // iam서비스
    @Autowired DummyPermissionTypeDataTest permissionTypeDataTest;
    @Autowired DummyPermissionDataTest permissionDataTest;
    @Autowired DummyPermissionMappingDataTest permissionMappingDataTest;
    @Autowired EmployeeEncryptedRegistrationTest employeeDataTest;

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

    // 커뮤니케이션 서비스 (문의, 사건, 메세지 더미데이터 생성)
    @Autowired DummyIncidentDataTest incidentDataTest;
    @Autowired DummyInquiryDataTest inquiryDataTest;
    @Autowired DummyMessageJourneyStageSetupTest messageJourneyStageSetupTest;
    @Autowired DummyMessageRuleSetupTest messageRuleSetupTest;
    @Autowired DummyMessageTemplateSetupTest messageTemplateSetupTest;

    // 분석 서비스
    @Autowired ReportKpiDatasetGenerator reportKpiDatasetGenerator;


    @Test
    void generateAll() {
        // 호텔서비스
        hotelGroupDataTest.generate();
        propertyDataTest.generate();
        departmentDataTest.generate();
        positionDataTest.generate();

        // iam서비스
        permissionTypeDataTest.generate();
        permissionDataTest.generate();
        permissionMappingDataTest.generate();
        employeeDataTest.generate();


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

        // 고객 서비스
        customerDataTest.generate();
        membershipDataTest.generate();
        loyaltyDataTest.generate();

        // communication_service (문의, 사건, 메세지 더미데이터 생성)
        incidentDataTest.generate();
        inquiryDataTest.generate();
        messageJourneyStageSetupTest.generate();
        messageRuleSetupTest.generate();
        messageTemplateSetupTest.generate();

        // analytics_service (dashboard/report dummy data)
        reportKpiDatasetGenerator.generate();        

    }
}
