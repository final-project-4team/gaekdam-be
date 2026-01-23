package com.gaekdam.gaekdambe.iam_service.log.command.application.aop.aspect;

import com.gaekdam.gaekdambe.global.config.security.CustomUser;
import com.gaekdam.gaekdambe.iam_service.employee.command.domain.entity.Employee;
import com.gaekdam.gaekdambe.iam_service.employee.command.infrastructure.EmployeeRepository;
import com.gaekdam.gaekdambe.iam_service.log.command.application.aop.annotation.AuditLog;
import com.gaekdam.gaekdambe.iam_service.log.command.application.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditLogAspect {

    private final AuditLogService auditLogService;
    private final EmployeeRepository employeeRepository;

    @AfterReturning(pointcut = "@annotation(auditLog)", returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, AuditLog auditLog, Object result) {
        try {
            // 1. 현재 로그인한 직원 가져오기
            Employee accessor = getCurrentEmployee();
            if (accessor == null) {
                log.warn("AuditLog 생성 실패: 로그인된 사용자 정보가 없습니다. (Anonymous User?)");
                return;
            }

            // 2. 로그 저장 요청
            auditLogService.saveAuditLog(
                    accessor,
                    auditLog.type(),
                    auditLog.details());

        } catch (Exception e) {
            log.error("AuditLog Aspect 오류: {}", e.getMessage(), e);
        }
    }

    private Employee getCurrentEmployee() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUser) {
            String loginId = ((CustomUser) authentication.getPrincipal()).getUsername();
            return employeeRepository.findByLoginId(loginId).orElse(null);
        }
        return null;
    }
}
