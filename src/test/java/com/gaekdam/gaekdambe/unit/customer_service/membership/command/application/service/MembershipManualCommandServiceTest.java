package com.gaekdam.gaekdambe.unit.customer_service.membership.command.application.service;

import com.gaekdam.gaekdambe.customer_service.customer.command.domain.ChangeSource;
import com.gaekdam.gaekdambe.customer_service.membership.command.application.dto.request.MembershipManualChangeRequest;
import com.gaekdam.gaekdambe.customer_service.membership.command.application.service.MembershipManualCommandService;
import com.gaekdam.gaekdambe.customer_service.membership.command.domain.MembershipGradeStatus;
import com.gaekdam.gaekdambe.customer_service.membership.command.domain.MembershipStatus;
import com.gaekdam.gaekdambe.customer_service.membership.command.domain.entity.Membership;
import com.gaekdam.gaekdambe.customer_service.membership.command.domain.entity.MembershipGrade;
import com.gaekdam.gaekdambe.customer_service.membership.command.domain.entity.MembershipHistory;
import com.gaekdam.gaekdambe.customer_service.membership.command.infrastructure.repository.MembershipGradeRepository;
import com.gaekdam.gaekdambe.customer_service.membership.command.infrastructure.repository.MembershipHistoryRepository;
import com.gaekdam.gaekdambe.customer_service.membership.command.infrastructure.repository.MembershipRepository;
import com.gaekdam.gaekdambe.global.exception.CustomException;
import com.gaekdam.gaekdambe.global.exception.ErrorCode;
import com.gaekdam.gaekdambe.hotel_service.hotel.command.domain.entity.HotelGroup;
import com.gaekdam.gaekdambe.iam_service.employee.command.domain.entity.Employee;
import com.gaekdam.gaekdambe.iam_service.employee.command.infrastructure.EmployeeRepository;
import com.gaekdam.gaekdambe.iam_service.log.command.application.service.AuditLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MembershipManualCommandServiceTest {

    private MembershipRepository membershipRepository;
    private MembershipGradeRepository membershipGradeRepository;
    private MembershipHistoryRepository membershipHistoryRepository;
    private EmployeeRepository employeeRepository;
    private AuditLogService auditLogService;

    private MembershipManualCommandService service;

    @BeforeEach
    void setUp() {
        membershipRepository = mock(MembershipRepository.class);
        membershipGradeRepository = mock(MembershipGradeRepository.class);
        membershipHistoryRepository = mock(MembershipHistoryRepository.class);
        employeeRepository = mock(EmployeeRepository.class);
        auditLogService = mock(AuditLogService.class);

        service = new MembershipManualCommandService(membershipRepository, membershipGradeRepository,
                membershipHistoryRepository, employeeRepository, auditLogService);

        Employee accessor = mock(Employee.class);
        lenient().when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(accessor));
    }

    @Test
    @DisplayName("request null이면 INVALID_INCORRECT_FORMAT")
    void requestNull_thenThrow() {
        // given

        // when
        CustomException ex = catchThrowableOfType(
                () -> service.changeMembershipManually(1L, 10L, 100L, null),
                CustomException.class);

        // then
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.INVALID_INCORRECT_FORMAT);
    }

    @Test
    @DisplayName("membershipGradeCode null이면 INVALID_INCORRECT_FORMAT")
    void gradeCodeNull_thenThrow() {
        // given
        MembershipManualChangeRequest req = new MembershipManualChangeRequest(
                null, MembershipStatus.ACTIVE, null, "reason", 10L);

        // when
        CustomException ex = catchThrowableOfType(
                () -> service.changeMembershipManually(1L, 10L, 100L, req),
                CustomException.class);

        // then
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.INVALID_INCORRECT_FORMAT);
    }

    @Test
    @DisplayName("membershipStatus null이면 INVALID_INCORRECT_FORMAT")
    void statusNull_thenThrow() {
        // given
        MembershipManualChangeRequest req = new MembershipManualChangeRequest(
                1L, null, null, "reason", 10L);

        // when
        CustomException ex = catchThrowableOfType(
                () -> service.changeMembershipManually(1L, 10L, 100L, req),
                CustomException.class);

        // then
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.INVALID_INCORRECT_FORMAT);
    }

    @Test
    @DisplayName("employeeCode null이면 EMPLOYEE_CODE_REQUIRED")
    void employeeCodeNull_thenThrow() {
        // given
        MembershipManualChangeRequest req = new MembershipManualChangeRequest(
                1L, MembershipStatus.ACTIVE, null, "reason", 10L);

        // when
        CustomException ex = catchThrowableOfType(
                () -> service.changeMembershipManually(1L, null, 100L, req),
                CustomException.class);

        // then
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.EMPLOYEE_CODE_REQUIRED);
    }

    @Test
    @DisplayName("changeReason null이면 MEMBERSHIP_MANUAL_REASON_REQUIRED")
    void changeReasonNull_thenThrow() {
        // given
        MembershipManualChangeRequest req = new MembershipManualChangeRequest(
                1L, MembershipStatus.ACTIVE, null, null, 10L);

        // when
        CustomException ex = catchThrowableOfType(
                () -> service.changeMembershipManually(1L, 10L, 100L, req),
                CustomException.class);

        // then
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.MEMBERSHIP_MANUAL_REASON_REQUIRED);
    }

    @Test
    @DisplayName("changeReason blank이면 MEMBERSHIP_MANUAL_REASON_REQUIRED")
    void changeReasonBlank_thenThrow() {
        // given
        MembershipManualChangeRequest req = new MembershipManualChangeRequest(
                1L, MembershipStatus.ACTIVE, null, "   ", 10L);

        // when
        CustomException ex = catchThrowableOfType(
                () -> service.changeMembershipManually(1L, 10L, 100L, req),
                CustomException.class);

        // then
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.MEMBERSHIP_MANUAL_REASON_REQUIRED);
    }

    @Test
    @DisplayName("afterGrade 없으면 MEMBERSHIP_GRADE_NOT_FOUND")
    void afterGradeNotFound_thenThrow() {
        // given
        MembershipManualChangeRequest req = new MembershipManualChangeRequest(
                999L, MembershipStatus.ACTIVE, null, "reason", 10L);
        when(membershipGradeRepository.findById(999L)).thenReturn(Optional.empty());

        // when
        CustomException ex = catchThrowableOfType(
                () -> service.changeMembershipManually(1L, 10L, 100L, req),
                CustomException.class);

        // then
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.MEMBERSHIP_GRADE_NOT_FOUND);
    }

    @Test
    @DisplayName("afterGrade 호텔그룹 불일치면 HOTEL_GROUP_CODE_NOT_MATCH")
    void hotelGroupMismatch_thenThrow() {
        // given
        MembershipManualChangeRequest req = new MembershipManualChangeRequest(
                1L, MembershipStatus.ACTIVE, null, "reason", 10L);

        MembershipGrade grade = mock(MembershipGrade.class);
        HotelGroup hg = mock(HotelGroup.class);

        when(membershipGradeRepository.findById(1L)).thenReturn(Optional.of(grade));
        when(grade.getHotelGroup()).thenReturn(hg);
        when(hg.getHotelGroupCode()).thenReturn(999L);

        // when
        CustomException ex = catchThrowableOfType(
                () -> service.changeMembershipManually(1L, 10L, 100L, req),
                CustomException.class);

        // then
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.HOTEL_GROUP_CODE_NOT_MATCH);
        verify(grade, never()).getMembershipGradeStatus();
    }

    @Test
    @DisplayName("afterGrade 비활성이면 MEMBERSHIP_GRADE_INACTIVE")
    void afterGradeInactive_thenThrow() {
        // given
        MembershipManualChangeRequest req = new MembershipManualChangeRequest(
                1L, MembershipStatus.ACTIVE, null, "reason", 10L);

        MembershipGrade grade = mock(MembershipGrade.class);
        HotelGroup hg = mock(HotelGroup.class);

        when(membershipGradeRepository.findById(1L)).thenReturn(Optional.of(grade));
        when(grade.getHotelGroup()).thenReturn(hg);
        when(hg.getHotelGroupCode()).thenReturn(1L);
        when(grade.getMembershipGradeStatus()).thenReturn(MembershipGradeStatus.INACTIVE);

        // when
        CustomException ex = catchThrowableOfType(
                () -> service.changeMembershipManually(1L, 10L, 100L, req),
                CustomException.class);

        // then
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.MEMBERSHIP_GRADE_INACTIVE);
    }

    @Test
    @DisplayName("afterGradeName null이면 MEMBERSHIP_GRADE_NAME_EMPTY")
    void afterGradeNameNull_thenThrow() {
        // given
        Long hotelGroupCode = 1L;
        Long employeeCode = 10L;
        Long customerCode = 100L;

        MembershipManualChangeRequest req = new MembershipManualChangeRequest(
                1L, MembershipStatus.ACTIVE, null, "reason", employeeCode);

        MembershipGrade afterGrade = mock(MembershipGrade.class);
        HotelGroup hg = mock(HotelGroup.class);

        when(membershipGradeRepository.findById(1L)).thenReturn(Optional.of(afterGrade));
        when(afterGrade.getHotelGroup()).thenReturn(hg);
        when(hg.getHotelGroupCode()).thenReturn(hotelGroupCode);
        when(afterGrade.getMembershipGradeStatus()).thenReturn(MembershipGradeStatus.ACTIVE);
        when(afterGrade.getGradeName()).thenReturn(null);

        // membership null 방지(핵심)
        Membership membership = Membership.registerMembership(
                customerCode, hotelGroupCode, null, LocalDateTime.now(), LocalDateTime.now());
        when(membershipRepository.findByCustomerCodeAndHotelGroupCode(customerCode, hotelGroupCode))
                .thenReturn(Optional.of(membership));

        // when
        CustomException ex = catchThrowableOfType(
                () -> service.changeMembershipManually(hotelGroupCode, employeeCode, customerCode, req),
                CustomException.class);

        // then
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.MEMBERSHIP_GRADE_NAME_EMPTY);
    }

    @Test
    @DisplayName("afterGradeName blank이면 MEMBERSHIP_GRADE_NAME_EMPTY")
    void afterGradeNameBlank_thenThrow() {
        // given
        Long hotelGroupCode = 1L;
        Long employeeCode = 10L;
        Long customerCode = 100L;

        MembershipManualChangeRequest req = new MembershipManualChangeRequest(
                1L, MembershipStatus.ACTIVE, null, "reason", employeeCode);

        MembershipGrade afterGrade = mock(MembershipGrade.class);
        HotelGroup hg = mock(HotelGroup.class);

        when(membershipGradeRepository.findById(1L)).thenReturn(Optional.of(afterGrade));
        when(afterGrade.getHotelGroup()).thenReturn(hg);
        when(hg.getHotelGroupCode()).thenReturn(hotelGroupCode);
        when(afterGrade.getMembershipGradeStatus()).thenReturn(MembershipGradeStatus.ACTIVE);
        when(afterGrade.getGradeName()).thenReturn("   ");

        // membership null 방지(핵심)
        Membership membership = Membership.registerMembership(
                customerCode, hotelGroupCode, null, LocalDateTime.now(), LocalDateTime.now());
        when(membershipRepository.findByCustomerCodeAndHotelGroupCode(customerCode, hotelGroupCode))
                .thenReturn(Optional.of(membership));

        // when
        CustomException ex = catchThrowableOfType(
                () -> service.changeMembershipManually(hotelGroupCode, employeeCode, customerCode, req),
                CustomException.class);

        // then
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.MEMBERSHIP_GRADE_NAME_EMPTY);
    }

    @Test
    @DisplayName("멤버십 기존 존재 시: 변경 + 이력 저장 (beforeGradeName 정상)")
    void existingMembership_success() throws Exception {
        // given
        Long hotelGroupCode = 1L;
        Long employeeCode = 10L;
        Long customerCode = 100L;

        LocalDateTime expiredAt = LocalDateTime.of(LocalDateTime.now().getYear(), 12, 31, 23, 59, 59);
        MembershipManualChangeRequest req = new MembershipManualChangeRequest(
                2L, MembershipStatus.SUSPENDED, expiredAt, "manual reason", employeeCode);

        MembershipGrade afterGrade = mock(MembershipGrade.class);
        HotelGroup hg = mock(HotelGroup.class);

        when(membershipGradeRepository.findById(2L)).thenReturn(Optional.of(afterGrade));
        when(afterGrade.getHotelGroup()).thenReturn(hg);
        when(hg.getHotelGroupCode()).thenReturn(hotelGroupCode);
        when(afterGrade.getMembershipGradeStatus()).thenReturn(MembershipGradeStatus.ACTIVE);
        when(afterGrade.getMembershipGradeCode()).thenReturn(2L);
        when(afterGrade.getGradeName()).thenReturn("GOLD");

        Membership membership = Membership.registerMembership(
                customerCode, hotelGroupCode, 1L,
                LocalDateTime.now().minusDays(10),
                LocalDateTime.now().minusDays(10));
        setField(membership, "membershipCode", 777L);
        setField(membership, "expiredAt", LocalDateTime.now().plusDays(1));
        setField(membership, "membershipStatus", MembershipStatus.ACTIVE);
        setField(membership, "membershipGradeCode", 1L);

        when(membershipRepository.findByCustomerCodeAndHotelGroupCode(customerCode, hotelGroupCode))
                .thenReturn(Optional.of(membership));

        MembershipGrade beforeGrade = mock(MembershipGrade.class);
        when(membershipGradeRepository.findById(1L)).thenReturn(Optional.of(beforeGrade));
        when(beforeGrade.getGradeName()).thenReturn("SILVER");

        ArgumentCaptor<MembershipHistory> historyCaptor = ArgumentCaptor.forClass(MembershipHistory.class);

        // when
        String result = service.changeMembershipManually(hotelGroupCode, employeeCode, customerCode, req);

        // then
        assertThat(result).isEqualTo("멤버십 수동 변경 완료");
        verify(membershipRepository, times(1)).save(membership);

        verify(membershipHistoryRepository).save(historyCaptor.capture());
        MembershipHistory saved = historyCaptor.getValue();

        assertThat(saved.getCustomerCode()).isEqualTo(customerCode);
        assertThat(saved.getMembershipCode()).isEqualTo(777L);
        assertThat(saved.getChangeSource()).isEqualTo(ChangeSource.MANUAL);
        assertThat(saved.getChangedByEmployeeCode()).isEqualTo(employeeCode);
        assertThat(saved.getChangeReason()).isEqualTo("manual reason");
        assertThat(saved.getBeforeGrade()).isEqualTo("SILVER");
        assertThat(saved.getAfterGrade()).isEqualTo("GOLD");
        assertThat(saved.getBeforeStatus()).isEqualTo(MembershipStatus.ACTIVE);
        assertThat(saved.getAfterStatus()).isEqualTo(MembershipStatus.SUSPENDED);
        assertThat(saved.getMembershipGradeCode()).isEqualTo(2L);
    }

    @Test
    @DisplayName("기존 멤버십 + beforeGrade 조회 empty 브랜치 커버")
    void existingMembership_beforeGradeNotFound_stillSaveHistory() throws Exception {
        // given
        Long hotelGroupCode = 1L;
        Long employeeCode = 10L;
        Long customerCode = 100L;

        MembershipManualChangeRequest req = new MembershipManualChangeRequest(
                2L, MembershipStatus.ACTIVE, null, "manual reason", employeeCode);

        MembershipGrade afterGrade = mock(MembershipGrade.class);
        HotelGroup hg = mock(HotelGroup.class);

        when(membershipGradeRepository.findById(2L)).thenReturn(Optional.of(afterGrade));
        when(afterGrade.getHotelGroup()).thenReturn(hg);
        when(hg.getHotelGroupCode()).thenReturn(hotelGroupCode);
        when(afterGrade.getMembershipGradeStatus()).thenReturn(MembershipGradeStatus.ACTIVE);
        when(afterGrade.getMembershipGradeCode()).thenReturn(2L);
        when(afterGrade.getGradeName()).thenReturn("GOLD");

        Membership membership = Membership.registerMembership(
                customerCode, hotelGroupCode, 1L,
                LocalDateTime.now().minusDays(10),
                LocalDateTime.now().minusDays(10));
        setField(membership, "membershipCode", 777L);
        setField(membership, "membershipGradeCode", 1L);

        when(membershipRepository.findByCustomerCodeAndHotelGroupCode(customerCode, hotelGroupCode))
                .thenReturn(Optional.of(membership));

        when(membershipGradeRepository.findById(1L)).thenReturn(Optional.empty());

        // when
        String result = service.changeMembershipManually(hotelGroupCode, employeeCode, customerCode, req);

        // then
        assertThat(result).isEqualTo("멤버십 수동 변경 완료");
        verify(membershipHistoryRepository).save(any(MembershipHistory.class));
    }

    @Test
    @DisplayName("기존 멤버십 + beforeGradeCode null 브랜치 커버")
    void existingMembership_beforeGradeCodeNull_stillSaveHistory() throws Exception {
        // given
        Long hotelGroupCode = 1L;
        Long employeeCode = 10L;
        Long customerCode = 100L;

        MembershipManualChangeRequest req = new MembershipManualChangeRequest(
                2L, MembershipStatus.ACTIVE, null, "manual reason", employeeCode);

        MembershipGrade afterGrade = mock(MembershipGrade.class);
        HotelGroup hg = mock(HotelGroup.class);

        when(membershipGradeRepository.findById(2L)).thenReturn(Optional.of(afterGrade));
        when(afterGrade.getHotelGroup()).thenReturn(hg);
        when(hg.getHotelGroupCode()).thenReturn(hotelGroupCode);
        when(afterGrade.getMembershipGradeStatus()).thenReturn(MembershipGradeStatus.ACTIVE);
        when(afterGrade.getMembershipGradeCode()).thenReturn(2L);
        when(afterGrade.getGradeName()).thenReturn("GOLD");

        Membership membership = Membership.registerMembership(
                customerCode, hotelGroupCode, null,
                LocalDateTime.now().minusDays(10),
                LocalDateTime.now().minusDays(10));
        setField(membership, "membershipCode", 777L);
        setField(membership, "membershipGradeCode", null);

        when(membershipRepository.findByCustomerCodeAndHotelGroupCode(customerCode, hotelGroupCode))
                .thenReturn(Optional.of(membership));

        // when
        String result = service.changeMembershipManually(hotelGroupCode, employeeCode, customerCode, req);

        // then
        assertThat(result).isEqualTo("멤버십 수동 변경 완료");
        verify(membershipHistoryRepository).save(any(MembershipHistory.class));
        verify(membershipGradeRepository, never()).findById(1L);
    }

    @Test
    @DisplayName("멤버십 미존재 시: 신규 생성(orElseGet) + 변경 + 이력 저장 (save 2번)")
    void newMembership_success() throws Exception {
        // given
        Long hotelGroupCode = 1L;
        Long employeeCode = 10L;
        Long customerCode = 100L;

        MembershipManualChangeRequest req = new MembershipManualChangeRequest(
                2L, MembershipStatus.ACTIVE, null, "manual reason", employeeCode);

        MembershipGrade afterGrade = mock(MembershipGrade.class);
        HotelGroup hg = mock(HotelGroup.class);

        when(membershipGradeRepository.findById(2L)).thenReturn(Optional.of(afterGrade));
        when(afterGrade.getHotelGroup()).thenReturn(hg);
        when(hg.getHotelGroupCode()).thenReturn(hotelGroupCode);
        when(afterGrade.getMembershipGradeStatus()).thenReturn(MembershipGradeStatus.ACTIVE);
        when(afterGrade.getMembershipGradeCode()).thenReturn(2L);
        when(afterGrade.getGradeName()).thenReturn("GOLD");

        when(membershipRepository.findByCustomerCodeAndHotelGroupCode(customerCode, hotelGroupCode))
                .thenReturn(Optional.empty());

        when(membershipRepository.save(any(Membership.class))).thenAnswer(inv -> {
            Membership m = inv.getArgument(0);
            if (m.getMembershipCode() == null) {
                setField(m, "membershipCode", 888L);
            }
            return m;
        });

        ArgumentCaptor<MembershipHistory> historyCaptor = ArgumentCaptor.forClass(MembershipHistory.class);

        // when
        String result = service.changeMembershipManually(hotelGroupCode, employeeCode, customerCode, req);

        // then
        assertThat(result).isEqualTo("멤버십 수동 변경 완료");
        verify(membershipRepository, times(2)).save(any(Membership.class));

        verify(membershipHistoryRepository).save(historyCaptor.capture());
        MembershipHistory saved = historyCaptor.getValue();

        assertThat(saved.getMembershipCode()).isNotNull();
        assertThat(saved.getAfterGrade()).isEqualTo("GOLD");
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }
}
