package com.gaekdam.gaekdambe.dummy;

import com.gaekdam.gaekdambe.dummy.generate.customer_service.customer.DummyCustomerStatusPostProcess;
import com.gaekdam.gaekdambe.dummy.generate.customer_service.customer.DummyMemberDataTest;
import com.gaekdam.gaekdambe.dummy.generate.customer_service.loyalty.DummyLoyaltyGradeDataTest;
import com.gaekdam.gaekdambe.dummy.generate.customer_service.membership.DummyMembershipGradeDataTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;

import com.gaekdam.gaekdambe.dummy.generate.analytics_service.report.dashboard.ReportTemplateGenerator;
import com.gaekdam.gaekdambe.dummy.generate.analytics_service.report.dashboard.ReportTemplateWidgetGenerator;
import com.gaekdam.gaekdambe.dummy.generate.analytics_service.report.dataset.ReportKpiDatasetGenerator;
import com.gaekdam.gaekdambe.dummy.generate.communication_service.incident.DummyIncidentDataTest;
import com.gaekdam.gaekdambe.dummy.generate.communication_service.inquiry.DummyInquiryCategoryDataTest;
import com.gaekdam.gaekdambe.dummy.generate.communication_service.inquiry.DummyInquiryDataTest;
import com.gaekdam.gaekdambe.dummy.generate.communication_service.messaging.DummyMessageJourneyStageSetupTest;
import com.gaekdam.gaekdambe.dummy.generate.communication_service.messaging.DummyMessageRuleSetupTest;
import com.gaekdam.gaekdambe.dummy.generate.communication_service.messaging.DummyMessageSendHistoryDataTest;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.StopWatch;

@DisabledIfEnvironmentVariable(named = "CI", matches = "true")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Rollback(value = false)
@ActiveProfiles("local-dummy")
@Tag("dummy")
class DummyDataRunnerTest {

    // 호텔서비스
    @Autowired
    DummyHotelGroupDataTest hotelGroupDataTest;
    @Autowired
    DummyDepartmentDataTest departmentDataTest;
    @Autowired
    DummyPositionDataTest positionDataTest;
    @Autowired
    DummyPropertyDataTest propertyDataTest;

    // iam서비스
    @Autowired
    DummyPermissionTypeDataTest permissionTypeDataTest;
    @Autowired
    DummyPermissionDataTest permissionDataTest;
    @Autowired
    EmployeeEncryptedRegistrationTest employeeDataTest;
    @Autowired
    DummyPermissionMappingDataTest permissionMappingDataTest;

    // 오퍼레이션 서비스
    @Autowired
    DummyFacilityDataTest facilityDataTest;
    @Autowired
    DummyReservationPackageDataTest reservationPackageDataTest;
    @Autowired
    DummyRoomDataTest roomDataTest;
    @Autowired
    DummyRoomTypeDataTest roomTypeDataTest;


    // 고객 서비스
    @Autowired
    DummyCustomerDataTest customerDataTest;
    @Autowired
    DummyMembershipDataTest membershipDataTest;
    @Autowired
    DummyLoyaltyDataTest loyaltyDataTest;
    @Autowired
    DummyCustomerStatusPostProcess customerStatusPostProcess;
    @Autowired
    DummyMemberDataTest memberDataTest;
    @Autowired
    DummyMembershipGradeDataTest membershipGradeDataTest;
    @Autowired
    DummyLoyaltyGradeDataTest loyaltyGradeDataTest;

    // 예약 서비스
    @Autowired
    DummyReservationDataTest reservationDataTest;
    @Autowired
    DummyStayDataTest stayDataTest;
    @Autowired
    DummyCheckInOutDataTest checkInOutDataTest;


    @Autowired
    DummyFacilityUsageDataTest facilityUsageDataTest;

