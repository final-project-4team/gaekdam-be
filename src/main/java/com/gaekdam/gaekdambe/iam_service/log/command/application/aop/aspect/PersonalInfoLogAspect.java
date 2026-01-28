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
import java.lang.reflect.Field;
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class PersonalInfoLogAspect {
  private final AuditLogService auditLogService;
  private final EmployeeRepository employeeRepository;
  private final CustomerRepository customerRepository;
  @AfterReturning(value = "@annotation(logAnnotation)", returning = "result")
  public void logAfterReturning(JoinPoint joinPoint, LogPersonalInfo logAnnotation, Object result) {
    try {
      Employee accessor = getCurrentEmployee();
      if (accessor == null) return;
      Object[] args = joinPoint.getArgs();
      if (isCustomerType(logAnnotation.type())) {
        // ✅ 수정됨: result(반환값)도 함께 전달
        handleCustomerLog(accessor, args, result, logAnnotation);
      } else if (isEmployeeType(logAnnotation.type())) {
        handleEmployeeLog(accessor, args, result, logAnnotation);
      }
    } catch (Exception e) {
      log.error("AOP 개인정보 로깅 중 오류 발생: {}", e.getMessage(), e);
    }
  }
  private void handleCustomerLog(Employee accessor, Object[] args, Object result, LogPersonalInfo annotation) {
    // ✅ 1순위: 반환값(Response DTO)에서 customerCode 찾기 (가장 정확함)
    Long customerCode = extractIdFromObject(result, "customerCode");

    // 2순위: 반환값 내부에 'customer' 객체가 있고 그 안에 'customerCode'가 있는 경우 (Nested DTO)
    if (customerCode == null) {
      customerCode = extractNestedId(result, "customer", "customerCode");
    }
    // 3순위: 기존 방식 (파라미터에서 찾기 - 최후의 수단)
    if (customerCode == null) {
      customerCode = findIdParameter(args);
    }
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
  private void handleEmployeeLog(Employee accessor, Object[] args, Object result, LogPersonalInfo annotation) {
    // 직원 로그도 동일한 로직 적용
    Long employeeCode = extractIdFromObject(result, "employeeCode");
    if (employeeCode == null) {
      employeeCode = findIdParameter(args);
    }
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
  // --- Helper Methods ---
  // DTO에서 특정 필드명(fieldName)의 Long 값을 꺼냄
  private Long extractIdFromObject(Object target, String fieldName) {
    if (target == null) return null;
    try {
      Field field = target.getClass().getDeclaredField(fieldName);
      field.setAccessible(true);
      Object value = field.get(target);
      if (value instanceof Long) {
        return (Long) value;
      }
    } catch (Exception e) {
      // 필드가 없거나 접근 불가하면 무시
    }
    return null;
  }
  // Nested DTO (ex: result.customer.customerCode) 처리
  private Long extractNestedId(Object target, String nestedObjectName, String fieldName) {
    if (target == null) return null;
    try {
      Field objectField = target.getClass().getDeclaredField(nestedObjectName);
      objectField.setAccessible(true);
      Object nestedObject = objectField.get(target);

      return extractIdFromObject(nestedObject, fieldName);
    } catch (Exception e) {
      return null;
    }
  }
  private Long findIdParameter(Object[] args) {
    for (int i = args.length - 1; i >= 0; i--) {
      if (args[i] instanceof Long) return (Long) args[i];
    }
    return null;
  }
  private String findReasonParameter(Object[] args) {
    // (...기존 코드 유지...)
    for (Object arg : args) {
      if (arg instanceof String) return (String) arg;
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