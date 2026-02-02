package com.gaekdam.gaekdambe.unit.iam_service.employee.command.application.service;

import com.gaekdam.gaekdambe.global.crypto.AesCryptoUtils;
import com.gaekdam.gaekdambe.global.crypto.DecryptionService;
import com.gaekdam.gaekdambe.global.crypto.KmsService;
import com.gaekdam.gaekdambe.global.crypto.PasswordValidator;
import com.gaekdam.gaekdambe.global.crypto.SearchHashService;
import com.gaekdam.gaekdambe.global.exception.CustomException;
import com.gaekdam.gaekdambe.global.smtp.MailSendService;
import com.gaekdam.gaekdambe.hotel_service.department.command.domain.entity.Department;
import com.gaekdam.gaekdambe.hotel_service.department.command.infrastructure.DepartmentRepository;
import com.gaekdam.gaekdambe.hotel_service.hotel.command.domain.entity.HotelGroup;
import com.gaekdam.gaekdambe.hotel_service.position.command.domain.entity.HotelPosition;
import com.gaekdam.gaekdambe.hotel_service.position.command.infrastructure.repository.HotelPositionRepository;
import com.gaekdam.gaekdambe.iam_service.employee.command.application.dto.request.EmployeeUpdateSecureRequest;
import com.gaekdam.gaekdambe.iam_service.employee.command.application.dto.request.PasswordChangeRequest;
import com.gaekdam.gaekdambe.iam_service.employee.command.application.service.EmployeeUpdateService;
import com.gaekdam.gaekdambe.iam_service.employee.command.domain.EmployeeStatus;
import com.gaekdam.gaekdambe.iam_service.employee.command.domain.entity.Employee;
import com.gaekdam.gaekdambe.iam_service.employee.command.infrastructure.EmployeeRepository;
import com.gaekdam.gaekdambe.iam_service.log.command.application.service.AuditLogService;
import com.gaekdam.gaekdambe.iam_service.permission.command.domain.entity.Permission;
import com.gaekdam.gaekdambe.iam_service.permission.command.infrastructure.PermissionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class EmployeeUpdateServiceTest {

    @InjectMocks
    private EmployeeUpdateService service;

    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private KmsService kmsService;
    @Mock
    private SearchHashService searchHashService;
    @Mock
    private DepartmentRepository departmentRepository;
    @Mock
    private HotelPositionRepository hotelPositionRepository;
    @Mock
    private PermissionRepository permissionRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuditLogService auditLogService;
    @Mock
    private MailSendService mailSendService;
    @Mock
    private DecryptionService decryptionService;

    @Test
    @DisplayName("update: 직원 정보 정상 수정 및 AuditLog 호출 확인")
    void updateEmployee_success() {
        // given
        Long hotelGroupCode = 100L;
        Long employeeCode = 1L;
        EmployeeUpdateSecureRequest request = new EmployeeUpdateSecureRequest(
                "010-9999-9999", "new@test.com", 20L, 30L, 40L, EmployeeStatus.ACTIVE);

        // Accessor
        Employee accessor = mock(Employee.class);
        HotelGroup accessorGroup = mock(HotelGroup.class);
        given(accessorGroup.getHotelGroupCode()).willReturn(hotelGroupCode);
        given(accessor.getHotelGroup()).willReturn(accessorGroup);
        given(accessor.getDekEnc()).willReturn(new byte[] { 1 });

        // Target Employee
        Employee target = mock(Employee.class);
        given(target.getEmployeeStatus()).willReturn(EmployeeStatus.ACTIVE);
        given(target.getPermission()).willReturn(mock(Permission.class));
        given(target.getPhoneNumberEnc()).willReturn(new byte[] { 1 }); // Ensure valid for decrypt

        given(target.getDekEnc()).willReturn(new byte[] { 2 });

        given(employeeRepository.findById(employeeCode)).willReturn(Optional.of(target));

        // Mock KMS
        given(kmsService.decryptDataKey(any()))
                .willReturn(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6 }); // plain 16bytes

        // Mock Find Deps
        Department newDept = mock(Department.class);
        given(newDept.getDepartmentName()).willReturn("NewDept");
        given(departmentRepository.findById(20L)).willReturn(Optional.of(newDept));

        HotelPosition newPos = mock(HotelPosition.class);
        given(newPos.getHotelPositionName()).willReturn("NewPos");
        given(hotelPositionRepository.findById(30L)).willReturn(Optional.of(newPos));

        Permission newPerm = mock(Permission.class); // diff logic
        given(newPerm.getPermissionName()).willReturn("NewPerm");
        given(permissionRepository.findById(40L)).willReturn(Optional.of(newPerm));

        // Start Static Mocks
        try (MockedStatic<AesCryptoUtils> mockedAes = Mockito.mockStatic(AesCryptoUtils.class,
                Mockito.withSettings().lenient())) {
            mockedAes.when(() -> AesCryptoUtils.encrypt(anyString(), any())).thenReturn(new byte[] { 9 });
            // decrypt for prev phone
            mockedAes.when(() -> AesCryptoUtils.decrypt(any(), any())).thenReturn("010-0000-0000");

            // when
            service.updateEmployee(hotelGroupCode, employeeCode, request, accessor);

            // then
            verify(target).updatePersonalInfo(any(), any(), any(), any());
            verify(target).updateOrganization(any(), any(), any());
            verify(target).updateStatus(EmployeeStatus.ACTIVE);
            verify(employeeRepository).save(target);

            // Verify Audit Log
            // Since permission changed, checking logPermissionChanged call
            // Since phone/dept/pos changed, checking saveAuditLog call
            verify(auditLogService).saveAuditLog(eq(accessor), any(), anyString(), anyString(), anyString());
        }
    }

    @Test
    @DisplayName("update: 부서/직급/상태 변경 시 Diff 로직 실행 확인")
    void updateEmployee_diffLogic_departmentPositionStatusChange() {
        // given
        Long hotelGroupCode = 100L;
        Long employeeCode = 1L;
        // 권한 변경 없이 부서/직급/상태만 변경
        EmployeeUpdateSecureRequest request = new EmployeeUpdateSecureRequest(
                "010-9999-9999", "new@test.com", 20L, 30L, null, EmployeeStatus.LOCKED);

        // Accessor
        Employee accessor = mock(Employee.class);
        HotelGroup accessorGroup = mock(HotelGroup.class);
        given(accessorGroup.getHotelGroupCode()).willReturn(hotelGroupCode);
        given(accessor.getHotelGroup()).willReturn(accessorGroup);
        given(accessor.getDekEnc()).willReturn(new byte[] { 1 });

        // Target Employee - 변경 전/후 상태를 순차적으로 반환하도록 설정
        Employee target = mock(Employee.class);

        Department oldDept = mock(Department.class);
        given(oldDept.getDepartmentName()).willReturn("OldDept");

        Department newDept = mock(Department.class);
        given(newDept.getDepartmentName()).willReturn("NewDept");

        // getDepartment()를 순차적으로 다르게 반환: 처음엔 oldDept, 나중엔 newDept
        given(target.getDepartment())
                .willReturn(oldDept) // 1st call (변경 전)
                .willReturn(oldDept) // 2nd call (updateOrganizationInfo 내부)
                .willReturn(newDept); // 3rd call (변경 후)

        HotelPosition oldPos = mock(HotelPosition.class);
        given(oldPos.getHotelPositionName()).willReturn("OldPos");

        HotelPosition newPos = mock(HotelPosition.class);
        given(newPos.getHotelPositionName()).willReturn("NewPos");

        given(target.getHotelPosition())
                .willReturn(oldPos) // 1st call (변경 전)
                .willReturn(oldPos) // 2nd call
                .willReturn(newPos); // 3rd call (변경 후)

        // getEmployeeStatus()도 순차 반환
        given(target.getEmployeeStatus())
                .willReturn(EmployeeStatus.ACTIVE) // 1st call (변경 전)
                .willReturn(EmployeeStatus.ACTIVE) // 2nd call
                .willReturn(EmployeeStatus.LOCKED); // 3rd call (변경 후)

        Permission perm = mock(Permission.class);
        given(perm.getPermissionCode()).willReturn(1L);
        given(perm.getPermissionName()).willReturn("SomePerm");
        given(target.getPermission()).willReturn(perm); // 권한은 변경 안됨

        given(target.getPhoneNumberEnc()).willReturn(new byte[] { 1 });
        given(target.getDekEnc()).willReturn(new byte[] { 2 });

        given(employeeRepository.findById(employeeCode)).willReturn(Optional.of(target));
        given(kmsService.decryptDataKey(any()))
                .willReturn(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6 });

        given(departmentRepository.findById(20L)).willReturn(Optional.of(newDept));
        given(hotelPositionRepository.findById(30L)).willReturn(Optional.of(newPos));

        try (MockedStatic<AesCryptoUtils> mockedAes = Mockito.mockStatic(AesCryptoUtils.class,
                Mockito.withSettings().lenient())) {
            mockedAes.when(() -> AesCryptoUtils.encrypt(anyString(), any())).thenReturn(new byte[] { 9 });
            mockedAes.when(() -> AesCryptoUtils.decrypt(any(), any())).thenReturn("010-0000-0000");

            // when
            service.updateEmployee(hotelGroupCode, employeeCode, request, accessor);

            // then
            // Diff 로직이 실행되어 변경사항이 감지되고 saveAuditLog가 호출되어야 함
            verify(auditLogService).saveAuditLog(
                    eq(accessor),
                    any(),
                    anyString(), // details에 부서/직급/상태 변경 포함
                    anyString(), // prev values
                    anyString() // new values
            );
        }
    }

    @Test
    @DisplayName("update : 비밀번호 변경 시 현재 비밀번호 불일치시 예외")
    void changePassword_fail_wrongCurrent() {
        // given
        Employee employee = mock(Employee.class);
        given(employee.getLoginId()).willReturn("testUser");
        given(employee.getPasswordHash()).willReturn("hashedOld");

        PasswordChangeRequest req = new PasswordChangeRequest("wrongOld", "newPass1!");

        given(passwordEncoder.matches("wrongOld", "hashedOld")).willReturn(false);

        try (MockedStatic<PasswordValidator> mockedVal = Mockito.mockStatic(PasswordValidator.class)) {
            // when
            Throwable t = catchThrowable(() -> service.changePassword(employee, req));

            // then
            assertThat(t).isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("현재 비밀번호가 일치하지 않습니다.");
        }
    }

    @Test
    @DisplayName("update : 비밀번호 변경 성공 (암호화 및 저장 확인)")
    void changePassword_success() {
        // given
        Employee employee = mock(Employee.class);
        given(employee.getLoginId()).willReturn("testUser");
        given(employee.getPasswordHash()).willReturn("hashedOld");

        PasswordChangeRequest req = new PasswordChangeRequest("correctOld", "newPass123!");

        given(passwordEncoder.matches("correctOld", "hashedOld")).willReturn(true);
        given(passwordEncoder.matches("newPass123!", "hashedOld")).willReturn(false);
        given(passwordEncoder.encode("newPass123!")).willReturn("hashedNew");

        try (MockedStatic<PasswordValidator> mockedVal = Mockito.mockStatic(PasswordValidator.class)) {
            // when
            service.changePassword(employee, req);

            // then
            verify(employee).changePassword("hashedNew");
            verify(employeeRepository).save(employee);
        }
    }

    @Test
    @DisplayName("update : 비밀번호 초기화 시 임시 비밀번호 생성 및 이메일 발송")
    void resetPassword_success() {
        // given
        Long empCode = 1L;
        Employee employee = mock(Employee.class);
        given(employee.getLoginId()).willReturn("user1");
        given(employee.getDekEnc()).willReturn(new byte[] { 1 });
        given(employee.getEmailEnc()).willReturn(new byte[] { 2 });

        given(employeeRepository.findById(empCode)).willReturn(Optional.of(employee));
        given(passwordEncoder.encode(anyString())).willReturn("encodedTemp");
        given(decryptionService.decrypt(anyLong(), any(), any())).willReturn("email@test.com");

        // when
        String tempPass = service.resetPassword(empCode);

        // then
        assertThat(tempPass).isNotNull();
        verify(employee).resetToActive("encodedTemp");
        verify(mailSendService).resetPasswordEmail(eq("email@test.com"), anyString());
    }

    @Test
    @DisplayName("update : 직원 상태를 Locked로 변경")
    void lockEmployee_success() {
        // given
        Long hgCode = 10L;
        Long empCode = 5L;

        Employee employee = mock(Employee.class);
        HotelGroup hg = mock(HotelGroup.class);
        given(hg.getHotelGroupCode()).willReturn(hgCode);
        given(employee.getHotelGroup()).willReturn(hg);
        given(employee.getEmployeeStatus()).willReturn(EmployeeStatus.ACTIVE);

        given(employeeRepository.findById(empCode)).willReturn(Optional.of(employee));

        // when
        service.lockEmployee(hgCode, empCode);

        // then
        verify(employee).employeeLocked();
        verify(employeeRepository).save(employee);
    }

    @Test
    @DisplayName("update: 호텔 그룹 불일치 시 updateEmployee 예외 발생")
    void updateEmployee_fail_hotelGroupMismatch() {
        // given
        Long hotelGroupCode = 100L;
        Long employeeCode = 1L;
        EmployeeUpdateSecureRequest request = new EmployeeUpdateSecureRequest(
                "010-9999-9999", "new@test.com", 20L, 30L, 40L, EmployeeStatus.ACTIVE);

        Employee accessor = mock(Employee.class);
        HotelGroup accessorGroup = mock(HotelGroup.class);
        given(accessorGroup.getHotelGroupCode()).willReturn(999L); // Different group
        given(accessor.getHotelGroup()).willReturn(accessorGroup);

        // when
        Throwable t = catchThrowable(() -> service.updateEmployee(hotelGroupCode, employeeCode, request, accessor));

        // then
        assertThat(t).isInstanceOf(CustomException.class);
        assertThat(((CustomException) t).getErrorCode())
                .isEqualTo(com.gaekdam.gaekdambe.global.exception.ErrorCode.HOTEL_GROUP_CODE_NOT_MATCH);
    }

    @Test
    @DisplayName("update: 직원 미존재 시 updateEmployee 예외 발생")
    void updateEmployee_fail_employeeNotFound() {
        // given
        Long hotelGroupCode = 100L;
        Long employeeCode = 999L;
        EmployeeUpdateSecureRequest request = new EmployeeUpdateSecureRequest(
                "010-9999-9999", "new@test.com", 20L, 30L, 40L, EmployeeStatus.ACTIVE);

        Employee accessor = mock(Employee.class);
        HotelGroup accessorGroup = mock(HotelGroup.class);
        given(accessorGroup.getHotelGroupCode()).willReturn(hotelGroupCode);
        given(accessor.getHotelGroup()).willReturn(accessorGroup);
        given(accessor.getDekEnc()).willReturn(new byte[] { 1 });

        given(employeeRepository.findById(employeeCode)).willReturn(Optional.empty());

        // when
        Throwable t = catchThrowable(() -> service.updateEmployee(hotelGroupCode, employeeCode, request, accessor));

        // then
        assertThat(t).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Employee not found");
    }

    @Test
    @DisplayName("update: 권한 변경 시 logPermissionChanged 호출 확인")
    void updateEmployee_permissionChange_callsLogPermissionChanged() {
        // given
        Long hotelGroupCode = 100L;
        Long employeeCode = 1L;
        EmployeeUpdateSecureRequest request = new EmployeeUpdateSecureRequest(
                "010-9999-9999", "new@test.com", 20L, 30L, 999L, EmployeeStatus.ACTIVE);

        Employee accessor = mock(Employee.class);
        HotelGroup accessorGroup = mock(HotelGroup.class);
        given(accessorGroup.getHotelGroupCode()).willReturn(hotelGroupCode);
        given(accessor.getHotelGroup()).willReturn(accessorGroup);
        given(accessor.getDekEnc()).willReturn(new byte[] { 1 });

        Employee target = mock(Employee.class);
        Permission oldPerm = mock(Permission.class);
        given(oldPerm.getPermissionCode()).willReturn(1L);
        given(target.getPermission()).willReturn(oldPerm);
        given(target.getEmployeeStatus()).willReturn(EmployeeStatus.ACTIVE);
        given(target.getDekEnc()).willReturn(new byte[] { 2 });
        given(target.getPhoneNumberEnc()).willReturn(new byte[] { 1 });

        given(employeeRepository.findById(employeeCode)).willReturn(Optional.of(target));
        given(kmsService.decryptDataKey(any()))
                .willReturn(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6 });

        Department newDept = mock(Department.class);
        given(departmentRepository.findById(20L)).willReturn(Optional.of(newDept));

        HotelPosition newPos = mock(HotelPosition.class);
        given(hotelPositionRepository.findById(30L)).willReturn(Optional.of(newPos));

        Permission newPerm = mock(Permission.class);
        given(newPerm.getPermissionCode()).willReturn(999L); // Different permission
        given(permissionRepository.findById(999L)).willReturn(Optional.of(newPerm));

        try (MockedStatic<AesCryptoUtils> mockedAes = Mockito.mockStatic(AesCryptoUtils.class,
                Mockito.withSettings().lenient())) {
            mockedAes.when(() -> AesCryptoUtils.encrypt(anyString(), any())).thenReturn(new byte[] { 9 });
            mockedAes.when(() -> AesCryptoUtils.decrypt(any(), any())).thenReturn("010-0000-0000");

            // when
            service.updateEmployee(hotelGroupCode, employeeCode, request, accessor);

            // then
            verify(auditLogService).logPermissionChanged(eq(target), eq(accessor), eq(oldPerm), eq(newPerm));
        }
    }

    @Test
    @DisplayName("update: 새 비밀번호가 현재 비밀번호와 동일할 때 예외")
    void changePassword_fail_newMatchesCurrent() {
        // given
        Employee employee = mock(Employee.class);
        given(employee.getLoginId()).willReturn("testUser");
        given(employee.getPasswordHash()).willReturn("hashedPass");

        PasswordChangeRequest req = new PasswordChangeRequest("currentPass", "currentPass");

        given(passwordEncoder.matches("currentPass", "hashedPass")).willReturn(true);

        try (MockedStatic<PasswordValidator> mockedVal = Mockito.mockStatic(PasswordValidator.class)) {
            // when
            Throwable t = catchThrowable(() -> service.changePassword(employee, req));

            // then
            assertThat(t).isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("새 비밀번호가 현재 비밀번호와 일치합니다.");
        }
    }

    @Test
    @DisplayName("update: 직원 미존재 시 resetPassword 예외 발생")
    void resetPassword_fail_employeeNotFound() {
        // given
        Long empCode = 999L;
        given(employeeRepository.findById(empCode)).willReturn(Optional.empty());

        // when
        Throwable t = catchThrowable(() -> service.resetPassword(empCode));

        // then
        assertThat(t).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Employee not found");
    }

    @Test
    @DisplayName("update: 호텔 그룹 불일치 시 lockEmployee 예외 발생")
    void lockEmployee_fail_hotelGroupMismatch() {
        // given
        Long hgCode = 10L;
        Long empCode = 5L;

        Employee employee = mock(Employee.class);
        HotelGroup hg = mock(HotelGroup.class);
        given(hg.getHotelGroupCode()).willReturn(999L); // Different group
        given(employee.getHotelGroup()).willReturn(hg);

        given(employeeRepository.findById(empCode)).willReturn(Optional.of(employee));

        // when
        Throwable t = catchThrowable(() -> service.lockEmployee(hgCode, empCode));

        // then
        assertThat(t).isInstanceOf(CustomException.class);
        assertThat(((CustomException) t).getErrorCode())
                .isEqualTo(com.gaekdam.gaekdambe.global.exception.ErrorCode.HOTEL_GROUP_CODE_NOT_MATCH);
    }

    @Test
    @DisplayName("update: unlockEmployee 성공 케이스")
    void unlockEmployee_success() {
        // given
        Long hgCode = 10L;
        Long empCode = 5L;

        Employee employee = mock(Employee.class);
        HotelGroup hg = mock(HotelGroup.class);
        given(hg.getHotelGroupCode()).willReturn(hgCode);
        given(employee.getHotelGroup()).willReturn(hg);
        given(employee.getEmployeeStatus()).willReturn(EmployeeStatus.LOCKED);

        given(employeeRepository.findById(empCode)).willReturn(Optional.of(employee));

        // when
        service.unlockEmployee(hgCode, empCode);

        // then
        verify(employee).employeeUnlocked();
        verify(employeeRepository).save(employee);
    }

    @Test
    @DisplayName("update: unlockEmployee DORMANCY 상태에서도 잠금 해제")
    void unlockEmployee_success_fromDormancy() {
        // given
        Long hgCode = 10L;
        Long empCode = 5L;

        Employee employee = mock(Employee.class);
        HotelGroup hg = mock(HotelGroup.class);
        given(hg.getHotelGroupCode()).willReturn(hgCode);
        given(employee.getHotelGroup()).willReturn(hg);
        given(employee.getEmployeeStatus()).willReturn(EmployeeStatus.DORMANCY);

        given(employeeRepository.findById(empCode)).willReturn(Optional.of(employee));

        // when
        service.unlockEmployee(hgCode, empCode);

        // then
        verify(employee).employeeUnlocked();
        verify(employeeRepository).save(employee);
    }

    @Test
    @DisplayName("update: 호텔 그룹 불일치 시 unlockEmployee 예외 발생")
    void unlockEmployee_fail_hotelGroupMismatch() {
        // given
        Long hgCode = 10L;
        Long empCode = 5L;

        Employee employee = mock(Employee.class);
        HotelGroup hg = mock(HotelGroup.class);
        given(hg.getHotelGroupCode()).willReturn(999L); // Different group
        given(employee.getHotelGroup()).willReturn(hg);

        given(employeeRepository.findById(empCode)).willReturn(Optional.of(employee));

        // when
        Throwable t = catchThrowable(() -> service.unlockEmployee(hgCode, empCode));

        // then
        assertThat(t).isInstanceOf(CustomException.class);
        assertThat(((CustomException) t).getErrorCode())
                .isEqualTo(com.gaekdam.gaekdambe.global.exception.ErrorCode.HOTEL_GROUP_CODE_NOT_MATCH);
    }

    @Test
    @DisplayName("update: dormancyEmployee 스케줄러 로직 검증")
    void dormancyEmployee_success() {
        // given
        Employee emp1 = mock(Employee.class);
        Employee emp2 = mock(Employee.class);
        List<Employee> targetEmployees = List.of(emp1, emp2);

        given(employeeRepository.findByEmployeeStatusAndLastLoginAtBefore(any(EmployeeStatus.class),
                any(LocalDateTime.class)))
                .willReturn(targetEmployees);

        // when
        service.dormancyEmployee();

        // then
        verify(emp1).employeeDormancy();
        verify(emp2).employeeDormancy();
        verify(employeeRepository).saveAll(targetEmployees);
    }
}
