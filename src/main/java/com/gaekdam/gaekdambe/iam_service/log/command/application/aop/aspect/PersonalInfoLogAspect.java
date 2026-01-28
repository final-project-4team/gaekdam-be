package com.gaekdam.gaekdambe.iam_service.log.command.application.aop.aspect;

import com.gaekdam.gaekdambe.global.config.security.CustomUser;
import com.gaekdam.gaekdambe.iam_service.employee.command.domain.entity.Employee;
import com.gaekdam.gaekdambe.iam_service.employee.command.infrastructure.EmployeeRepository;
import com.gaekdam.gaekdambe.iam_service.log.command.application.aop.annotation.LogPersonalInfo;
import com.gaekdam.gaekdambe.iam_service.log.command.application.service.AuditLogService;
import com.gaekdam.gaekdambe.iam_service.permission_type.command.domain.seeds.PermissionTypeKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.gaekdam.gaekdambe.customer_service.customer.command.infrastructure.repository.CustomerRepository;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class PersonalInfoLogAspect {

    private final AuditLogService auditLogService;
    private final EmployeeRepository employeeRepository;
    private final CustomerRepository customerRepository;

    @AfterReturning(value = "@annotation(logAnnotation)", returning = "result")
    // @LogPersonalInfo가 일치하는 함수 가져옴
    public void logAfterReturning(JoinPoint joinPoint, LogPersonalInfo logAnnotation, Object result) {
        // JoinPoint 누가 호출, 어떤 메서드, 무슨 파라미터 정보가 들어있음
        try {
            Employee accessor = getCurrentEmployee();
            if (accessor == null) {
                log.warn("개인정보 조회 로그 실패: 로그인된 사용자 정보가 없음");
                return;
            }

            // 메서드에 전달된 인자값 배열을 꺼냄 ex 유저 디테일 (args[0] = 호텔 그룹 코드 ,args[1] = 직원 코드 )
            Object[] args = joinPoint.getArgs();

            // 권한 타입에 따라 분기 처리
            if (isCustomerType(logAnnotation.type())) {
                handleCustomerLog(accessor, args, logAnnotation);
            } else if (isEmployeeType(logAnnotation.type())) {
                handleEmployeeLog(accessor, args, logAnnotation);
            }

        } catch (Exception e) {
            // 로깅 실패가 비즈니스 로직에 영향을 주지 않도록 로깅만 하고 무시
            log.error("AOP 개인정보 로깅 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    private void handleCustomerLog(Employee accessor, Object[] args, LogPersonalInfo annotation) {

        Long customerCode = findIdParameter(args);
        String reason = findReasonParameter(args);

        if (customerCode != null) {
            customerRepository.findById(customerCode).ifPresent(targetCustomer -> {
                auditLogService.logCustomerAccess(
                        accessor,
                        targetCustomer,
                        annotation.type(),
                        reason != null ? reason : annotation.purpose());
            });
        }
    }

    private void handleEmployeeLog(Employee accessor, Object[] args, LogPersonalInfo annotation) {
        Long employeeCode = findIdParameter(args);
        String reason = findReasonParameter(args);

        if (employeeCode != null) {
            employeeRepository.findById(employeeCode).ifPresent(targetEmployee -> {
                auditLogService.logEmployeeAccess(
                        accessor,
                        targetEmployee,
                        annotation.type(),
                        reason != null ? reason : annotation.purpose());
            });
        }
    }

    private Long findIdParameter(Object[] args) {
        // 가장 뒤에온 파라미터 부터 검사 , 타입이 Long이면 리턴(유저/고객 id로 판단하고)
        for (int i = args.length - 1; i >= 0; i--) {
            if (args[i] instanceof Long) {
                return (Long) args[i];
            }
        }
        return null;
    }

    private String findReasonParameter(Object[] args) {
        // 1. DTO 객체 내부의 "reason" 필드 탐색 (우선순위)
        for (Object arg : args) {
            if (arg == null || arg instanceof String || arg instanceof Number)
                continue;

            try {
                // 상위 클래스까지 탐색이 필요할 수 있으나, 보통 DTO는 평면적이므로 declaredField 먼저 시도
                java.lang.reflect.Field field = arg.getClass().getDeclaredField("reason");
                field.setAccessible(true);
                Object value = field.get(arg);
                if (value instanceof String && value != null && !((String) value).isEmpty()) {
                    return (String) value;
                }
            } catch (Exception e) {
                // 필드가 없거나 접근 불가능하면 무시
            }
        }

        // 2. 메서드 인자 중 String 타입 탐색 (Fallback)
        for (Object arg : args) {
            if (arg instanceof String) {
                return (String) arg;
            }
        }
        return null;
    }

    private boolean isCustomerType(PermissionTypeKey type) {
        return type.name().startsWith("CUSTOMER");
    }

    private boolean isEmployeeType(PermissionTypeKey type) {
        return type.name().startsWith("EMPLOYEE");
    }

    private Employee getCurrentEmployee() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUser) {
            return employeeRepository.findByLoginId(((CustomUser) authentication.getPrincipal())
                    .getUsername()).orElseThrow();
        }
        return null;
    }
}
