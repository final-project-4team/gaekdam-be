package com.gaekdam.gaekdambe.unit.iam_service.auth.command.application.service;

import com.gaekdam.gaekdambe.hotel_service.hotel.command.domain.entity.HotelGroup;
import com.gaekdam.gaekdambe.iam_service.auth.command.application.service.LoginAuthService;
import com.gaekdam.gaekdambe.iam_service.employee.command.domain.EmployeeStatus;
import com.gaekdam.gaekdambe.iam_service.employee.command.domain.entity.Employee;
import com.gaekdam.gaekdambe.iam_service.employee.command.infrastructure.EmployeeRepository;
import com.gaekdam.gaekdambe.iam_service.log.command.application.service.AuditLogService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.ArgumentMatchers.any;


@ExtendWith(MockitoExtension.class)
class LoginAuthServiceTest {

    @InjectMocks
    private LoginAuthService service;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private AuditLogService auditLogService;

    @Test
    @DisplayName("loginSuccess: 로그인 성공 시 상태 업데이트 및 로그 기록")
    void loginSuccess() {
        // given
        Employee employee = mock(Employee.class);
        String userIp = "127.0.0.1";
        HotelGroup hotelGroup = mock(HotelGroup.class);

        given(employee.getLoginId()).willReturn("testUser");
        given(employee.getHotelGroup()).willReturn(hotelGroup);
        given(hotelGroup.getHotelGroupCode()).willReturn(1L);

        // when
        service.loginSuccess(employee, userIp);

        // then
        verify(employee).loginSuccess(); // 실패 카운트 초기화 등
        verify(auditLogService).logLoginSuccess(employee, userIp);
        verify(employeeRepository).save(employee);
    }

    @Test
    @DisplayName("loginFailed: 로그인 실패 시 카운트 증가 및 로그 기록 (잠금 아님)")
    void loginFailed_normal() {
        // given
        Employee employee = mock(Employee.class);
        String userIp = "127.0.0.1";
        String reason = "Password Mismatch";

        given(employee.getLoginId()).willReturn("testUser");
        given(employee.getFailedLoginCount()).willReturn(1); // 5회 미만
        // given(employee.getEmployeeStatus()).willReturn(EmployeeStatus.ACTIVE);
        // Unnecessary due to short-circuit

        // when
        service.loginFailed(employee, userIp, reason);

        // then
        verify(employee).loginFailed();
        verify(auditLogService).logLoginFailed(employee, userIp, reason);
        verify(employeeRepository).save(employee);

        // 잠금 로직 실행 안됨
        verify(employee, never()).employeeLocked();
        verify(auditLogService, never()).logAccountLocked(any(), any());
    }

    @Test
    @DisplayName("loginFailed: 5회 실패 시 계정 잠금 처리")
    void loginFailed_lock() {
        // given
        Employee employee = mock(Employee.class);
        String userIp = "127.0.0.1";
        String reason = "Password Mismatch";

        given(employee.getLoginId()).willReturn("testUser");
        // loginFailed() 호출 후 카운트가 5가 된다고 가정하거나, getFailedLoginCount()가 5를 리턴하도록 설정
        given(employee.getFailedLoginCount()).willReturn(5);
        given(employee.getEmployeeStatus()).willReturn(EmployeeStatus.ACTIVE);

        // when
        service.loginFailed(employee, userIp, reason);

        // then
        verify(employee).loginFailed();
        verify(auditLogService).logLoginFailed(employee, userIp, reason);

        // 잠금 로직 실행 확인
        verify(employee).employeeLocked();
        verify(auditLogService).logAccountLocked(employee, userIp);

        verify(employeeRepository).save(employee);
    }
}
