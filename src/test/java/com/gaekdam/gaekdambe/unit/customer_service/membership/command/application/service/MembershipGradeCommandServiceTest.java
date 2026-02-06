package com.gaekdam.gaekdambe.unit.customer_service.membership.command.application.service;

import com.gaekdam.gaekdambe.customer_service.membership.command.application.dto.request.MembershipGradeRequest;
import com.gaekdam.gaekdambe.customer_service.membership.command.application.service.MembershipGradeCommandService;
import com.gaekdam.gaekdambe.customer_service.membership.command.domain.MembershipGradeStatus;
import com.gaekdam.gaekdambe.customer_service.membership.command.domain.entity.MembershipGrade;
import com.gaekdam.gaekdambe.customer_service.membership.command.infrastructure.repository.MembershipGradeRepository;
import com.gaekdam.gaekdambe.customer_service.membership.command.infrastructure.repository.MembershipHistoryRepository;
import com.gaekdam.gaekdambe.customer_service.membership.command.infrastructure.repository.MembershipRepository;
import com.gaekdam.gaekdambe.customer_service.membership.query.mapper.MembershipBatchMapper;
import com.gaekdam.gaekdambe.global.exception.CustomException;
import com.gaekdam.gaekdambe.global.exception.ErrorCode;
import com.gaekdam.gaekdambe.hotel_service.hotel.command.domain.entity.HotelGroup;
import com.gaekdam.gaekdambe.hotel_service.hotel.command.infrastructure.repository.HotelGroupRepository;
import com.gaekdam.gaekdambe.iam_service.employee.command.domain.entity.Employee;
import com.gaekdam.gaekdambe.iam_service.employee.command.infrastructure.EmployeeRepository;
import com.gaekdam.gaekdambe.iam_service.log.command.application.service.AuditLogService;
import com.gaekdam.gaekdambe.iam_service.permission_type.command.domain.seeds.PermissionTypeKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MembershipGradeCommandServiceTest {

        private MembershipBatchMapper membershipBatchMapper;
        private MembershipRepository membershipRepository;
        private HotelGroupRepository hotelGroupRepository;
        private MembershipGradeRepository membershipGradeRepository;
        private MembershipHistoryRepository membershipHistoryRepository;
        private EmployeeRepository employeeRepository;
        private AuditLogService auditLogService;

        private MembershipGradeCommandService service;

        @BeforeEach
        void setUp() {
                hotelGroupRepository = mock(HotelGroupRepository.class);
                membershipGradeRepository = mock(MembershipGradeRepository.class);
                membershipBatchMapper = mock(MembershipBatchMapper.class);
                membershipRepository = mock(MembershipRepository.class);
                membershipHistoryRepository = mock(MembershipHistoryRepository.class);
                employeeRepository = mock(EmployeeRepository.class);
                auditLogService = mock(AuditLogService.class);

                service = new MembershipGradeCommandService(
                                membershipBatchMapper,
                                membershipRepository,
                                membershipHistoryRepository,
                                hotelGroupRepository,
                                membershipGradeRepository,
                                employeeRepository,
                                auditLogService);
        }

        private MembershipGradeRequest validReq() {
                return new MembershipGradeRequest("GOLD", 2L, "tier", 1000L, 1);
        }

        // ---------- create ----------

        @Test
        @DisplayName("create: gradeName null이면 INVALID_INCORRECT_FORMAT")
        void create_gradeName_null_thenThrow() {
                // given
                MembershipGradeRequest req = new MembershipGradeRequest(null, 1L, "comment", 1000L, 1);

                // when
                CustomException ex = catchThrowableOfType(
                                () -> service.createMembershipGrade(req, 1L),
                                CustomException.class);

                // then
                assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.INVALID_INCORRECT_FORMAT);
                verifyNoInteractions(hotelGroupRepository, membershipGradeRepository);
        }

        @Test
        @DisplayName("create: gradeName blank이면 INVALID_INCORRECT_FORMAT")
        void create_gradeName_blank_thenThrow() {
                // given
                MembershipGradeRequest req = new MembershipGradeRequest("   ", 1L, "comment", 1000L, 1);

                // when
                CustomException ex = catchThrowableOfType(
                                () -> service.createMembershipGrade(req, 1L),
                                CustomException.class);

                // then
                assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.INVALID_INCORRECT_FORMAT);
                verifyNoInteractions(hotelGroupRepository, membershipGradeRepository);
        }

        @Test
        @DisplayName("create: tierLevel null이면 INVALID_INCORRECT_FORMAT")
        void create_tierLevel_null_thenThrow() {
                // given
                MembershipGradeRequest req = new MembershipGradeRequest("GOLD", null, "comment", 1000L, 1);

                // when
                CustomException ex = catchThrowableOfType(
                                () -> service.createMembershipGrade(req, 1L),
                                CustomException.class);

                // then
                assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.INVALID_INCORRECT_FORMAT);
                verifyNoInteractions(hotelGroupRepository, membershipGradeRepository);
        }

        @Test
        @DisplayName("create: tierComment blank이면 INVALID_INCORRECT_FORMAT")
        void create_tierComment_blank_thenThrow() {
                // given
                MembershipGradeRequest req = new MembershipGradeRequest("GOLD", 1L, "   ", 1000L, 1);

                // when
                CustomException ex = catchThrowableOfType(
                                () -> service.createMembershipGrade(req, 1L),
                                CustomException.class);

                // then
                assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.INVALID_INCORRECT_FORMAT);
                verifyNoInteractions(hotelGroupRepository, membershipGradeRepository);
        }

        @Test
        @DisplayName("create: 호텔그룹 없으면 HOTEL_GROUP_NOT_FOUND")
        void create_hotelGroup_notFound() {
                // given
                Long hotelGroupCode = 1L;
                MembershipGradeRequest req = validReq();
                when(hotelGroupRepository.findById(hotelGroupCode)).thenReturn(Optional.empty());

                // when
                CustomException ex = catchThrowableOfType(
                                () -> service.createMembershipGrade(req, hotelGroupCode),
                                CustomException.class);

                // then
                assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.HOTEL_GROUP_NOT_FOUND);
                verifyNoInteractions(membershipGradeRepository);
        }

        @Test
        @DisplayName("create: 성공 시 save 호출 + 성공 메시지")
        void create_success() {
                // given
                Long hotelGroupCode = 1L;
                MembershipGradeRequest req = validReq();
                HotelGroup hotelGroup = mock(HotelGroup.class);
                when(hotelGroupRepository.findById(hotelGroupCode)).thenReturn(Optional.of(hotelGroup));

                // when
                String result = service.createMembershipGrade(req, hotelGroupCode);

                // then
                assertThat(result).isEqualTo("멤버십 등급 생성 완료");
                verify(membershipGradeRepository).save(any(MembershipGrade.class));
        }

        // ---------- delete ----------

        @Test
        @DisplayName("delete: 등급 없으면 MEMBERSHIP_GRADE_NOT_FOUND")
        void delete_grade_notFound() {
                // given
                when(membershipGradeRepository.findById(10L)).thenReturn(Optional.empty());

                // when
                CustomException ex = catchThrowableOfType(
                                () -> service.deleteMembershipGrade(1L, 10L),
                                CustomException.class);

                // then
                assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.MEMBERSHIP_GRADE_NOT_FOUND);
                verify(membershipGradeRepository, never()).save(any());
        }

        @Test
        @DisplayName("delete: 호텔그룹 코드 불일치면 HOTEL_GROUP_CODE_NOT_MATCH")
        void delete_hotelGroup_mismatch() {
                // given
                Long hotelGroupCode = 1L;
                Long gradeCode = 10L;

                HotelGroup hg = mock(HotelGroup.class);
                when(hg.getHotelGroupCode()).thenReturn(999L);

                MembershipGrade grade = MembershipGrade.registerMembershipGrade(
                                hg, "GOLD", 1L, "tier", 1000L, 1);
                when(membershipGradeRepository.findById(gradeCode)).thenReturn(Optional.of(grade));

                // when
                CustomException ex = catchThrowableOfType(
                                () -> service.deleteMembershipGrade(hotelGroupCode, gradeCode),
                                CustomException.class);

                // then
                assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.HOTEL_GROUP_CODE_NOT_MATCH);
                verify(membershipGradeRepository, never()).save(any());
        }

        @Test
        @DisplayName("delete: 이미 INACTIVE면 MEMBERSHIP_GRADE_ALREADY_INACTIVE")
        void delete_already_inactive() {
                // given
                Long hotelGroupCode = 1L;
                Long gradeCode = 10L;

                HotelGroup hg = mock(HotelGroup.class);
                when(hg.getHotelGroupCode()).thenReturn(hotelGroupCode);

                MembershipGrade grade = MembershipGrade.registerMembershipGrade(
                                hg, "GOLD", 1L, "tier", 1000L, 1);
                grade.deleteMemberShipGradeStatus();

                when(membershipGradeRepository.findById(gradeCode)).thenReturn(Optional.of(grade));

                // when
                CustomException ex = catchThrowableOfType(
                                () -> service.deleteMembershipGrade(hotelGroupCode, gradeCode),
                                CustomException.class);

                // then
                assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.MEMBERSHIP_GRADE_ALREADY_INACTIVE);
                verify(membershipGradeRepository, never()).save(any());
        }

        @Test
        @DisplayName("delete: ACTIVE면 INACTIVE로 변경 후 save + 성공 메시지")
        void delete_success() {
                // given
                Long hotelGroupCode = 1L;
                Long gradeCode = 10L;

                HotelGroup hg = mock(HotelGroup.class);
                when(hg.getHotelGroupCode()).thenReturn(hotelGroupCode);

                MembershipGrade grade = MembershipGrade.registerMembershipGrade(
                                hg, "GOLD", 1L, "tier", 1000L, 1);
                when(membershipGradeRepository.findById(gradeCode)).thenReturn(Optional.of(grade));

                // when
                String result = service.deleteMembershipGrade(hotelGroupCode, gradeCode);

                // then
                assertThat(result).isEqualTo("멤버십이 등급이 삭제 되었습니다");
                assertThat(grade.getMembershipGradeStatus()).isEqualTo(MembershipGradeStatus.INACTIVE);
                verify(membershipGradeRepository).save(grade);
        }

        // ---------- update ----------

        @Test
        @DisplayName("update: gradeName blank이면 INVALID_INCORRECT_FORMAT")
        void update_gradeName_blank_thenThrow() {
                // given
                String accessorId = "hong0";
                MembershipGradeRequest req = new MembershipGradeRequest("   ", 1L, "tier", 1000L, 1);

                // when
                CustomException ex = catchThrowableOfType(
                                () -> service.updateMembershipGrade(1L, 10L, req, accessorId),
                                CustomException.class);

                // then
                assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.INVALID_INCORRECT_FORMAT);
                verifyNoInteractions(membershipGradeRepository, employeeRepository, auditLogService);
        }

        @Test
        @DisplayName("update: 등급 없으면 MEMBERSHIP_GRADE_NOT_FOUND")
        void update_grade_notFound() {
                // given
                String accessorId = "hong0";
                MembershipGradeRequest req = validReq();
                when(membershipGradeRepository.findById(10L)).thenReturn(Optional.empty());

                // when
                CustomException ex = catchThrowableOfType(
                                () -> service.updateMembershipGrade(1L, 10L, req, accessorId),
                                CustomException.class);

                // then
                assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.MEMBERSHIP_GRADE_NOT_FOUND);
                verifyNoInteractions(employeeRepository, auditLogService);
        }

        @Test
        @DisplayName("update: 호텔그룹 코드 불일치면 HOTEL_GROUP_CODE_NOT_MATCH")
        void update_hotelGroup_mismatch() {
                // given
                Long hotelGroupCode = 1L;
                Long gradeCode = 10L;
                String accessorId = "hong0";
                MembershipGradeRequest req = validReq();

                HotelGroup hg = mock(HotelGroup.class);
                when(hg.getHotelGroupCode()).thenReturn(999L);

                MembershipGrade grade = MembershipGrade.registerMembershipGrade(
                                hg, "SILVER", 1L, "tier", 1000L, 1);
                when(membershipGradeRepository.findById(gradeCode)).thenReturn(Optional.of(grade));

                // when
                CustomException ex = catchThrowableOfType(
                                () -> service.updateMembershipGrade(hotelGroupCode, gradeCode, req, accessorId),
                                CustomException.class);

                // then
                assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.HOTEL_GROUP_CODE_NOT_MATCH);
                verifyNoInteractions(employeeRepository, auditLogService);
        }

        @Test
        @DisplayName("update: 접근자 직원 없으면 EMPLOYEE_NOT_FOUND")
        void update_accessor_notFound() {
                // given
                Long hotelGroupCode = 1L;
                Long gradeCode = 10L;
                String accessorId = "hong0";

                MembershipGradeRequest req = validReq();

                HotelGroup hg = mock(HotelGroup.class);
                when(hg.getHotelGroupCode()).thenReturn(hotelGroupCode);

                MembershipGrade grade = MembershipGrade.registerMembershipGrade(
                                hg, "SILVER", 1L, "tier", 1000L, 1);
                when(membershipGradeRepository.findById(gradeCode)).thenReturn(Optional.of(grade));
                when(employeeRepository.findByLoginId(accessorId)).thenReturn(Optional.empty());

                // when
                CustomException ex = catchThrowableOfType(
                                () -> service.updateMembershipGrade(hotelGroupCode, gradeCode, req, accessorId),
                                CustomException.class);

                // then
                assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.EMPLOYEE_NOT_FOUND);
                verifyNoInteractions(auditLogService);
        }

        @Test
        @DisplayName("update: 정상 수정되면 반영 + auditLog 호출 + 성공 메시지 (save는 호출 안 함)")
        void update_success() {
                // given
                Long hotelGroupCode = 1L;
                Long gradeCode = 10L;
                String accessorId = "hong0";

                MembershipGradeRequest req = new MembershipGradeRequest(
                                "GOLD", 2L, "new tier", 2000L, 2);

                HotelGroup hg = mock(HotelGroup.class);
                when(hg.getHotelGroupCode()).thenReturn(hotelGroupCode);

                MembershipGrade grade = MembershipGrade.registerMembershipGrade(
                                hg, "SILVER", 1L, "tier", 1000L, 1);
                when(membershipGradeRepository.findById(gradeCode)).thenReturn(Optional.of(grade));

                Employee accessor = mock(Employee.class);
                when(employeeRepository.findByLoginId(accessorId)).thenReturn(Optional.of(accessor));

                ArgumentCaptor<String> detailsCaptor = ArgumentCaptor.forClass(String.class);

                // when
                String result = service.updateMembershipGrade(hotelGroupCode, gradeCode, req, accessorId);

                // then
                assertThat(result).isEqualTo("등급 정보가 수정 되었습니다");
                assertThat(grade.getGradeName()).isEqualTo("GOLD");
                assertThat(grade.getTierLevel()).isEqualTo(2L);

                verify(membershipGradeRepository, never()).save(any());

                verify(auditLogService).saveAuditLog(
                                eq(accessor),
                                eq(PermissionTypeKey.MEMBERSHIP_POLICY_UPDATE),
                                detailsCaptor.capture(),
                                any(),
                                any());
                assertThat(detailsCaptor.getValue()).contains("등급명").contains("티어");
        }
}
