package com.gaekdam.gaekdambe.communication_service.messaging.command.application.service;

import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ConditionExprEvaluator {

    private final ExpressionParser parser = new SpelExpressionParser();

    /**
     * conditionExpr가 비어있으면 true.
     * 표현식이 실패하면 false 처리(안전 우선).
     */
    public boolean evaluate(String conditionExpr, Map<String, Object> vars) {
        if (conditionExpr == null || conditionExpr.isBlank()) {
            return true;
        }

        try {
            StandardEvaluationContext ctx = new StandardEvaluationContext();
            vars.forEach(ctx::setVariable);

            Expression exp = parser.parseExpression(conditionExpr);
            Boolean result = exp.getValue(ctx, Boolean.class);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            return false;
        }
    }
}
