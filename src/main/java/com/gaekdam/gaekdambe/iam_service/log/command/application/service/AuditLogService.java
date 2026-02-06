package com.gaekdam.gaekdambe.iam_service.log.command.application.service;

import com.gaekdam.gaekdambe.customer_service.customer.command.domain.entity.Customer;
import com.gaekdam.gaekdambe.iam_service.employee.command.domain.entity.Employee;
import com.gaekdam.gaekdambe.iam_service.log.command.domain.LoginResult;
import com.gaekdam.gaekdambe.iam_service.log.command.domain.entity.LoginLog;
import com.gaekdam.gaekdambe.iam_service.log.command.domain.entity.PermissionChangedLog;
import com.gaekdam.gaekdambe.iam_service.log.command.domain.entity.PersonalInformationLog;
import com.gaekdam.gaekdambe.iam_service.log.command.infrastructure.LoginLogRepository;
import com.gaekdam.gaekdambe.iam_service.log.command.infrastructure.PermissionChangedLogRepository;
import com.gaekdam.gaekdambe.iam_service.log.command.infrastructure.PersonalInformationLogRepository;
import com.gaekdam.gaekdambe.iam_service.permission.command.domain.entity.Permission;
import com.gaekdam.gaekdambe.iam_service.permission_type.command.domain.seeds.PermissionTypeKey;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogService {

        private final LoginLogRepository loginLogRepository;
        private final PermissionChangedLogRepository permissionChangedLogRepository;
        private final PersonalInformationLogRepository personalInformationLogRepository;
        private final com.gaekdam.gaekdambe.iam_service.log.command.infrastructure.AuditLogRepository auditLogRepository;

        // 일반 활동 감사 로그 저장
        @Transactional(propagation = Propagation.REQUIRES_NEW)
        public void saveAuditLog(
                        Employee employee,
                        PermissionTypeKey type,
                        String details,
                        String previousValue,
                        String newValue) {
                try {
                        com.gaekdam.gaekdambe.iam_service.log.command.domain.entity.AuditLog log = com.gaekdam.gaekdambe.iam_service.log.command.domain.entity.AuditLog
                                        .createLog(
                                                        employee, type, details, previousValue, newValue);

                        auditLogRepository.save(log);

                } catch (Exception e) {
                        log.error("AuditLog 저장 실패 - employee: {}, error: {}",
                                        employee.getLoginId(), e.getMessage(), e);
                }
        }

        // 로그인 성공 로그 저장
        @Transactional(propagation = Propagation.REQUIRES_NEW)
        public void logLoginSuccess(Employee employee, String userIp) {
                try {
                        LoginLog successLog = LoginLog.createLoginLog(
                                        "LOGIN_SUCCESS",
                                        employee,
                                        userIp,
                                        LocalDateTime.now(),
                                        LoginResult.SUCCESS,
                                        null,
                                        employee.getHotelGroup());
                        loginLogRepository.save(successLog);

                        log.info("로그인 성공 로그 저장 완료 - loginId: {}, ip: {}, hotelGroup: {}",
                                        employee.getLoginId(),
                                        userIp,
                                        employee.getHotelGroup().getHotelGroupCode());

                } catch (Exception e) {
                        // 로그 저장 실패 시에도 로그인은 성공 처리
                        log.error("로그인 성공 로그 저장 실패 - loginId: {}, ip: {}, error: {}",
                                        employee.getLoginId(), userIp, e.getMessage(), e);
                }
        }

        // 로그인 실패 로그 저장

        @Transactional(propagation = Propagation.REQUIRES_NEW)
        public void logLoginFailed(Employee employee, String userIp, String failedReason) {
                try {
                        LoginLog failedLog = LoginLog.createLoginLog(
                                        "LOGIN_FAILED",
                                        employee,
                                        userIp,
                                        LocalDateTime.now(),
                                        LoginResult.FAIL,
                                        failedReason,
                                        employee.getHotelGroup());
                        loginLogRepository.save(failedLog);

                        log.warn("로그인 실패 로그 저장 완료 - loginId: {}, ip: {}, failedCount: {}",
                                        employee.getLoginId(),
                                        userIp,
                                        employee.getFailedLoginCount());

                } catch (Exception e) {
                        log.error("로그인 실패 로그 저장 실패 - loginId: {}, ip: {}, error: {}",
                                        employee.getLoginId(), userIp, e.getMessage(), e);
                }
        }

        // 로그인 시 계정 잠금 로그 저장
        @Transactional(propagation = Propagation.REQUIRES_NEW)
        public void logAccountLocked(Employee employee, String userIp) {
                try {
                        LoginLog lockedLog = LoginLog.createLoginLog(
                                        "ACCOUNT_LOCKED",
                                        employee,
                                        userIp,
                                        LocalDateTime.now(),
                                        LoginResult.FAIL,
                                        null,
                                        employee.getHotelGroup());
                        loginLogRepository.save(lockedLog);

                        log.error("계정 잠금 로그 저장 완료 - loginId: {}, ip: {}, reason: 로그인 5회 실패",
                                        employee.getLoginId(),
                                        userIp);

                } catch (Exception e) {
                        log.error("계정 잠금 로그 저장 실패 - loginId: {}, ip: {}, error: {}",
                                        employee.getLoginId(), userIp, e.getMessage(), e);
                }
        }

        // 권한 변경 로그 저장
        @Transactional(propagation = Propagation.REQUIRED)
        public void logPermissionChanged(
                        Employee employee,
                        Employee accessor,
                        Permission beforePermission,
                        Permission afterPermission) {

                try {

                        PermissionChangedLog auditLog = PermissionChangedLog.createPermissionChangedLog(
                                        LocalDateTime.now(),
                                        accessor,
                                        employee.getHotelGroup(),
                                        beforePermission,
                                        afterPermission,
                                        employee);
                        permissionChangedLogRepository.save(auditLog);

                        log.info("권한 변경 로그 저장 완료 - employee: ({}), accessor: {}, {} -> {}",
                                        employee.getLoginId(),
                                        accessor.getLoginId(),
                                        beforePermission.getPermissionName(),
                                        afterPermission.getPermissionName());

                } catch (Exception e) {
                        // 권한 변경 로그는 중요하므로 예외 발생 시 로그만 남기고 예외 전파
                        log.error("권한 변경 로그 저장 실패 - employeeCode: {}, accessorCode: {}, error: {}",
                                        employee.getEmployeeCode(),
                                        accessor.getEmployeeCode(),
                                        e.getMessage(), e);

                        // 감사 로그 저장 실패는 심각한 문제이므로 예외 재발생
                        throw new RuntimeException("권한 변경 감사 로그 저장 실패", e);
                }
        }

        // 직원의 고객 개인정보 조회 로그 저장
        @Transactional(propagation = Propagation.REQUIRES_NEW)
        public void logCustomerAccess(
                        Employee accessor,
                        Customer targetCustomer,
                        PermissionTypeKey permissionTypeKey,
                        String purpose) {

                try {
                        PersonalInformationLog accessLog = PersonalInformationLog.createCustomerAccessLog(
                                        accessor,
                                        targetCustomer,
                                        permissionTypeKey,
                                        purpose);

                        personalInformationLogRepository.save(accessLog);

                        log.info("고객 개인정보 조회 로그 저장 완료 - accessor: {}, customer: {}, type: {}, purpose: {}",
                                        accessor.getLoginId(),
                                        targetCustomer.getCustomerCode(),
                                        permissionTypeKey,
                                        purpose);

                } catch (Exception e) {
                        // 개인정보 조회 로그 저장 실패는 심각한 문제
                        log.error("CRITICAL: 고객 개인정보 조회 로그 저장 실패 - accessor: {}, customer: {}, error: {}",
                                        accessor.getLoginId(),
                                        targetCustomer != null ? targetCustomer.getCustomerCode() : "N/A",
                                        e.getMessage(), e);

                }
        }

        // 직원의 직원 개인정보 조회 로그 저장
        @Transactional(propagation = Propagation.REQUIRES_NEW)
        public void logEmployeeAccess(
                        Employee accessor,
                        Employee targetEmployee,
                        PermissionTypeKey permissionTypeKey,
                        String purpose) {

                try {
                        PersonalInformationLog accessLog = PersonalInformationLog.createEmployeeAccessLog(
                                        accessor,
                                        targetEmployee,
                                        permissionTypeKey,
                                        purpose);

                        personalInformationLogRepository.save(accessLog);

                        log.info("직원 개인정보 조회 로그 저장 완료 - accessor: {}, targetEmployee: {}, type: {}, purpose: {}",
                                        accessor.getLoginId(),
                                        targetEmployee.getLoginId(),
                                        permissionTypeKey,
                                        purpose);

                } catch (Exception e) {
                        log.error("CRITICAL: 직원 개인정보 조회 로그 저장 실패 - accessor: {}, targetEmployee: {}, error: {}",
                                        accessor.getLoginId(),
                                        targetEmployee != null ? targetEmployee.getLoginId() : "N/A",
                                        e.getMessage(), e);

                }
        }
}
