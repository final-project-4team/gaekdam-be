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
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.aspectj.lang.reflect.MethodSignature;

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
                // log.warn("AuditLog 생성 실패: 로그인된 사용자 정보가 없습니다. (Anonymous User?)");
                return;
            }

            // 2. SpEL 파싱 (동적 상세 내용 생성)
            String details = parseDetail(joinPoint, auditLog.details(), result);

            // 3. 로그 저장 요청
            auditLogService.saveAuditLog(
                    accessor,
                    auditLog.type(),
                    details,
                    null,
                    null);

        } catch (Exception e) {
            log.error("AuditLog Aspect 오류: {}", e.getMessage(), e);
        }
    }

    private String parseDetail(JoinPoint joinPoint, String detailsExpression, Object result) {
        if (detailsExpression == null || detailsExpression.isBlank()) {
            return "";
        }

        try {
            ExpressionParser parser = new SpelExpressionParser();
            StandardEvaluationContext context = new StandardEvaluationContext();

            // 메서드 파라미터 바인딩
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            String[] parameterNames = signature.getParameterNames();
            Object[] args = joinPoint.getArgs();

            if (parameterNames != null) {
                for (int i = 0; i < parameterNames.length; i++) {
                    context.setVariable(parameterNames[i], args[i]);
                }
            }

            // 리턴값 바인딩
            context.setVariable("result", result);

            // 파싱 실행
            return parser.parseExpression(detailsExpression).getValue(context, String.class);

        } catch (Exception e) {
            log.warn("AuditLog SpEL 파싱 실패 - expression: {}, error: {}", detailsExpression, e.getMessage());
            return detailsExpression; // 파싱 실패 시 원본 문자열 반환
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