    // 커뮤니케이션 서비스 (문의, 사건, 메세지 더미데이터 생성)
    @Autowired
    DummyIncidentDataTest incidentDataTest;
    @Autowired
    DummyInquiryDataTest inquiryDataTest;
    @Autowired
    DummyInquiryCategoryDataTest inquiryCategoryDataTest;
    @Autowired
    DummyMessageJourneyStageSetupTest messageJourneyStageSetupTest;
    @Autowired
    DummyMessageTemplateSetupTest messageTemplateSetupTest;
    @Autowired
    DummyMessageRuleSetupTest messageRuleSetupTest;
    @Autowired
    DummyMessageSendHistoryDataTest messageSendHistoryDataTest;

    // 분석 서비스
    @Autowired
    ReportKpiDatasetGenerator reportKpiDatasetGenerator;
    @Autowired
    ReportTemplateGenerator reportTemplateGenerator;
    @Autowired
    ReportTemplateWidgetGenerator reportTemplateWidgetGenerator;


    // 시간측정위한 메서드
    private void run(StopWatch sw, String name, Runnable task) {
        sw.start(name);
        try {
            task.run();
        } finally {
            sw.stop();
        }
    }

    @Test
    void generateAll() {
        StopWatch sw = new StopWatch("DummyDataGenerate");

        System.out.println(">>> generateAll called");

        // 호텔 서비스
        run(sw, "hotelGroup", hotelGroupDataTest::generate);
        run(sw, "property", propertyDataTest::generate);
        run(sw, "department", departmentDataTest::generate);
        run(sw, "position", positionDataTest::generate);

        // IAM 서비스
        run(sw, "permissionType", permissionTypeDataTest::generate);
        run(sw, "permission", permissionDataTest::generate);
        run(sw, "permissionMapping", permissionMappingDataTest::generate);
        run(sw, "employee", employeeDataTest::generate);

        // 오퍼레이션 서비스
        run(sw, "facility", facilityDataTest::generate);
        run(sw, "reservationPackage", reservationPackageDataTest::generate);
        run(sw, "roomType", roomTypeDataTest::generate);
        run(sw, "room", roomDataTest::generate);


        // 고객 서비스
        run(sw, "customer", customerDataTest::generate);
        run(sw, "membershipGrade", membershipGradeDataTest::generate);
        run(sw, "loyaltyGrade", loyaltyGradeDataTest::generate);

        // 예약 서비스
        run(sw, "reservation(100k)", reservationDataTest::generate);
        run(sw, "stay", stayDataTest::generate);
        run(sw, "checkInOut", checkInOutDataTest::generate);

        // 부대시설 이용
        run(sw, "facilityUsage", facilityUsageDataTest::generate);

        run(sw, "membership", membershipDataTest::generate);
        run(sw, "member", memberDataTest::generate);
        run(sw, "loyalty", loyaltyDataTest::generate);

        // 고객 상태 후처리 (StayStatus : 마지막 COMPLETED 기준)
        run(sw, "customerStatusPostProcess", customerStatusPostProcess::generate);

        // 커뮤니케이션 서비스
        run(sw, "inquiryCategory", inquiryCategoryDataTest::generate);
        run(sw, "inquiry", inquiryDataTest::generate);
        run(sw, "incident", incidentDataTest::generate);
        run(sw, "messageStage", messageJourneyStageSetupTest::generate);
//        run(sw, "messageTemplate", messageTemplateSetupTest::generate);
//        run(sw, "messageRule", messageRuleSetupTest::generate);
        // 메세지 히스토리 더미데이터 생성x
//        run(sw, "messageSendHistory", messageSendHistoryDataTest::generate);

        // 분석 서비스
        run(sw, "reportKpiDataset", reportKpiDatasetGenerator::generate);
        run(sw, "reportTemplate", reportTemplateGenerator::generate);
        run(sw, "reportTemplateWidget", reportTemplateWidgetGenerator::generate);

        System.out.println(sw.prettyPrint());
    }

}
