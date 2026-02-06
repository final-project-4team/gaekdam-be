package com.gaekdam.gaekdambe.unit.iam_service.log.command.application.service;

import com.gaekdam.gaekdambe.customer_service.customer.command.domain.entity.Customer;
import com.gaekdam.gaekdambe.hotel_service.hotel.command.domain.entity.HotelGroup;
import com.gaekdam.gaekdambe.iam_service.employee.command.domain.entity.Employee;
import com.gaekdam.gaekdambe.iam_service.log.command.application.service.AuditLogService;
import com.gaekdam.gaekdambe.iam_service.log.command.domain.entity.LoginLog;
import com.gaekdam.gaekdambe.iam_service.log.command.domain.entity.PermissionChangedLog;
import com.gaekdam.gaekdambe.iam_service.log.command.domain.entity.PersonalInformationLog;
import com.gaekdam.gaekdambe.iam_service.log.command.infrastructure.AuditLogRepository;
import com.gaekdam.gaekdambe.iam_service.log.command.infrastructure.LoginLogRepository;
import com.gaekdam.gaekdambe.iam_service.log.command.infrastructure.PermissionChangedLogRepository;
import com.gaekdam.gaekdambe.iam_service.log.command.infrastructure.PersonalInformationLogRepository;
import com.gaekdam.gaekdambe.iam_service.permission.command.domain.entity.Permission;
import com.gaekdam.gaekdambe.iam_service.permission_type.command.domain.seeds.PermissionTypeKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceTest {

    @InjectMocks
    private AuditLogService service;

    @Mock
    private LoginLogRepository loginLogRepository;
    @Mock
    private PermissionChangedLogRepository permissionChangedLogRepository;
    @Mock
    private PersonalInformationLogRepository personalInformationLogRepository;
    @Mock
    private AuditLogRepository auditLogRepository;

    @Test
    @DisplayName("saveAuditLog: 감사 로그 저장 성공")
    void saveAuditLog_success() {
        // given
        Employee employee = mock(Employee.class);
        HotelGroup hg = mock(HotelGroup.class);
        given(hg.getHotelGroupCode()).willReturn(1L);
        given(employee.getHotelGroup()).willReturn(hg);

        // when
        service.saveAuditLog(employee, PermissionTypeKey.EMPLOYEE_CREATE, "details", "old", "new");

        // then
        verify(auditLogRepository)
                .save(any(com.gaekdam.gaekdambe.iam_service.log.command.domain.entity.AuditLog.class));
    }

    @Test
    @DisplayName("logLoginSuccess: 로그인 성공 로그 저장 성공")
    void logLoginSuccess_success() {
        // given
        Employee employee = mock(Employee.class);
        HotelGroup hg = mock(HotelGroup.class);
        given(employee.getHotelGroup()).willReturn(hg);

        // when
        service.logLoginSuccess(employee, "127.0.0.1");

        // then
        verify(loginLogRepository).save(any(LoginLog.class));
    }

    @Test
    @DisplayName("logPermissionChanged: 권한 변경 로그 저장 성공")
    void logPermissionChanged_success() {
        // given
        Employee employee = mock(Employee.class);
        Employee accessor = mock(Employee.class);
        Permission oldPerm = mock(Permission.class);
        Permission newPerm = mock(Permission.class);
        HotelGroup hg = mock(HotelGroup.class);

        given(employee.getHotelGroup()).willReturn(hg);

        // when
        service.logPermissionChanged(employee, accessor, oldPerm, newPerm);

        // then
        verify(permissionChangedLogRepository).save(any(PermissionChangedLog.class));
    }

    @Test
    @DisplayName("logPermissionChanged: 저장 실패 시 RuntimeException 발생")
    void logPermissionChanged_fail() {
        // given
        Employee employee = mock(Employee.class);
        Employee accessor = mock(Employee.class);
        Permission oldPerm = mock(Permission.class);
        Permission newPerm = mock(Permission.class);

        given(employee.getHotelGroup()).willReturn(mock(HotelGroup.class));
        doThrow(new RuntimeException("DB Error")).when(permissionChangedLogRepository).save(any());

        // when & then
        assertThatThrownBy(() -> service.logPermissionChanged(employee, accessor, oldPerm, newPerm))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("권한 변경 감사 로그 저장 실패");
    }

    @Test
    @DisplayName("logCustomerAccess: 개인정보 조회 로그 저장 성공")
    void logCustomerAccess_success() {
        // given
        Employee accessor = mock(Employee.class);
        Customer customer = mock(Customer.class);

        // when
        service.logCustomerAccess(accessor, customer, PermissionTypeKey.CUSTOMER_READ, "Check");

        // then
        verify(personalInformationLogRepository).save(any(PersonalInformationLog.class));
    }

    // ========== Exception Branch Tests ==========

    @Test
    @DisplayName("saveAuditLog: 저장 실패 시 예외를 로그만 남기고 전파하지 않음")
    void saveAuditLog_catchException() {
        // given
        Employee employee = mock(Employee.class);
        given(employee.getLoginId()).willReturn("testUser");
        HotelGroup hg = mock(HotelGroup.class);
        given(employee.getHotelGroup()).willReturn(hg);

        doThrow(new RuntimeException("DB Error")).when(auditLogRepository).save(any());

        // when - 예외가 발생해도 메서드는 정상 종료되어야 함 (catch 블록에서 처리)
        service.saveAuditLog(employee, PermissionTypeKey.EMPLOYEE_CREATE, "details", "old", "new");

        // then - save는 호출되었지만 예외가 전파되지 않음
        verify(auditLogRepository).save(any());
    }

    @Test
    @DisplayName("logLoginSuccess: 저장 실패 시 예외를 로그만 남기고 전파하지 않음")
    void logLoginSuccess_catchException() {
        // given
        Employee employee = mock(Employee.class);
        given(employee.getLoginId()).willReturn("testUser");
        given(employee.getHotelGroup()).willReturn(mock(HotelGroup.class));

        doThrow(new RuntimeException("DB Error")).when(loginLogRepository).save(any());

        // when - 예외가 발생해도 메서드는 정상 종료되어야 함
        service.logLoginSuccess(employee, "127.0.0.1");

        // then
        verify(loginLogRepository).save(any(LoginLog.class));
    }

    @Test
    @DisplayName("logLoginFailed: 로그인 실패 로그 저장 성공")
    void logLoginFailed_success() {
        // given
        Employee employee = mock(Employee.class);
        given(employee.getLoginId()).willReturn("testUser");
        given(employee.getHotelGroup()).willReturn(mock(HotelGroup.class));
        given(employee.getFailedLoginCount()).willReturn(3);

        // when
        service.logLoginFailed(employee, "127.0.0.1", "Wrong password");

        // then
        verify(loginLogRepository).save(any(LoginLog.class));
    }

    @Test
    @DisplayName("logLoginFailed: 저장 실패 시 예외를 로그만 남기고 전파하지 않음")
    void logLoginFailed_catchException() {
        // given
        Employee employee = mock(Employee.class);
        given(employee.getLoginId()).willReturn("testUser");
        given(employee.getHotelGroup()).willReturn(mock(HotelGroup.class));

        doThrow(new RuntimeException("DB Error")).when(loginLogRepository).save(any());

        // when
        service.logLoginFailed(employee, "127.0.0.1", "Wrong password");

        // then - 예외가 전파되지 않음
        verify(loginLogRepository).save(any(LoginLog.class));
    }

    @Test
    @DisplayName("logAccountLocked: 계정 잠금 로그 저장 성공")
    void logAccountLocked_success() {
        // given
        Employee employee = mock(Employee.class);
        given(employee.getLoginId()).willReturn("testUser");
        given(employee.getHotelGroup()).willReturn(mock(HotelGroup.class));

        // when
        service.logAccountLocked(employee, "127.0.0.1");

        // then
        verify(loginLogRepository).save(any(LoginLog.class));
    }

    @Test
    @DisplayName("logAccountLocked: 저장 실패 시 예외를 로그만 남기고 전파하지 않음")
    void logAccountLocked_catchException() {
        // given
        Employee employee = mock(Employee.class);
        given(employee.getLoginId()).willReturn("testUser");
        given(employee.getHotelGroup()).willReturn(mock(HotelGroup.class));

        doThrow(new RuntimeException("DB Error")).when(loginLogRepository).save(any());

        // when
        service.logAccountLocked(employee, "127.0.0.1");

        // then - 예외가 전파되지 않음
        verify(loginLogRepository).save(any(LoginLog.class));
    }

    @Test
    @DisplayName("logEmployeeAccess: 직원 개인정보 조회 로그 저장 성공")
    void logEmployeeAccess_success() {
        // given
        Employee accessor = mock(Employee.class);
        given(accessor.getLoginId()).willReturn("admin");
        Employee targetEmployee = mock(Employee.class);
        given(targetEmployee.getLoginId()).willReturn("targetUser");

        // when
        service.logEmployeeAccess(accessor, targetEmployee, PermissionTypeKey.EMPLOYEE_READ, "정보 확인");

        // then
        verify(personalInformationLogRepository).save(any(PersonalInformationLog.class));
    }

    @Test
    @DisplayName("logEmployeeAccess: 저장 실패 시 예외를 로그만 남기고 전파하지 않음")
    void logEmployeeAccess_catchException() {
        // given
        Employee accessor = mock(Employee.class);
        given(accessor.getLoginId()).willReturn("admin");
        Employee targetEmployee = mock(Employee.class);
        given(targetEmployee.getLoginId()).willReturn("targetUser");

        doThrow(new RuntimeException("DB Error")).when(personalInformationLogRepository).save(any());

        // when
        service.logEmployeeAccess(accessor, targetEmployee, PermissionTypeKey.EMPLOYEE_READ, "정보 확인");

        // then - 예외가 전파되지 않음
        verify(personalInformationLogRepository).save(any(PersonalInformationLog.class));
    }

    @Test
    @DisplayName("logCustomerAccess: 저장 실패 시 예외를 로그만 남기고 전파하지 않음")
    void logCustomerAccess_catchException() {
        // given
        Employee accessor = mock(Employee.class);
        given(accessor.getLoginId()).willReturn("admin");
        Customer customer = mock(Customer.class);
        given(customer.getCustomerCode()).willReturn(123L);

        doThrow(new RuntimeException("DB Error")).when(personalInformationLogRepository).save(any());

        // when
        service.logCustomerAccess(accessor, customer, PermissionTypeKey.CUSTOMER_READ, "Check");

        // then - 예외가 전파되지 않음
        verify(personalInformationLogRepository).save(any(PersonalInformationLog.class));
    }
}
